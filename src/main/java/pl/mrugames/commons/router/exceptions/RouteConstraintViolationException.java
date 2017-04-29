package pl.mrugames.commons.router.exceptions;

import java.util.List;

public class RouteConstraintViolationException extends RuntimeException {
    private final List<String> messages;

    public RouteConstraintViolationException(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}
