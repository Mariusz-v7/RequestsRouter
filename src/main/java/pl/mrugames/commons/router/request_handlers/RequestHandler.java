package pl.mrugames.commons.router.request_handlers;

public interface RequestHandler<In, Out> {
    Out handleRequest(In request);
}
