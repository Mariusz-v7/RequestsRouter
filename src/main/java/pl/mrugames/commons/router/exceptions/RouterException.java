package pl.mrugames.commons.router.exceptions;

public class RouterException extends RuntimeException {
    public RouterException(String message) {
        super(message);
    }

    public RouterException(String message, Throwable cause) {
        super(message, cause);
    }
}
