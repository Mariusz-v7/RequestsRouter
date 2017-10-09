package pl.mrugames.commons.router.request_handlers;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.*;
import pl.mrugames.commons.router.arg_resolvers.PathArgumentResolver;
import pl.mrugames.commons.router.arg_resolvers.RequestPayloadArgumentResolver;
import pl.mrugames.commons.router.arg_resolvers.SessionArgumentResolver;
import pl.mrugames.commons.router.sessions.Session;
import pl.mrugames.commons.router.sessions.SessionManager;

import java.lang.reflect.InvocationTargetException;

@Component
public class RequestProcessor {

    private final SessionManager sessionManager;
    private final Router router;
    private final PathArgumentResolver pathArgumentResolver;
    private final RequestPayloadArgumentResolver requestPayloadArgumentResolver;
    private final SessionArgumentResolver sessionArgumentResolver;
    private final ExceptionHandler exceptionHandler;

    private RequestProcessor(SessionManager sessionManager,
                             Router router,
                             PathArgumentResolver pathArgumentResolver,
                             RequestPayloadArgumentResolver requestPayloadArgumentResolver,
                             SessionArgumentResolver sessionArgumentResolver,
                             ExceptionHandler exceptionHandler) {
        this.sessionManager = sessionManager;
        this.router = router;
        this.pathArgumentResolver = pathArgumentResolver;
        this.requestPayloadArgumentResolver = requestPayloadArgumentResolver;
        this.sessionArgumentResolver = sessionArgumentResolver;
        this.exceptionHandler = exceptionHandler;
    }

    Observable<Response> closeStreamRequest(long requestId) {
        Session session = sessionManager.getSession();

        session.unregisterEmitter(requestId);
        session.unregisterSubscription(requestId);
        return Observable.empty();
    }

    Observable<Response> standardRequest(RouteInfo routeInfo,
                                         long requestId,
                                         String route,
                                         RequestMethod requestMethod,
                                         Object requestPayload) throws InvocationTargetException, IllegalAccessException {

        Session session = sessionManager.getSession();

        Object returnValue = router.navigate(routeInfo,
                pathArgumentResolver.resolve(requestMethod + ":" + route, routeInfo.getRoutePattern(), routeInfo.getParameters()),
                requestPayloadArgumentResolver.resolve(requestPayload, routeInfo.getParameters()),
                sessionArgumentResolver.resolve(session, routeInfo.getParameters())
        );

        if (returnValue instanceof Mono) {
            Mono<?> mono = (Mono) returnValue;

            Object payload = mono.getResponseStatus() == ResponseStatus.OK ? mono.getPayload() : mono.getError();

            return Observable.just(new Response(requestId, mono.getResponseStatus(), payload));
        }

        if (returnValue instanceof Subject) {
            try {
                session.registerEmitter(requestId, (Subject) returnValue);
            } catch (RuntimeException e) {
                ((Subject) returnValue).onComplete();
                throw e;
            }

            return onObservable((Subject<?>) returnValue, ReplaySubject.create(), requestId);
        }

        if (returnValue instanceof Observable) {
            Subject<Object> subject = ReplaySubject.create();
            Disposable disposable = ((Observable<?>) returnValue).subscribe(subject::onNext, subject::onError, subject::onComplete);

            try {
                session.registerEmitter(requestId, subject);
                session.registerSubscription(requestId, disposable);
            } catch (RuntimeException e) {
                subject.onComplete();
                disposable.dispose();
                throw e;
            }

            return onObservable(subject, ReplaySubject.create(), requestId);
        }

        return Observable.just(new Response(requestId, ResponseStatus.OK, returnValue));
    }

    Observable<Response> onObservable(Subject<?> sourceSubject, Subject<Response> responseSubject, long requestId) {
        sourceSubject.subscribe(
                next -> {
                    if (next instanceof Mono) {
                        Mono<?> mono = (Mono) next;
                        if (mono.getResponseStatus() == ResponseStatus.OK || mono.getResponseStatus() == ResponseStatus.STREAM) {
                            responseSubject.onNext(new Response(requestId, ResponseStatus.STREAM, mono.getPayload()));
                        } else {
                            responseSubject.onNext(new Response(requestId, mono.getResponseStatus(), mono.getError()));
                            responseSubject.onComplete();
                        }
                    } else {
                        responseSubject.onNext(new Response(requestId, ResponseStatus.STREAM, next));
                    }
                },
                error -> {
                    responseSubject.onNext(exceptionHandler.handle(requestId, error));
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
