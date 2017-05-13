package pl.mrugames.commons.router.request_handlers;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.*;
import pl.mrugames.commons.router.arg_resolvers.PathArgumentResolver;
import pl.mrugames.commons.router.arg_resolvers.RequestPayloadArgumentResolver;
import pl.mrugames.commons.router.arg_resolvers.SessionArgumentResolver;
import pl.mrugames.commons.router.sessions.Session;
import pl.mrugames.commons.router.sessions.SessionManager;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class ObjectRequestHandlerSpec {
    @Autowired
    private ObjectRequestHandler handler;

    @Autowired
    private Router router;

    @Autowired
    private PathArgumentResolver pathArgumentResolver;

    @Autowired
    private RequestPayloadArgumentResolver requestPayloadArgumentResolver;

    @Autowired
    private SessionArgumentResolver sessionArgumentResolver;

    @Autowired
    private SessionManager sessionManager;

    private PublishSubject<String> sourceSubject;
    private PublishSubject<Response> responseSubject;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
        sourceSubject = PublishSubject.create();
        responseSubject = PublishSubject.create();

        doReturn(mock(Session.class)).when(sessionManager).getSession(anyString(), anyString());
    }

    @After
    public void after() {
        reset(handler, router, pathArgumentResolver, sessionArgumentResolver, requestPayloadArgumentResolver, sessionManager);
        sourceSubject.onComplete();
        responseSubject.onComplete();
    }

    @Test
    public void givenHandleRequestIsCalled_thenDelegateToNext() throws Exception {
        Request request1 = new Request(1, "", "", "", RequestMethod.GET, Collections.emptyMap());
        Request request2 = new Request(2, "", "", "", RequestMethod.POST, Collections.emptyMap());
        Response response1 = new Response(1, ResponseStatus.OK, "something");
        Response response2 = new Response(2, ResponseStatus.OK, "something");

        doReturn(Observable.just(response1))
                .when(handler).next(request1);
        doReturn(Observable.just(response2))
                .when(handler).next(request2);

        Response real1 = handler.handleRequest(request1).blockingFirst();
        Response real2 = handler.handleRequest(request2).blockingFirst();

        verify(handler).next(request1);
        verify(handler).next(request2);

        assertThat(real1).isEqualTo(response1);
        assertThat(real2).isEqualTo(response2);
    }

    @Test
    public void givenNextMethodThrowsException_whenHandleRequest_thenReturnErrorResponse() throws Exception {
        Request request = new Request(100, "", "", "", RequestMethod.GET, Collections.emptyMap());
        doThrow(new Exception("test msg")).when(handler).next(request);

        Response response = handler.handleRequest(request).blockingFirst();

        doCallRealMethod().when(handler).next(any());

        assertThat(response.getId()).isEqualTo(request.getId());
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.INTERNAL_ERROR);
        assertThat((String) response.getPayload()).matches("Error: test msg, [\\S\\s]*");
    }

    @Test
    public void whenRequest_thenResponseWithSameId() throws Exception {
        Request request = new Request(100, "", "", "app/test/route1", RequestMethod.GET, Collections.emptyMap());
        Response response = handler.next(request).blockingFirst();

        assertThat(response.getId()).isEqualTo(request.getId());
    }

    @Test
    public void whenNextIsCalled_thenRouterIsRequestedToSearchForRoute() {
        Request request = new Request(100, "", "", "app/test/concat", RequestMethod.GET, Collections.emptyMap());
        handler.handleRequest(request);

        verify(router).findRoute("app/test/concat", RequestMethod.GET);
    }

    @Test
    public void givenArgumentResolversReturnDifferentInstances_whenRequest_thenNavigateWithThatInstances() throws InvocationTargetException, IllegalAccessException {
        Map<String, Object> pathArg = new HashMap<>();
        pathArg.put("1", "1");
        Map<String, Object> payloadArg = new HashMap<>();
        payloadArg.put("1", "1");
        Map<Class<?>, Optional<Object>> sessionArg = new HashMap<>();
        sessionArg.put(String.class, Optional.of("1"));

        doReturn(pathArg).when(pathArgumentResolver).resolve(any(), anyString(), any());
        doReturn(payloadArg).when(requestPayloadArgumentResolver).resolve(any(), any());
        doReturn(sessionArg).when(sessionArgumentResolver).resolve(any(), any());

        RouteInfo routeInfo = router.findRoute("app/test/concat", RequestMethod.GET);

        Request request = new Request(100, "", "", "app/test/concat", RequestMethod.GET, Collections.emptyMap());
        handler.handleRequest(request);

        verify(router).navigate(routeInfo, pathArg, payloadArg, sessionArg);
    }

    @Test
    public void whenRequest_thenPathResolverIsCalledWithProperPath() {
        RouteInfo routeInfo = router.findRoute("app/test/concat", RequestMethod.GET);

        Request request = new Request(100, "", "", "app/test/concat", RequestMethod.GET, Collections.emptyMap());
        handler.handleRequest(request);

        verify(pathArgumentResolver).resolve("GET:app/test/concat", routeInfo.getRoutePattern(), routeInfo.getParameters());
    }

    @Test
    public void givenRouterReturnsSomeObject_whenRequest_thenReturnResponseOk() throws InvocationTargetException, IllegalAccessException {
        Object someObject = new Object();
        doReturn(someObject).when(router).navigate(any(), anyMap(), anyMap(), anyMap());

        Request request = new Request(92, "", "", "app/test/route1", RequestMethod.GET, Collections.emptyMap());

        Response response = handler.handleRequest(request).blockingFirst();

        assertThat(response.getId()).isEqualTo(92);
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.OK);
        assertThat(response.getPayload()).isEqualTo(someObject);
    }

    @Test
    public void givenRouterReturnsMono_whenRequest_thenCopyMonoDataIntoResponse() {
        Stream.of(ResponseStatus.values()).forEach(status -> {
            Mono<String> returnedVal = Mono.of(status, "asdf");

            try {
                doReturn(returnedVal).when(router).navigate(any(), anyMap(), anyMap(), anyMap());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                fail();
            }

            Request request = new Request(92, "", "", "app/test/route1", RequestMethod.GET, Collections.emptyMap());

            Response response = handler.handleRequest(request).blockingFirst();

            assertThat(response.getId()).isEqualTo(92);
            assertThat(response.getStatus()).isEqualTo(status);
            assertThat(response.getPayload()).isEqualTo("asdf");
        });
    }

    @Test
    public void givenRouterReturnsSubject_whenItEmitsNextFrames_thenResponseHasStatusOfSTREAM() throws InvocationTargetException, IllegalAccessException {
        doReturn(sourceSubject).when(router).navigate(any(), anyMap(), anyMap(), anyMap());

        Request request = new Request(92, "", "", "app/test/route1", RequestMethod.GET, Collections.emptyMap());

        TestObserver<Response> testObserver = TestObserver.create();

        handler.handleRequest(request).subscribe(testObserver);

        sourceSubject.onNext("first");
        sourceSubject.onNext("second");
        sourceSubject.onNext("last");
        sourceSubject.onComplete();

        testObserver.awaitTerminalEvent();

        testObserver.assertValues(
                new Response(92, ResponseStatus.STREAM, "first"),
                new Response(92, ResponseStatus.STREAM, "second"),
                new Response(92, ResponseStatus.STREAM, "last"),
                new Response(92, ResponseStatus.CLOSE, null)
        );
    }

    @Test
    public void givenRouterReturnsSubject_whenRequest_thenRegisterEmitter() throws InvocationTargetException, IllegalAccessException {
        Session session = spy(new Session("", s -> {
        }));

        doReturn(sourceSubject).when(router).navigate(any(), anyMap(), anyMap(), anyMap());
        doReturn(session).when(sessionManager).getSession(anyString(), anyString());

        Request request = new Request(92, "", "", "app/test/route1", RequestMethod.GET, Collections.emptyMap());

        handler.handleRequest(request);
        verify(session).registerEmitter(92, sourceSubject);
    }


}
