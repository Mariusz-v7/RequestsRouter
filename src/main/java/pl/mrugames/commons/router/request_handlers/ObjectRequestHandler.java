package pl.mrugames.commons.router.request_handlers;

import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.Request;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.RouteInfo;
import pl.mrugames.commons.router.Router;

@Component
public class ObjectRequestHandler implements RequestHandler<Request, Response> {
    private RequestProcessor requestProcessor;
    private final Router router;
    private final ExceptionHandler exceptionHandler;

    ObjectRequestHandler(RequestProcessor requestProcessor, Router router, ExceptionHandler exceptionHandler) {
        this.requestProcessor = requestProcessor;
        this.router = router;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public RouterResult<Response> handleRequest(Request request) {
        try {
            return next(request);
        } catch (Exception e) {
            return new RouterResult<>(null, exceptionHandler.handle(request.getId(), e));
        }
    }

    RouterResult<Response> next(Request request) throws Exception {
        switch (request.getRequestType()) {
            case STANDARD:
                RouteInfo routeInfo = router.findRoute(request.getRoute(), request.getRequestMethod());
                return requestProcessor.standardRequest(routeInfo, request.getId(), request.getSession(), request.getRoute(), request.getRequestMethod(), request.getPayload());
            case CLOSE_STREAM:
                return requestProcessor.closeStreamRequest(request.getId(), request.getSession());
            default:
                throw new IllegalStateException("Unknown request type: " + request.getRequestType());
        }
    }
}
