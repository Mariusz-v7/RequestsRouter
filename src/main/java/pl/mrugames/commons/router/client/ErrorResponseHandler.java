package pl.mrugames.commons.router.client;

import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.request_handlers.ExceptionHandler;

import javax.annotation.PostConstruct;

@Component
class ErrorResponseHandler {
    private final ExceptionHandler exceptionHandler;

    ErrorResponseHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @PostConstruct
    void init() {
        exceptionHandler.registerHandler(ErrorResponseException.class, e -> new Response(-1, e.getResponseStatus(), e.getMessage()));
    }
}
