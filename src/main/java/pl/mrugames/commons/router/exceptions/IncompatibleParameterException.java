package pl.mrugames.commons.router.exceptions;

public class IncompatibleParameterException extends RouterException {
    private final String incompatibleParameter;

    public IncompatibleParameterException(String incompatibleParameter, Class<?> expected, Class<?> actual) {
        super("Incompatible parameter: '" + incompatibleParameter + "'. Expected: '" + expected + "', but actual was: '" + actual + "'");
        this.incompatibleParameter = incompatibleParameter;
    }

    public IncompatibleParameterException(String incompatibleParameter, Class<?> expected) {
        super("Could not convert '" + incompatibleParameter + "' into '" + expected + "'");
        this.incompatibleParameter = incompatibleParameter;
    }

    public String getIncompatibleParameter() {
        return incompatibleParameter;
    }
}
