package pl.mrugames.commons.router.request_handlers;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.*;
import pl.mrugames.commons.router.arg_resolvers.PathArgumentResolver;
import pl.mrugames.commons.router.arg_resolvers.RequestPayloadArgumentResolver;
import pl.mrugames.commons.router.arg_resolvers.SessionArgumentResolver;
import pl.mrugames.commons.router.permissions.PermissionChecker;
import pl.mrugames.commons.router.sessions.Session;
import pl.mrugames.commons.router.sessions.SessionManager;

@Component
public class ObjectRequestHandler implements RequestHandler<Request, Response> {
    private final SessionManager sessionManager;
    private final Router router;
    private final PathArgumentResolver pathArgumentResolver;
    private final RequestPayloadArgumentResolver requestPayloadArgumentResolver;
    private final SessionArgumentResolver sessionArgumentResolver;
    private final PermissionChecker permissionChecker;

    ObjectRequestHandler(SessionManager sessionManager,
                         Router router,
                         PathArgumentResolver pathArgumentResolver,
                         RequestPayloadArgumentResolver requestPayloadArgumentResolver,
                         SessionArgumentResolver sessionArgumentResolver,
                         PermissionChecker permissionChecker) {
        this.sessionManager = sessionManager;
        this.router = router;
        this.pathArgumentResolver = pathArgumentResolver;
        this.requestPayloadArgumentResolver = requestPayloadArgumentResolver;
        this.sessionArgumentResolver = sessionArgumentResolver;
        this.permissionChecker = permissionChecker;
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
        Session session = sessionManager.getSession(request.getSession());

        switch (request.getRequestType()) {
            case STANDARD:
                RouteInfo routeInfo = router.findRoute(request.getRoute(), request.getRequestMethod());

                Mono<?> permissionStatus = permissionChecker.checkPermissions(session, routeInfo.getAccessType(), routeInfo.getAllowedRoles());
                if (permissionStatus.getResponseStatus() != ResponseStatus.OK) {
                    return Observable.just(new Response(request.getId(), permissionStatus.getResponseStatus(), permissionStatus.getPayload()));
                }

                Object returnValue = router.navigate(routeInfo,
                        pathArgumentResolver.resolve(request.getRequestMethod() + ":" + request.getRoute(), routeInfo.getRoutePattern(), routeInfo.getParameters()),
                        requestPayloadArgumentResolver.resolve(request.getPayload(), routeInfo.getParameters()),
                        sessionArgumentResolver.resolve(session, routeInfo.getParameters())
                );

                if (returnValue instanceof Mono) {
                    Mono<?> mono = (Mono) returnValue;
                    return Observable.just(new Response(request.getId(), mono.getResponseStatus(), mono.getPayload()));
                }

                if (returnValue instanceof Subject) {
                    session.registerEmitter(request.getId(), (Subject) returnValue);
                    return onSubject((Subject) returnValue, PublishSubject.create(), request.getId());
                }

                return Observable.just(new Response(request.getId(), ResponseStatus.OK, returnValue));
            default:
                throw new IllegalStateException("Unknown request type: " + request.getRequestType());
        }
    }

    Observable<Response> onSubject(Subject<?> sourceSubject, Subject<Response> responseSubject, long requestId) {
        sourceSubject.subscribe(
                next -> responseSubject.onNext(new Response(requestId, ResponseStatus.STREAM, next)),
                error -> {
                    responseSubject.onNext(new Response(requestId, ResponseStatus.CLOSE, error));
                    responseSubject.onComplete();
                },
                () -> {
                    responseSubject.onNext(new Response(requestId, ResponseStatus.CLOSE, null));
                    responseSubject.onComplete();
                }
        );

        return responseSubject.hide();
    }

}
