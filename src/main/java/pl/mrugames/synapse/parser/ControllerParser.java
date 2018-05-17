package pl.mrugames.synapse.parser;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.util.DigestUtils;
import pl.mrugames.synapse.annotations.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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

    private RouteParameter parseParameter(Parameter parameter) {
        //TODO: generics

        Arg arg = parameter.getAnnotation(Arg.class);
        if (arg != null) {
            return new RouteParameter(arg.value(), ParameterResolution.PAYLOAD, parameter.getType(), resolveDefaultValue(arg.defaultValue()));
        }

        PathVar pathVar = parameter.getAnnotation(PathVar.class);
        if (pathVar != null) {
            return new RouteParameter(pathVar.value(), ParameterResolution.PATH_VAR, parameter.getType(), null);
        }

        SessionVar sessionVar = parameter.getAnnotation(SessionVar.class);
        if (sessionVar != null) {
            return new RouteParameter(sessionVar.value(), ParameterResolution.SESSION, parameter.getType(), resolveDefaultValue(sessionVar.defaultValue()));
        }

        String encodedName = DigestUtils.md5DigestAsHex(parameter.getType().getCanonicalName().getBytes());

        return new RouteParameter(encodedName, ParameterResolution.SESSION, parameter.getType(), null);
    }

    Object resolveDefaultValue(String defaultValue) {
        if (Objects.equals(defaultValue, NullDefaultValueIdentifier.NULL_DEFAULT_VALUE_IDENTIFIER)) {
            return null;
        }

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
