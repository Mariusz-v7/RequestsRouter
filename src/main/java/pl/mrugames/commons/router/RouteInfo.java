package pl.mrugames.commons.router;

import java.lang.reflect.Method;
import java.util.List;

public class RouteInfo {
    public enum ParameterType {
        PATH_VAR, ARG, NONE
    }

    public static class Parameter {
        private final String name;
        private final Class<?> type;
        private final String defaultValue;
        private final ParameterType parameterType;

        Parameter(String name, Class<?> type, String defaultValue, ParameterType parameterType) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
            this.parameterType = parameterType;
        }

        public String getName() {
            return name;
        }

        public Class<?> getType() {
            return type;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public ParameterType getParameterType() {
            return parameterType;
        }
    }

    private final Object controllerInstance;
    private final Method method;
    private final List<Parameter> parameters;
    private final String routePattern;

    RouteInfo(Object controllerInstance, Method method, List<Parameter> parameters, String routePattern) {
        this.controllerInstance = controllerInstance;
        this.method = method;
        this.parameters = parameters;
        this.routePattern = routePattern;
    }

    Object getControllerInstance() {
        return controllerInstance;
    }

    Method getMethod() {
        return method;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public String getRoutePattern() {
        return routePattern;
    }
}
