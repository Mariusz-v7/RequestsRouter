package pl.mrugames.commons.router;

import java.io.Serializable;

public class RouteParameter implements Serializable {
    public enum ParameterType {
        PATH_VAR, ARG, NONE
    }

    private final String name;
    private final Class<?> type;
    private final String defaultValue;
    private final ParameterType parameterType;
    private final Class<?>[] generics;

    RouteParameter(String name, Class<?> type, String defaultValue, ParameterType parameterType, Class<?>[] generics) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.parameterType = parameterType;
        this.generics = generics;
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

    public Class<?>[] getGenerics() {
        return generics;
    }
}
