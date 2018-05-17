package pl.mrugames.synapse.parser;

public class RouteParameter {
    private final String name;
    private final ParameterResolution resolution;
    private final Class<?> type;

    public RouteParameter(String name, ParameterResolution resolution, Class<?> type) {
        this.name = name;
        this.resolution = resolution;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RouteParameter that = (RouteParameter) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (resolution != that.resolution) return false;
        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (resolution != null ? resolution.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RouteParameter{" +
                "name='" + name + '\'' +
                ", resolution=" + resolution +
                ", type=" + type +
                '}';
    }
}
