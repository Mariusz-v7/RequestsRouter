package pl.mrugames.commons.router;

import java.lang.reflect.Method;
import java.util.List;

class RouteInfo {
    static class Parameter {
        private String name;
        private Class<?> type;
        private String defaultValue;

        Parameter(String name, Class<?> type, String defaultValue) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
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
