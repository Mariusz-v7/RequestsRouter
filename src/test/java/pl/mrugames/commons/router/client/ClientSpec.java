package pl.mrugames.commons.router.client;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.Subject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import pl.mrugames.commons.router.RequestMethod;
import pl.mrugames.commons.router.RequestType;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.ResponseStatus;
import pl.mrugames.commons.router.sessions.Session;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(BlockJUnit4ClassRunner.class)
public class ClientSpec {
    private Client client;
    private Connector connector;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        connector = mock(Connector.class);

        doReturn(true).when(connector).isRunning();

        client = spy(new Client(1000, connector, new Session()));
    }

    @Test
    public void whenTimeout_thenObservableError() throws InterruptedException {
        TestObserver<Object> testObserver = new TestObserver<>();
        Observable<Object> observable = client.send("", "", RequestMethod.GET, 100).response;
        observable.subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
        testObserver.assertError(TimeoutException.class);
        testObserver.assertTerminated();
    }

    @Test
    public void whenSend_thenAddToBuffer() {
        assertThat(client.getBuffer()).isEmpty();
        client.send("");
        assertThat(client.getBuffer()).containsOnlyKeys(1L);
    }

    @Test
    public void whenTimeout_thenSubjectIsCompleted() throws InterruptedException {
        TestObserver<Object> testObserver = new TestObserver<>();
        client._send("", "", RequestMethod.GET, 100);

        Subject<?> subject = client.getBuffer().get(1L);

        subject.subscribe(testObserver);
        testObserver.awaitTerminalEvent();
        testObserver.assertError(TimeoutException.class);
        testObserver.assertTerminated();
    }

    @Test
    public void whenTimeout_thenRemoveFromBuffer() {
        TestObserver<Object> testObserver = new TestObserver<>();
        client._send("", "", RequestMethod.GET, 100).response.subscribe(testObserver);
        testObserver.awaitTerminalEvent();

        assertThat(client.getBuffer()).isEmpty();
    }

    @Test
    public void givenFrameSent_whenResponseWithSameIdComes_thenResolveObservable() {
        Object payload = "payload";
        Response response = new Response(1, ResponseStatus.OK, payload);

        TestObserver<Object> testObserver = new TestObserver<>();
        client.send("").response.subscribe(testObserver);

        client.onFrameReceive(response);

        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(payload);
        testObserver.assertTerminated();
    }

    @Test
    public void givenFrameSent_whenOkResponse_thenCompleteObservable() {
        Response response = new Response(1, ResponseStatus.OK, "payload");

        TestObserver<Object> testObserver = new TestObserver<>();
        client.send("").response.subscribe(testObserver);

        client.onFrameReceive(response);

        testObserver.awaitTerminalEvent();
        assertThat(client.getBuffer()).isEmpty();
    }

    @Test
    public void givenFrameSent_whenOkResponse_thenCompleteSubject() {
        Object payload = "payload";
        Response response = new Response(1, ResponseStatus.OK, payload);

        TestObserver<Object> testObserver = new TestObserver<>();
        client._send("", "", RequestMethod.GET, 100);

        client.getBuffer().get(1L).subscribe(testObserver);

        client.onFrameReceive(response);

        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(payload);
        testObserver.assertTerminated();
    }

    @Test
    public void givenFrameSent_whenStream_thenEmitAllAndClose() {
        Response response1 = new Response(1, ResponseStatus.STREAM, "1");
        Response response2 = new Response(1, ResponseStatus.STREAM, "2");
        Response response3 = new Response(1, ResponseStatus.STREAM, "3");
        Response response4 = new Response(1, ResponseStatus.STREAM, null);
        Response response5 = new Response(1, ResponseStatus.CLOSE, "4");

        TestObserver<Object> testObserver = new TestObserver<>();
        client._send("", "", RequestMethod.GET, 100).response.subscribe(testObserver);

        TestObserver<Object> testObserver2 = new TestObserver<>();
        client.getBuffer().get(1L).subscribe(testObserver2);

        client.onFrameReceive(response1);
        client.onFrameReceive(response2);
        client.onFrameReceive(response3);
        client.onFrameReceive(response4);
        client.onFrameReceive(response5);

        testObserver.awaitTerminalEvent();

        testObserver.assertValues("1", "2", "3");
        testObserver.assertTerminated();
        testObserver.assertComplete();
        testObserver.assertNoErrors();

        testObserver2.awaitTerminalEvent();
        testObserver2.assertTerminated();
        testObserver2.assertComplete();
    }

    @Test
    public void givenResponseWithNull_thenNoValuesAndComplete() {
        Response response = new Response(1, ResponseStatus.OK, null);
        TestObserver<Object> testObserver = new TestObserver<>();

        client.send("").response.subscribe(testObserver);
        client.onFrameReceive(response);

        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
        testObserver.assertComplete();
        testObserver.assertTerminated();
        testObserver.assertNoErrors();
    }

    @Test
    public void givenRequestSent_whenError_thenEmitErrorAndClear() {
        Response response = new Response(1, ResponseStatus.ERROR, "error");
        TestObserver<Object> testObserver = new TestObserver<>();
        TestObserver<Object> testObserver2 = new TestObserver<>();

        client.send("").response.subscribe(testObserver);

        client.getBuffer().get(1L).subscribe(testObserver2);

        client.onFrameReceive(response);

        testObserver.awaitTerminalEvent();
        testObserver.assertTerminated();
        testObserver.assertNoValues();
        testObserver.assertError(e -> Objects.equals(e.getMessage(), "error"));

        assertThat(client.getBuffer()).isEmpty();

        testObserver2.assertTerminated();
    }

    @Test
    public void whenSend_thenCallConnector() {
        client._send("", "", RequestMethod.GET, 100);
        verify(connector)
                .send(anyLong(), eq(""), eq(""), eq(RequestMethod.GET), eq(RequestType.STANDARD));
    }

    @Test
    public void whenUnknownFrame_thenJustLogMessage() {
        client.onFrameReceive(new Response(989, ResponseStatus.OK, "unknown"));
    }

    @Test
    public void whenCloseStream_thenSendProperFrame() {
        client.closeStream(2);
        verify(connector).send(eq(2L), eq(null), eq(null), eq(null), eq(RequestType.CLOSE_STREAM));
    }

    @Test
    public void givenStream_whenClose_thenDoNotOverwriteBuffer() {
        client.send("");

        Subject<?> subject1 = client.getBuffer().get(1L);

        client.closeStream(1L);

        Subject<?> subject2 = client.getBuffer().get(1L);

        assertThat(subject1).isSameAs(subject2);
    }
}
