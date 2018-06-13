package pl.mrugames.synapse.parser;

import com.google.common.primitives.Primitives;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.util.DigestUtils;
import pl.mrugames.synapse.RequestMethod;
import pl.mrugames.synapse.annotations.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ControllerParser {
    private final ExpressionParser expressionParser;

    ControllerParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    Map<RequestMethod, Map<String, RouteData>> parseRoutes(List<Object> controllers) {
        /* TODO:
        {
            GET: {
                'route1/xxx': route data,
                'route2/yyy': route data,
            },
            POST: {
                'route1/...': route data,
                'route2/...': route data,
            }
        }
         */

        Map<RequestMethod, Map<String, RouteData>> map = new HashMap<>();
        for (RequestMethod method : RequestMethod.values()) {
            map.put(method, new HashMap<>());
        }

        return Collections.unmodifiableMap(map);
    }

    Controller getControllerAnnotation(Object controllerInstance) {
        Controller controller = controllerInstance.getClass().getAnnotation(Controller.class);

        if (controller == null) {
            throw new IllegalArgumentException(controllerInstance.getClass().getName() + " is not a controller! (missing annotation)");
        }

        return controller;
    }

    List<Method> getRoutes(Object controllerInstance) {
        List<Method> methods = new LinkedList<>();

        methods.addAll(Arrays.asList(controllerInstance.getClass().getDeclaredMethods()));

        methods = getSuperRoutes(controllerInstance.getClass(), methods);

        return methods.stream()
                .filter(this::hasRouteAnnotation)
                .collect(Collectors.toList());
    }

    List<RouteParameter> getRouteParameters(Method route) {
        return Stream.of(route.getParameters())
                .map(this::parseParameter)
                .collect(Collectors.toList());
    }

    RouteParameter parseParameter(Parameter parameter) {
        PathVar pathVar = parameter.getAnnotation(PathVar.class);

        if (pathVar != null) {
            if (pathVar.value().isEmpty()) {
                throw new IllegalArgumentException("Failed to parse parameter. Empty value cannot be provided for @PathVar annotation.");
            }

            Pattern pattern = Pattern.compile("[-A-Za-z0-9_]+");
            if (!pattern.matcher(pathVar.value()).matches()) {
                throw new IllegalArgumentException("Failed to parse parameter. Value '" + pathVar.value() + "' is not allowed.");
            }

            if (!parameter.getType().isPrimitive() && !Primitives.isWrapperType(parameter.getType())) {
                throw new IllegalArgumentException("Failed to parse parameter: '" + pathVar.value() + "'. Only primitive types can be set for @PathVar annotation. Type: " + parameter.getType().getCanonicalName());
            }

            return new RouteParameter(pathVar.value(), ParameterResolution.PATH_VAR, parameter.getType(), null, true);
        }

        Arg arg = parameter.getAnnotation(Arg.class);
        if (arg != null) {
            if (arg.value().isEmpty()) {
                throw new IllegalArgumentException("Failed to parse parameter. Name has to be provided for @Arg annotation");
            }

            Object defaultVal = resolveDefaultValue(arg.defaultValue());
            return new RouteParameter(arg.value(), ParameterResolution.PAYLOAD, parameter.getType(), defaultVal, arg.required());
        }

        SessionVar sessionVar = parameter.getAnnotation(SessionVar.class);
        if (sessionVar != null) {

            Object defaultVal = resolveDefaultValue(sessionVar.defaultValue());
            return new RouteParameter(computeName(sessionVar.value(), parameter.getType()), ParameterResolution.SESSION, parameter.getType(), defaultVal, sessionVar.required());
        }

        return new RouteParameter(computeName("", parameter.getType()), ParameterResolution.SESSION, parameter.getType(), null, true);
    }

    Object resolveDefaultValue(String defaultValue) {
        Expression expression = expressionParser.parseExpression(defaultValue);

        return expression.getValue();
    }

    private List<Method> getSuperRoutes(Class<?> controllerClass, List<Method> methods) {
        Class<?> superclass = controllerClass.getSuperclass();

        if (superclass == null) {
            return methods;
        }

        methods.addAll(Arrays.asList(superclass.getDeclaredMethods()));

        return getSuperRoutes(superclass, methods);
    }

    private boolean hasRouteAnnotation(Method method) {
        return method.isAnnotationPresent(Route.class);
    }

    private String computeName(String annotationValue, Class<?> type) {
        if (annotationValue.isEmpty()) {
            return DigestUtils.md5DigestAsHex(type.getCanonicalName().getBytes());
        }

        return annotationValue;
    }
}
