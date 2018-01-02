package pl.mrugames.commons.router.request_handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.ResponseStatus;
import pl.mrugames.commons.router.RouteExceptionWrapper;
import pl.mrugames.commons.router.exceptions.ApplicationException;
import pl.mrugames.commons.router.exceptions.IncompatibleParameterException;
import pl.mrugames.commons.router.exceptions.ParameterNotFoundException;
import pl.mrugames.commons.router.exceptions.RouteConstraintViolationException;
import pl.mrugames.commons.router.sessions.SessionDoesNotExistException;
import pl.mrugames.commons.router.sessions.SessionExpiredException;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

@Service
public class ExceptionHandler {
    private class Handler<T extends Throwable> {
        final Class<T> supportedType;
        final Function<T, Response> handler;

        Handler(Class<T> supportedType, Function<T, Response> handler) {
            this.supportedType = supportedType;
            this.handler = handler;
        }
    }

    private Logger logger = LoggerFactory.getLogger(getClass());
    private final List<Handler<Throwable>> handlers;

    ExceptionHandler() {
        handlers = new CopyOnWriteArrayList<>();
    }

    @PostConstruct
    void init() {
        registerHandler(ParameterNotFoundException.class, e -> new Response(-1, ResponseStatus.BAD_REQUEST, e.getMessage()));
        registerHandler(IllegalArgumentException.class, e -> new Response(-1, ResponseStatus.BAD_REQUEST, e.getMessage()));
        registerHandler(IncompatibleParameterException.class, e -> new Response(-1, ResponseStatus.BAD_REQUEST, e.getMessage()));
        registerHandler(RouteConstraintViolationException.class, e -> new Response(-1, ResponseStatus.BAD_PARAMETERS, e.getMessages()));
        registerHandler(SessionExpiredException.class, e -> new Response(-1, ResponseStatus.BAD_REQUEST, "Session expired"));
        registerHandler(SessionDoesNotExistException.class, e -> new Response(-1, ResponseStatus.BAD_REQUEST, "Session does not exist"));
        registerHandler(ApplicationException.class, e -> new Response(-1, e.getResponseStatus(), e.getMessage()));
        registerHandler(AuthenticationException.class, e -> new Response(-1, ResponseStatus.PERMISSION_DENIED, e.getMessage()));
        registerHandler(AccessDeniedException.class, e -> new Response(-1, ResponseStatus.PERMISSION_DENIED, e.getMessage()));
    }

    Response handle(long requestId, Throwable e) {
        if (e instanceof RouteExceptionWrapper) {
            e = e.getCause();
        }

        Handler<Throwable> mostSpecific = null;
        for (Handler<Throwable> handler : handlers) {
            if (!handler.supportedType.isAssignableFrom(e.getClass())) {
                continue;
            }

            if (mostSpecific == null || mostSpecific.supportedType.isAssignableFrom(handler.supportedType)) {
                mostSpecific = handler;
            }
        }

        if (mostSpecific != null) {
            Response sample = mostSpecific.handler.apply(e);
            return new Response(requestId, sample.getStatus(), sample.getPayload());
        }

        logger.error("Internal error while processing the request", e);
        return new Response(requestId, ResponseStatus.INTERNAL_ERROR, String.format("Error: %s, %s", e.getMessage(), ErrorUtil.exceptionStackTraceToString(e)));
    }

    /**
     * @param handler - handler which translates the exception to response. Response id will be rewritten thus
     *                it doesn't matter.
     *                example:
     *                exceptionHandler.registerHandler(RuntimeException.class,
     *                e -> new Response(-1, ResponseStatus.BAD_PARAMETERS, customPayload));
     */
    @SuppressWarnings("unchecked")
    public <T extends Throwable> void registerHandler(@NotNull Class<T> supportedType, Function<T, Response> handler) {
        if (supportedType == null) {
            throw new IllegalArgumentException("Supported type may not be null");
        }

        for (Handler existing : handlers) {
            if (supportedType.equals(existing.supportedType)) {
                throw new IllegalArgumentException("Handler of type '" + existing.supportedType.getSimpleName() + "' is already registered");
            }
        }

        handlers.add(new Handler(supportedType, handler));
    }
}
