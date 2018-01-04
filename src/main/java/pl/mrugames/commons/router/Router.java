package pl.mrugames.commons.router;

import com.google.common.base.Defaults;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import pl.mrugames.commons.router.annotations.ArgDefaultValue;
import pl.mrugames.commons.router.annotations.Translate;
import pl.mrugames.commons.router.exceptions.RouteConstraintViolationException;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class Router {
    private final Map<String, RouteInfo> routes;
    private final RouterInitializer initializer;
    private final AntPathMatcher pathMatcher;
    private final I18nReplacer i18nReplacer;

    Router(RouterInitializer initializer, AntPathMatcher antPathMatcher, I18nReplacer i18nReplacer) {
        this.initializer = initializer;
        this.routes = new HashMap<>();
        this.pathMatcher = antPathMatcher;
        this.i18nReplacer = i18nReplacer;
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
            Object returnValue = routeInfo.getMethod().invoke(routeInfo.getControllerInstance(), args);
            if (returnValue == null) {
                Class<?> returnType = routeInfo.getMethod().getReturnType();

                if (returnType != void.class) {
                    return Mono.NO_VAL;
                } else {
                    return null;
                }
            }

            if (returnValue instanceof String) {
                returnValue = i18nReplacer.replace((String) returnValue);
            } else if (returnValue.getClass().isAnnotationPresent(Translate.class)) {
                translateObject(returnValue);
            }

            return returnValue;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();

            if (cause instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) cause;

                List<String> messages = cve.getConstraintViolations().stream()
                        .map(c -> getConstraintMessage(c, routeInfo.getParameters(), routeInfo.getMethod().getDeclaringClass()))
                        .collect(Collectors.toList());

                throw new RouteConstraintViolationException(messages);
            }

            if (cause instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else if (cause instanceof Exception) {
                throw new RouteExceptionWrapper(cause);
            }

            throw (Error) e.getCause();
        }
    }

    private String getConstraintMessage(ConstraintViolation<?> constraintViolation, List<RouteParameter> parameters, Class<?> controllerClass) {
        if (!controllerClass.equals(constraintViolation.getRootBeanClass())) {
            return String.format("value '%s' %s", constraintViolation.getInvalidValue(), constraintViolation.getMessage());
        }

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

    private void translateObject(Object returnValue) throws IllegalAccessException {

        for (Field field : returnValue.getClass().getDeclaredFields()) {
            boolean accessible = field.isAccessible();

            if (!field.getType().isAssignableFrom(String.class)) {
                if (field.getType().isAnnotationPresent(Translate.class)) {
                    if (!accessible) {
                        field.setAccessible(true);
                    }

                    Object nestedValue = field.get(returnValue);

                    if (!accessible) {
                        field.setAccessible(false);
                    }

                    translateObject(nestedValue);
                }

                continue;
            }

            if (!accessible) {
                field.setAccessible(true);
            }

            String str = (String) field.get(returnValue);
            String translated = i18nReplacer.replace(str);

            if (!str.equals(translated) && Modifier.isFinal(field.getModifiers())) {
                throw new IllegalArgumentException("Field " + field.getName() + " is final. Cannot apply translation.");
            }

            if (!str.equals(translated)) {
                field.set(returnValue, translated);
            }

            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }

}
