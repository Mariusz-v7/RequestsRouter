package pl.mrugames.commons.router.request_handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.ResponseStatus;
import pl.mrugames.commons.router.exceptions.ApplicationException;
import pl.mrugames.commons.router.exceptions.IncompatibleParameterException;
import pl.mrugames.commons.router.exceptions.ParameterNotFoundException;
import pl.mrugames.commons.router.exceptions.RouteConstraintViolationException;
import pl.mrugames.commons.router.sessions.SessionDoesNotExistException;
import pl.mrugames.commons.router.sessions.SessionExpiredException;

@Service
class ExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    ExceptionHandler() {
    }

    Response handle(long requestId, Throwable e) {

        if (e instanceof ParameterNotFoundException || e instanceof IllegalArgumentException || e instanceof IncompatibleParameterException) {
            return new Response(requestId, ResponseStatus.BAD_REQUEST, e.getMessage());
        }

        if (e instanceof RouteConstraintViolationException) {
            return new Response(requestId, ResponseStatus.BAD_PARAMETERS, ((RouteConstraintViolationException) e).getMessages());
        }

        if (e instanceof SessionExpiredException) {
            return new Response(requestId, ResponseStatus.BAD_REQUEST, "Session expired");
        }

        if (e instanceof SessionDoesNotExistException) {
            return new Response(requestId, ResponseStatus.BAD_REQUEST, "Session does not exist");
        }

        if (e instanceof ApplicationException) {
            return new Response(requestId, ((ApplicationException) e).getResponseStatus(), e.getMessage());
        }

        if (e instanceof AuthenticationException || e instanceof AccessDeniedException) {
            return new Response(requestId, ResponseStatus.PERMISSION_DENIED, e.getMessage());
        }

        logger.error("Internal error while processing the request", e);
        return new Response(requestId, ResponseStatus.INTERNAL_ERROR, String.format("Error: %s, %s", e.getMessage(), ErrorUtil.exceptionStackTraceToString(e)));
    }
}
