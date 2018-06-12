package pl.mrugames.synapse.parser;

import javax.annotation.Nullable;

public class RouteParameter {
    private final String name;
    private final ParameterResolution resolution;
    private final Class<?> type;
    private final Object defaultValue;
    private final boolean required;

    public RouteParameter(String name, ParameterResolution resolution, Class<?> type, @Nullable Object defaultValue, boolean required) {
        this.name = name;
        this.resolution = resolution;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public ParameterResolution getResolution() {
        return resolution;
    }

    public Class<?> getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }
}
