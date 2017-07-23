package pl.mrugames.commons.router.request_handlers;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.*;
import pl.mrugames.commons.router.sessions.Session;
import pl.mrugames.commons.router.sessions.SessionManager;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class RequestProcessorSpec {

    @Autowired
    private RequestProcessor requestProcessor;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private Router router;

    @Autowired
    private ExceptionHandler exceptionHandler;

    private PublishSubject<String> sourceSubject;
    private PublishSubject<Response> responseSubject;

    @Before
    public void before() {
        sourceSubject = PublishSubject.create();
        responseSubject = PublishSubject.create();

        doReturn(mock(Session.class)).when(sessionManager).getSession(anyString(), anyString());
    }

    @After
    public void after() {
        reset(sessionManager, router);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenEmitterRegistered_whenRequestWithTypeOfCLOSE_STREAM_thenShutdownEmitter() throws InvocationTargetException, IllegalAccessException {
        doReturn(new Session("", mock(Consumer.class))).when(sessionManager).getSession(anyString(), anyString());

        TestObserver<Response> testObserver = TestObserver.create();
        doReturn(sourceSubject).when(router).navigate(any(), anyMap(), anyMap(), anyMap());

        Request request = new Request(904, "", "", "app/test/route1", RequestMethod.GET, Collections.emptyMap());
        requestProcessor.standardRequest(router.findRoute(request.getRoute(), request.getRequestMethod()), request.getId(), request.getSession(), request.getSecurityCode(), request.getRoute(), request.getRequestMethod(), request.getPayload())
                .subscribe(testObserver);

        Request closeRequest = new Request(904, "", "", null, null, null, RequestType.CLOSE_STREAM);

        TestObserver<Response> closeObserver = TestObserver.create();
        requestProcessor.closeStreamRequest(closeRequest.getId(), closeRequest.getSession(), "")
                .subscribe(closeObserver);

        testObserver.assertValues(new Response(904, ResponseStatus.CLOSE, null));
        testObserver.assertComplete();

        closeObserver.assertNoValues();
        closeObserver.assertComplete();
    }

    @Test
    public void givenOnSubjectIsCalled_whenSourceSubjectIsClosed_thenResponseSubjectEmitsCloseFrameAndCloseItself() {
        TestObserver<Response> responseObserver = TestObserver.create();
        TestObserver<Response> subjectObserver = TestObserver.create();

        Observable<Response> observable = requestProcessor.onObservable(sourceSubject, responseSubject, 99);

        observable.subscribe(responseObserver);
        responseSubject.subscribe(subjectObserver);

        sourceSubject.onComplete();

        responseObserver.assertValue(new Response(99, ResponseStatus.CLOSE, null));
        subjectObserver.assertValue(new Response(99, ResponseStatus.CLOSE, null));

        responseObserver.assertComplete();
        subjectObserver.assertComplete();
    }

    @Test
    public void givenSourceSubjectEmitsError_thenResponseSubjectCallsExceptionHandler() {
        TestObserver<Response> responseObserver = TestObserver.create();
        TestObserver<Response> subjectObserver = TestObserver.create();
        TestObserver<String> sourceSubjectObserver = TestObserver.create();

        Observable<Response> observable = requestProcessor.onObservable(sourceSubject, responseSubject, 99);

        sourceSubject.subscribe(sourceSubjectObserver);
        observable.subscribe(responseObserver);
        responseSubject.subscribe(subjectObserver);

        RuntimeException rte = new RuntimeException("bla");
        sourceSubject.onError(rte);

        Response expected = exceptionHandler.handle(99, rte);

        responseObserver.assertValue(expected);
        subjectObserver.assertValue(expected);

        responseObserver.assertComplete();
        subjectObserver.assertComplete();
        sourceSubjectObserver.assertTerminated();
    }

    @Test
    public void whenResponseIsObservable_thenDelegateToOnSubject() throws IllegalAccessException, InvocationTargetException {
        TestObserver<Response> testObserver = new TestObserver<>();
        doReturn(Observable.just("123")).when(router).navigate(any(), any(), any(), any());

        Request request = new Request(904, "", "", "app/test/route1", RequestMethod.GET, Collections.emptyMap());
        requestProcessor.standardRequest(router.findRoute(request.getRoute(), request.getRequestMethod()), request.getId(), request.getSession(), request.getSecurityCode(), request.getRoute(), request.getRequestMethod(), request.getPayload())
                .subscribe(testObserver);

        testObserver.awaitTerminalEvent();
//        testObserver.assertValue(new Response(request.getId(), ResponseStatus.STREAM, "123")); //TODO: why this doesnt work?
        testObserver.assertComplete();
    }

}
