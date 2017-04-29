package pl.mrugames.commons.router.request_handlers;

import io.reactivex.Observable;
import org.springframework.stereotype.Service;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.ResponseStatus;
import pl.mrugames.commons.router.exceptions.ParameterNotFoundException;

@Service
class ExceptionHandler {
    public Observable<Response> handle(long requestId, Exception e) {

        if (e instanceof ParameterNotFoundException) {
            return Observable.just(new Response(requestId, ResponseStatus.BAD_REQUEST, e.getMessage()));
        }

        return Observable.just(new Response(requestId, ResponseStatus.INTERNAL_ERROR, String.format("Error: %s, %s", e.getMessage(), ErrorUtil.exceptionStackTraceToString(e))));
    }
}
