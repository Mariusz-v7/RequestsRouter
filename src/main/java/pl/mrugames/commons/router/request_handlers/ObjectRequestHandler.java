package pl.mrugames.commons.router.request_handlers;

import io.reactivex.Observable;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.*;
import pl.mrugames.commons.router.sessions.SessionManager;

@Component
public class ObjectRequestHandler implements RequestHandler<Request, Response> {
    private final SessionManager sessionManager;
    private RequestProcessor requestRequestProcessor;
    private final Router router;

    ObjectRequestHandler(SessionManager sessionManager, RequestProcessor requestRequestProcessor, Router router) {
        this.sessionManager = sessionManager;
        this.requestRequestProcessor = requestRequestProcessor;
        this.router = router;
    }

    @Override
    public Observable<Response> handleRequest(Request request) {
        try {
            return next(request);
        } catch (Exception e) {
            return Observable.just(new Response(request.getId(), ResponseStatus.INTERNAL_ERROR, String.format("Error: %s, %s", e.getMessage(), ErrorUtil.exceptionStackTraceToString(e))));
        }
    }

    Observable<Response> next(Request request) throws Exception {
        switch (request.getRequestType()) {
            case STANDARD:
                RouteInfo routeInfo = router.findRoute(request.getRoute(), request.getRequestMethod());
                return requestRequestProcessor.standardRequest(routeInfo, request.getId(), request.getSession(), request.getRoute(), request.getRequestMethod(), request.getPayload());
            case CLOSE_STREAM:
                return requestRequestProcessor.closeStreamRequest(request.getId(), request.getSession());
            default:
                throw new IllegalStateException("Unknown request type: " + request.getRequestType());
        }
    }
}
