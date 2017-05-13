package pl.mrugames.commons.router.request_handlers;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.*;
import pl.mrugames.commons.router.arg_resolvers.PathArgumentResolver;
import pl.mrugames.commons.router.arg_resolvers.RequestPayloadArgumentResolver;
import pl.mrugames.commons.router.arg_resolvers.SessionArgumentResolver;
import pl.mrugames.commons.router.sessions.Session;
import pl.mrugames.commons.router.sessions.SessionManager;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@Component
public class RequestProcessor {

    private final SessionManager sessionManager;
    private final Router router;
    private final PathArgumentResolver pathArgumentResolver;
    private final RequestPayloadArgumentResolver requestPayloadArgumentResolver;
    private final SessionArgumentResolver sessionArgumentResolver;

    private RequestProcessor(SessionManager sessionManager,
                             Router router,
                             PathArgumentResolver pathArgumentResolver,
                             RequestPayloadArgumentResolver requestPayloadArgumentResolver,
                             SessionArgumentResolver sessionArgumentResolver) {
        this.sessionManager = sessionManager;
        this.router = router;
        this.pathArgumentResolver = pathArgumentResolver;
        this.requestPayloadArgumentResolver = requestPayloadArgumentResolver;
        this.sessionArgumentResolver = sessionArgumentResolver;
    }

    Observable<Response> closeStreamRequest(long requestId, String sessionId, String securityCode) {
        Session session = sessionManager.getSession(sessionId, securityCode);

        session.unregisterEmitter(requestId);
        return Observable.empty();
    }

    Observable<Response> standardRequest(RouteInfo routeInfo,
                                         long requestId,
                                         String sessionId,
                                         String securityCode,
                                         String route,
                                         RequestMethod requestMethod,
                                         Map<String, Object> requestPayload) throws InvocationTargetException, IllegalAccessException {

        Session session = sessionManager.getSession(sessionId, securityCode);

        Object returnValue = router.navigate(routeInfo,
                pathArgumentResolver.resolve(requestMethod + ":" + route, routeInfo.getRoutePattern(), routeInfo.getParameters()),
                requestPayloadArgumentResolver.resolve(requestPayload, routeInfo.getParameters()),
                sessionArgumentResolver.resolve(session, routeInfo.getParameters())
        );

        if (returnValue instanceof Mono) {
            Mono<?> mono = (Mono) returnValue;
            return Observable.just(new Response(requestId, mono.getResponseStatus(), mono.getPayload()));
        }

        if (returnValue instanceof Subject) {
            session.registerEmitter(requestId, (Subject) returnValue);
            return onSubject((Subject) returnValue, PublishSubject.create(), requestId);
        }

        return Observable.just(new Response(requestId, ResponseStatus.OK, returnValue));
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
