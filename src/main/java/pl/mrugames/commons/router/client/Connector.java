package pl.mrugames.commons.router.client;

import pl.mrugames.commons.router.RequestMethod;
import pl.mrugames.commons.router.RequestType;
import pl.mrugames.commons.router.Response;

import java.util.function.Consumer;

public interface Connector {
    boolean isRunning();

    void send(long id, String route, Object payload, RequestMethod requestMethod, RequestType requestType);

    void onResponseReceive(Consumer<Response> consumer);
}
