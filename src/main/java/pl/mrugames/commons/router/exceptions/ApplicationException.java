package pl.mrugames.commons.router.exceptions;

import pl.mrugames.commons.router.ResponseStatus;

public class ApplicationException extends RuntimeException {
    private final ResponseStatus responseStatus;

    public ApplicationException(ResponseStatus responseStatus, String message) {
        super(message);
        this.responseStatus = responseStatus;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }
}
