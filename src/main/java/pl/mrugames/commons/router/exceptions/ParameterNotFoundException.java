package pl.mrugames.commons.router.exceptions;

public class ParameterNotFoundException extends RouterException {
    private final String missingParameter;

    public ParameterNotFoundException(String missingParameter) {
        super("Could not find '" + missingParameter + "' parameter in the request");
        this.missingParameter = missingParameter;
    }

    public ParameterNotFoundException(String missingParameter, Throwable cause) {
        super("Could not find '" + missingParameter + "' parameter in the request", cause);
        this.missingParameter = missingParameter;
    }

    public String getMissingParameter() {
        return missingParameter;
    }
}
