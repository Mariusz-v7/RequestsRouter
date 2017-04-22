package pl.mrugames.commons.router.request_handlers;

import io.reactivex.Observable;

public interface RequestHandler<In, Out> {
    /**
     * @return response observable.
     * <p>
     * Returned value should never emit errors!
     * Method should never throw any exceptions!
     */
    Observable<Out> handleRequest(In request);
}
