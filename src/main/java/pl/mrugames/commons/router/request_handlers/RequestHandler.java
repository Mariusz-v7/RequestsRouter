package pl.mrugames.commons.router.request_handlers;

import java.io.Serializable;

public interface RequestHandler<In, Out extends Serializable> {
    /**
     * @return response observable.
     * <p>
     * Returned value should never emit errors!
     * Method should never throw any exceptions!
     */
    RouterResult<Out> handleRequest(In request);
}
