package pl.mrugames.commons.router;

import com.google.common.base.Defaults;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import pl.mrugames.commons.router.annotations.ArgDefaultValue;
import pl.mrugames.commons.router.exceptions.RouteConstraintViolationException;
import pl.mrugames.commons.router.exceptions.RouterException;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class Router {
    private final Map<String, RouteInfo> routes;
    private final RouterInitializer initializer;
    private final AntPathMatcher pathMatcher;

    Router(RouterInitializer initializer, AntPathMatcher antPathMatcher) {
        this.initializer = initializer;
        this.routes = new HashMap<>();
        this.pathMatcher = antPathMatcher;
    }

    @PostConstruct
    private void postConstruct() {
        routes.putAll(initializer.getRoutes());
    }

    public RouteInfo findRoute(String route, RequestMethod requestMethod) {
        route = requestMethod.name() + ":" + route;

        for (Map.Entry<String, RouteInfo> entry : routes.entrySet()) {
            if (pathMatcher.match(entry.getKey(), route)) {
                return entry.getValue();
            }
        }

        throw new IllegalArgumentException("Route not found: " + route);
    }

    public Object navigate(RouteInfo routeInfo,
                           Map<String, Object> pathParameters,
                           Map<String, Object> payloadParameters,
                           Map<Class<?>, Optional<Object>> sessionParameters) throws IllegalAccessException {

        List<RouteParameter> parameters = routeInfo.getParameters();
        Object[] args = new Object[parameters.size()];

        int i = 0;
        for (RouteParameter parameter : parameters) {
            switch (parameter.getParameterType()) {
                case PATH_VAR:
                    args[i] = pathParameters.get(parameter.getName());
                    break;
                case ARG:
                    if (payloadParameters.containsKey(parameter.getName())) {
                        args[i] = payloadParameters.get(parameter.getName());
                    } else if (parameter.getDefaultValue().equals(ArgDefaultValue.ARG_NULL_DEFAULT_VALUE)) {
                        throw new IllegalArgumentException("Missing argument: " + parameter.getName());
                    } else {
                        args[i] = parameter.getDefaultValue();
                    }
                    break;
                case NONE:
                    Optional<Object> arg = sessionParameters.get(parameter.getType());
                    if (arg.isPresent()) {
                        args[i] = arg.get();
                    } else {
                        args[i] = Defaults.defaultValue(parameter.getType());
                    }
                    break;
            }

            ++i;
        }

        try {
            return routeInfo.getMethod().invoke(routeInfo.getControllerInstance(), args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();

            if (cause instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) cause;

                List<String> messages = cve.getConstraintViolations().stream()
                        .map(c -> getConstraintMessage(c, routeInfo.getParameters()))
                        .collect(Collectors.toList());

                throw new RouteConstraintViolationException(messages);
            }

            if (cause instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else if (cause instanceof Exception) {
                throw new RouterException("Method invocation exception", e);
            }

            throw (Error) e.getCause();
        }
    }

    private String getConstraintMessage(ConstraintViolation<?> constraintViolation, List<RouteParameter> parameters) {
        Iterator<Path.Node> iterator = constraintViolation.getPropertyPath().iterator();
        Path.Node parameter = null;
        while (iterator.hasNext()) {
            parameter = iterator.next();
        }

        if (parameter == null) {
            throw new IllegalStateException("Shouldn't be null");
        }

        if (!(parameter instanceof NodeImpl)) {
            throw new IllegalStateException("Should be hibernate implementation");
        }

        int index = ((NodeImpl) parameter).getParameterIndex();

        if (index < 0) {
            throw new IllegalStateException("Parameter index should be greater or equal to 0");
        }

        if (parameters.size() < index) {
            throw new IllegalStateException("List should contain the parameter");
        }

        String paramName = parameters.get(index).getName();

        return paramName + ": " + constraintViolation.getMessage();
    }

}
