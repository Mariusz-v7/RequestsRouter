package pl.mrugames.commons.router.request_handlers;

import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.ResponseStatus;
import pl.mrugames.commons.router.exceptions.IncompatibleParameterException;
import pl.mrugames.commons.router.exceptions.ParameterNotFoundException;
import pl.mrugames.commons.router.exceptions.RouteConstraintViolationException;

@Service
class ExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    ExceptionHandler() {
    }

    Observable<Response> handle(long requestId, Exception e) {

        if (e instanceof ParameterNotFoundException || e instanceof IllegalArgumentException || e instanceof IncompatibleParameterException) {
            return Observable.just(new Response(requestId, ResponseStatus.BAD_REQUEST, e.getMessage()));
        }

        if (e instanceof RouteConstraintViolationException) {
            return Observable.just(new Response(requestId, ResponseStatus.BAD_PARAMETERS, ((RouteConstraintViolationException) e).getMessages()));
        }

        logger.error("Internal error while processing the request", e);
        return Observable.just(new Response(requestId, ResponseStatus.INTERNAL_ERROR, String.format("Error: %s, %s", e.getMessage(), ErrorUtil.exceptionStackTraceToString(e))));
    }
}
