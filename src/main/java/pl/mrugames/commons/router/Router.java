package pl.mrugames.commons.router;

import com.google.common.base.Defaults;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import pl.mrugames.commons.router.annotations.ArgDefaultValue;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class Router {
    private final Map<String, RouteInfo> routes;
    private final RouterInitializer initializer;
    private final AntPathMatcher pathMatcher;
    private final ExecutableValidator validator;

    private Router(RouterInitializer initializer, AntPathMatcher antPathMatcher, ExecutableValidator executableValidator) {
        this.initializer = initializer;
        this.routes = new HashMap<>();
        this.pathMatcher = antPathMatcher;
        this.validator = executableValidator;
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
                           Map<Class<?>, Optional<Object>> sessionParameters) throws InvocationTargetException, IllegalAccessException {

        List<RouteInfo.Parameter> parameters = routeInfo.getParameters();
        Object[] args = new Object[parameters.size()];

        int i = 0;
        for (RouteInfo.Parameter parameter : parameters) {
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

        Set<ConstraintViolation<Object>> constraints = validator.validateParameters(routeInfo.getControllerInstance(), routeInfo.getMethod(), args);
        if (!constraints.isEmpty()) {
            throw new ConstraintViolationException(constraints);
        }

        return routeInfo.getMethod().invoke(routeInfo.getControllerInstance(), args);
    }

}
