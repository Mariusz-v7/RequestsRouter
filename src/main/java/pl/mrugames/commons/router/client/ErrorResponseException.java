package pl.mrugames.commons.router.client;

import pl.mrugames.commons.router.ResponseStatus;

public class ErrorResponseException extends RuntimeException {
    private final ResponseStatus responseStatus;

    ErrorResponseException(ResponseStatus responseStatus, String message) {
        super(message);
        this.responseStatus = responseStatus;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }
}
