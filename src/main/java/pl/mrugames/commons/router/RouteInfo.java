package pl.mrugames.commons.router;

import java.lang.reflect.Method;
import java.util.List;

class RouteInfo {
    enum ParameterType {
        PATH_VAR, ARG, NONE
    }

    static class Parameter {
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

        String getName() {
            return name;
        }

        Class<?> getType() {
            return type;
        }

        String getDefaultValue() {
            return defaultValue;
        }

        ParameterType getParameterType() {
            return parameterType;
        }
    }

    private final Object controllerInstance;
    private final Method method;
    private final List<Parameter> parameters;

    RouteInfo(Object controllerInstance, Method method, List<Parameter> parameters) {
        this.controllerInstance = controllerInstance;
        this.method = method;
        this.parameters = parameters;
    }

    Object getControllerInstance() {
        return controllerInstance;
    }

    Method getMethod() {
        return method;
    }

    List<Parameter> getParameters() {
        return parameters;
    }
}
