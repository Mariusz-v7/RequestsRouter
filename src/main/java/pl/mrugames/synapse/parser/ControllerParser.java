package pl.mrugames.synapse.parser;

import com.google.common.primitives.Primitives;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.util.DigestUtils;
import pl.mrugames.synapse.annotations.Controller;
import pl.mrugames.synapse.annotations.PathVar;
import pl.mrugames.synapse.annotations.Route;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ControllerParser {
    private final ExpressionParser expressionParser;

    ControllerParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
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

        String name = DigestUtils.md5DigestAsHex(String.class.getCanonicalName().getBytes());
        return new RouteParameter(name, ParameterResolution.SESSION, parameter.getType(), null, true);
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
}
