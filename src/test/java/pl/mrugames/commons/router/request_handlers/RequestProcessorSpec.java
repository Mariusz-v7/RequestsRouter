package pl.mrugames.commons.router.request_handlers;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

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

    private PublishSubject<String> sourceSubject;
    private PublishSubject<Response> responseSubject;

    @Before
    public void before() {
        sourceSubject = PublishSubject.create();
        responseSubject = PublishSubject.create();

        doReturn(mock(Session.class)).when(sessionManager).getSession(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenEmitterRegistered_whenRequestWithTypeOfCLOSE_STREAM_thenShutdownEmitter() throws InvocationTargetException, IllegalAccessException {
        doReturn(new Session("", mock(Consumer.class))).when(sessionManager).getSession(anyString());

        TestObserver<Response> testObserver = TestObserver.create();
        doReturn(sourceSubject).when(router).navigate(any(), anyMap(), anyMap(), anyMap());

        Request request = new Request(904, "", "app/test/route1", RequestMethod.GET, Collections.emptyMap());
        requestProcessor.standardRequest(request.getId(), request.getSession(), request.getRoute(), request.getRequestMethod(), request.getPayload()).subscribe(testObserver);

        Request closeRequest = new Request(904, "", null, null, null, RequestType.CLOSE_STREAM);

        TestObserver<Response> closeObserver = TestObserver.create();
        requestProcessor.closeStreamRequest(closeRequest.getId(), closeRequest.getSession()).subscribe(closeObserver);

        testObserver.assertValues(new Response(904, ResponseStatus.CLOSE, null));
        testObserver.assertComplete();

        closeObserver.assertValue(new Response(904, ResponseStatus.CLOSE, null));
        closeObserver.assertComplete();
    }

    @Test
    public void givenOnSubjectIsCalled_whenSourceSubjectIsClosed_thenResponseSubjectEmitsCloseFrameAndCloseItself() {
        TestObserver<Response> responseObserver = TestObserver.create();
        TestObserver<Response> subjectObserver = TestObserver.create();

        Observable<Response> observable = requestProcessor.onSubject(sourceSubject, responseSubject, 99);

        observable.subscribe(responseObserver);
        responseSubject.subscribe(subjectObserver);

        sourceSubject.onComplete();

        responseObserver.assertValue(new Response(99, ResponseStatus.CLOSE, null));
        subjectObserver.assertValue(new Response(99, ResponseStatus.CLOSE, null));

        responseObserver.assertComplete();
        subjectObserver.assertComplete();
    }

    @Test
    public void givenSourceSubjectEmitsError_thenResponseSubjectEmitsClose() {
        TestObserver<Response> responseObserver = TestObserver.create();
        TestObserver<Response> subjectObserver = TestObserver.create();
        TestObserver<String> sourceSubjectObserver = TestObserver.create();

        Observable<Response> observable = requestProcessor.onSubject(sourceSubject, responseSubject, 99);

        sourceSubject.subscribe(sourceSubjectObserver);
        observable.subscribe(responseObserver);
        responseSubject.subscribe(subjectObserver);

        RuntimeException rte = new RuntimeException("bla");
        sourceSubject.onError(rte);

        responseObserver.assertValue(new Response(99, ResponseStatus.CLOSE, rte));
        subjectObserver.assertValue(new Response(99, ResponseStatus.CLOSE, rte));

        responseObserver.assertComplete();
        subjectObserver.assertComplete();
        sourceSubjectObserver.assertTerminated();
    }

}
