package pl.mrugames.commons.router.request_handlers;

import io.reactivex.Observable;
import org.springframework.stereotype.Service;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.ResponseStatus;
import pl.mrugames.commons.router.exceptions.IncompatibleParameterException;
import pl.mrugames.commons.router.exceptions.ParameterNotFoundException;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@Service
class ExceptionHandler {

    ExceptionHandler() {
    }

    public Observable<Response> handle(long requestId, Exception e) {

        if (e instanceof ParameterNotFoundException || e instanceof IllegalArgumentException || e instanceof IncompatibleParameterException) {
            return Observable.just(new Response(requestId, ResponseStatus.BAD_REQUEST, e.getMessage()));
        }

        if (e instanceof ConstraintViolationException) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("message", e.getMessage());
            payload.put("violations", ((ConstraintViolationException) e).getConstraintViolations());
            return Observable.just(new Response(requestId, ResponseStatus.BAD_PARAMETERS, payload));
        }

        return Observable.just(new Response(requestId, ResponseStatus.INTERNAL_ERROR, String.format("Error: %s, %s", e.getMessage(), ErrorUtil.exceptionStackTraceToString(e))));
    }
}
