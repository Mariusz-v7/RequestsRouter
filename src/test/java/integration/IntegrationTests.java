package integration;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.Subject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.Mono;
import pl.mrugames.commons.router.ResponseStatus;
import pl.mrugames.commons.router.client.Client;
import pl.mrugames.commons.router.client.ErrorResponseException;
import pl.mrugames.commons.router.client.ResponseHandle;
import pl.mrugames.commons.router.sessions.SessionManager;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        Cfg.class
})
public class IntegrationTests {
    @Autowired
    private Client client;

    @Autowired
    @Qualifier("integrationSubject")
    Subject<String> subject;

    @Autowired
    SessionManager sessionManager;

    @Before
    public void before() {
        sessionManager.createSession();
    }

    @Test
    public void shouldNavigateToProperRouteAndReturnProperResult() {
        TestObserver<Object> testObserver = client.send("integration", Collections.singletonMap("a", 1))
                .response.test();

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(true);
        testObserver.assertComplete();
    }

    @Test
    public void givenVoidRoute_whenReceive_thenNoValues() {
        TestObserver<Object> testObserver = client.send("integration/void")
                .response.test();

        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
        testObserver.assertComplete();
    }

    @Test
    public void whenRouteReturnsNull_thenReceiveMonoNoValue() {
        TestObserver<Object> testObserver = client.send("integration/null")
                .response.test();

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(Mono.NO_VAL);
        testObserver.assertComplete();
    }

    @Test
    public void whenRouteReturnsMonoVoid_thenReceiveNoValues() {
        TestObserver<Object> testObserver = client.send("integration/mono-void")
                .response.test();

        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
        testObserver.assertComplete();
    }

    @Test
    public void whenRouteReturnsMonoWithError_thenError() {
        TestObserver<Object> testObserver = client.send("integration/mono-error")
                .response.test();

        testObserver.awaitTerminalEvent();
        testObserver.assertError(new ErrorResponseException(ResponseStatus.ERROR, ""));
    }

    @Test
    public void whenRouteThrowsException_thenError() {
        TestObserver<Object> testObserver = client.send("integration/exception")
                .response.test();

        testObserver.awaitTerminalEvent();
        testObserver.assertError(new ErrorResponseException(ResponseStatus.BAD_REQUEST, "an exception!"));
    }

    @Test
    public void whenRouteReturnsMonoErrorWithMessage_thenErrorWithMessage() {
        TestObserver<Object> testObserver = client.send("integration/mono-error-custom")
                .response.test();

        testObserver.awaitTerminalEvent();
        testObserver.assertError(new ErrorResponseException(ResponseStatus.BAD_PARAMETERS, "bad params!"));
    }

    @Test
    public void whenObservable_thenReturnPayload() {
        TestObserver<Object> testObserver = client.send("integration/observable")
                .response.test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertValue("hello");
    }

    @Test
    public void whenObservableMultiple_thenReturnAll() {
        TestObserver<Object> testObserver = client.send("integration/observable-multi")
                .response.test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertValues("hello", "observable");
    }

    @Test
    public void whenObservableError_thenEmitError() {
        TestObserver<Object> testObserver = client.send("integration/observable-error")
                .response.test();

        testObserver.awaitTerminalEvent();
        testObserver.assertError(new ErrorResponseException(ResponseStatus.BAD_REQUEST, "what?"));
    }

    @Test
    public void whenObservableWithMonoVoid_thenReturnNoValues() {
        TestObserver<Object> testObserver = client.send("integration/observable-mono-void")
                .response.test();

        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
        testObserver.assertComplete();
    }

    @Test
    public void whenObservableMonoError_thenReturnError() {
        TestObserver<Object> testObserver = client.send("integration/observable-mono-error")
                .response.test();

        testObserver.awaitTerminalEvent();
        testObserver.assertError(new ErrorResponseException(ResponseStatus.ERROR, "error"));
    }

    @Test
    public void givenSubscribedToObservable_whenCloseStream_thenSendClose() {
        ResponseHandle<Object> responseHandle = client.send("integration/subj-observable");

        TestObserver<Object> testObserver = responseHandle.response.test();

        subject.onNext("a");
        subject.onNext("b");

        client.closeStream(responseHandle.id);

        subject.onNext("c");
        subject.onNext("d");

        testObserver.assertValues("a", "b");
        testObserver.assertValueCount(2);
        testObserver.assertComplete();
    }

    @Test
    public void givenMultipleSubscribersToObservable_whenCloseStream_thenSourceHasNoObservables() {
        assertThat(subject.hasObservers()).isFalse();

        ResponseHandle<Object> responseHandle1 = client.send("integration/subj-observable");
        ResponseHandle<Object> responseHandle2 = client.send("integration/subj-observable");
        ResponseHandle<Object> responseHandle3 = client.send("integration/subj-observable");
        ResponseHandle<Object> responseHandle4 = client.send("integration/subj-observable");

        assertThat(subject.hasObservers()).isTrue();

        client.closeStream(responseHandle1.id);
        client.closeStream(responseHandle2.id);
        client.closeStream(responseHandle3.id);
        client.closeStream(responseHandle4.id);

        assertThat(subject.hasObservers()).isFalse();
    }

    @Test
    public void givenParameterNotRequired_whenNavigate_thenInsertNull() {
        ResponseHandle<String> responseHandle = client.send("integration/required-false");
        TestObserver<String> testObserver = responseHandle.response.test();

        testObserver.assertValue("null");
        testObserver.assertNoErrors();
    }

    @Test
    public void givenParameterNotRequiredAndDefaultSet_whenNavigate_thenInsertDefault() {
        ResponseHandle<String> responseHandle = client.send("integration/required-false-with-default");
        TestObserver<String> testObserver = responseHandle.response.test();

        testObserver.assertValue("default");
        testObserver.assertNoErrors();
    }
}
