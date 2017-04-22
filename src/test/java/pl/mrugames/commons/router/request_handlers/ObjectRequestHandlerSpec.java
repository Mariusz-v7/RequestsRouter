package pl.mrugames.commons.router.request_handlers;

import org.junit.After;
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
import pl.mrugames.commons.router.permissions.AccessType;
import pl.mrugames.commons.router.permissions.RoleHolder;
import pl.mrugames.commons.router.sessions.Session;

import java.util.*;

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

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @After
    public void after() {
        reset(handler, router, pathArgumentResolver, sessionArgumentResolver, requestPayloadArgumentResolver);
    }

    private String generateString(int len) {
        String allowed = "abcdefgh";
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        while (len-- > 0) {
            stringBuilder.append(allowed.charAt(random.nextInt(allowed.length())));
        }

        return stringBuilder.toString();
    }

    @Test
    public void givenHandleRequestIsCalled_thenDelegateToNext() throws Exception {
        Request request1 = new Request(1, generateString(ObjectRequestHandler.SESSION_ID_MIN_LENGTH), "", RequestMethod.GET, Collections.emptyMap());
        Request request2 = new Request(2, generateString(ObjectRequestHandler.SESSION_ID_MIN_LENGTH), "", RequestMethod.POST, Collections.emptyMap());
        Response response1 = new Response(1, ResponseStatus.OK, "something");
        Response response2 = new Response(2, ResponseStatus.OK, "something");

        doReturn(response1).when(handler).next(request1);
        doReturn(response2).when(handler).next(request2);

        Response real1 = handler.handleRequest(request1);
        Response real2 = handler.handleRequest(request2);

        verify(handler).next(request1);
        verify(handler).next(request2);

        assertThat(real1).isEqualTo(response1);
        assertThat(real2).isEqualTo(response2);
    }

    @Test
    public void givenNextMethodThrowsException_whenHandleRequest_thenReturnErrorResponse() throws Exception {
        Request request = new Request(100, generateString(ObjectRequestHandler.SESSION_ID_MIN_LENGTH), "", RequestMethod.GET, Collections.emptyMap());
        doThrow(new Exception("test msg")).when(handler).next(request);

        Response response = handler.handleRequest(request);

        doCallRealMethod().when(handler).next(any());

        assertThat(response.getId()).isEqualTo(request.getId());
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.INTERNAL_ERROR);
        assertThat((String) response.getPayload()).matches("Error: test msg, [\\S\\s]*");
    }

    @Test
    public void whenRequest_thenResponseWithSameId() throws Exception {
        Request request = new Request(100, generateString(ObjectRequestHandler.SESSION_ID_MIN_LENGTH), "app/test/route1", RequestMethod.GET, Collections.emptyMap());
        Response response = handler.next(request);

        assertThat(response.getId()).isEqualTo(request.getId());
    }

    @Test
    public void givenRequestWithSessionIdLessThan64chars_thenException() throws Exception {
        Request request = new Request(100, generateString(ObjectRequestHandler.SESSION_ID_MIN_LENGTH - 1), "", RequestMethod.GET, Collections.emptyMap());

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Session id must be at least " + ObjectRequestHandler.SESSION_ID_MIN_LENGTH + " characters long");

        handler.next(request);
    }

    @Test
    public void whenNextIsCalled_thenRouterIsRequestedToSearchForRoute() {
        Request request = new Request(100, generateString(ObjectRequestHandler.SESSION_ID_MIN_LENGTH), "app/test/concat", RequestMethod.GET, Collections.emptyMap());
        handler.handleRequest(request);

        verify(router).findRoute("app/test/concat", RequestMethod.GET);
    }

    @Test
    public void givenArgumentResolversReturnDifferentInstances_whenRequest_thenNavigateWithThatInstances() {
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

        Request request = new Request(100, generateString(ObjectRequestHandler.SESSION_ID_MIN_LENGTH), "app/test/concat", RequestMethod.GET, Collections.emptyMap());
        handler.handleRequest(request);

        verify(router).navigate(routeInfo, pathArg, payloadArg, sessionArg);
    }

    @Test
    public void whenRequest_thenPathResolverIsCalledWithProperPath() {
        RouteInfo routeInfo = router.findRoute("app/test/concat", RequestMethod.GET);

        Request request = new Request(100, generateString(ObjectRequestHandler.SESSION_ID_MIN_LENGTH), "app/test/concat", RequestMethod.GET, Collections.emptyMap());
        handler.handleRequest(request);

        verify(pathArgumentResolver).resolve("GET:app/test/concat", routeInfo.getRoutePattern(), routeInfo.getParameters());
    }

    @Test
    public void givenSessionHasNoUserLogged_andRouteRequiresLoggedUsers_whenCheckPermissions_thenDeny() {
        RouteInfo routeInfo = new RouteInfo(null, null, null, null, AccessType.ONLY_LOGGED_IN, Collections.emptyList());
        Session session = new Session("mock", s -> {
        });

        ResponseStatus status = handler.checkPermissions(session, routeInfo).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.NOT_AUTHORIZED);
    }

    @Test
    public void givenSessionHasUserLogged_andRouteRequiresIt_whenCheckPermissions_thenOk() {
        RouteInfo routeInfo = new RouteInfo(null, null, null, null, AccessType.ONLY_LOGGED_IN, Collections.emptyList());
        Session session = new Session("mock", s -> {
        });
        session.add(RoleHolder.class, mock(RoleHolder.class));

        ResponseStatus status = handler.checkPermissions(session, routeInfo).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.OK);
    }

    @Test
    public void givenSessionHasNoUserLogged_andRouteRequiresNoLogged_whenCheckPermissions_thenOk() {
        RouteInfo routeInfo = new RouteInfo(null, null, null, null, AccessType.ONLY_NOT_LOGGED_IN, Collections.emptyList());
        Session session = new Session("mock", s -> {
        });

        ResponseStatus status = handler.checkPermissions(session, routeInfo).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.OK);
    }

    @Test
    public void givenSessionHasUserLogged_andRouteRequiresNoLogged_thenDeny() {
        RouteInfo routeInfo = new RouteInfo(null, null, null, null, AccessType.ONLY_NOT_LOGGED_IN, Collections.emptyList());
        Session session = new Session("mock", s -> {
        });
        session.add(RoleHolder.class, mock(RoleHolder.class));

        ResponseStatus status = handler.checkPermissions(session, routeInfo).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.ONLY_FOR_NOT_AUTHORIZED);
    }

    @Test
    public void givenSessionHasNoUserLogged_andRouteRequiresRoles_thenDeny() {
        RouteInfo routeInfo = new RouteInfo(null, null, null, null, AccessType.ONLY_WITH_SPECIFIC_ROLES, Collections.emptyList());
        Session session = new Session("mock", s -> {
        });

        ResponseStatus status = handler.checkPermissions(session, routeInfo).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.PERMISSION_DENIED);
    }

    @Test
    public void givenSessionHasUserWithoutPermissions_thenDeny() {
        RouteInfo routeInfo = new RouteInfo(null, null, null, null, AccessType.ONLY_WITH_SPECIFIC_ROLES, Collections.singletonList("admin"));
        Session session = new Session("mock", s -> {
        });
        RoleHolder roleHolder = mock(RoleHolder.class);
        doReturn(Collections.singletonList("user")).when(roleHolder).getRoles();
        session.add(RoleHolder.class, roleHolder);

        ResponseStatus status = handler.checkPermissions(session, routeInfo).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.PERMISSION_DENIED);
    }

    @Test
    public void givenSessionHasUserWithPermissions_thenOk() {
        RouteInfo routeInfo = new RouteInfo(null, null, null, null, AccessType.ONLY_WITH_SPECIFIC_ROLES, Collections.singletonList("admin"));
        Session session = new Session("mock", s -> {
        });
        RoleHolder roleHolder = mock(RoleHolder.class);
        doReturn(Collections.singletonList("admin")).when(roleHolder).getRoles();
        session.add(RoleHolder.class, roleHolder);

        ResponseStatus status = handler.checkPermissions(session, routeInfo).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.OK);
    }

    @Test
    public void givenSessionHasNoUser_andRouteHasAllAllowed_thenOk() {
        RouteInfo routeInfo = new RouteInfo(null, null, null, null, AccessType.ALL_ALLOWED, Collections.emptyList());
        Session session = new Session("mock", s -> {
        });

        ResponseStatus status = handler.checkPermissions(session, routeInfo).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.OK);
    }

    @Test
    public void givenCheckPermissionsReturnStatusOtherThanOk_whenRequest_thenResponseWithReturnedStatus() {
        doReturn(Mono.of(ResponseStatus.PERMISSION_DENIED, "xxx")).when(handler).checkPermissions(any(), any());
        Request request = new Request(90, generateString(ObjectRequestHandler.SESSION_ID_MIN_LENGTH), "app/test/route1", RequestMethod.GET, Collections.emptyMap());

        Response response = handler.handleRequest(request);

        assertThat(response.getId()).isEqualTo(90);
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.PERMISSION_DENIED);
        assertThat(response.getPayload()).isEqualTo("xxx");
    }

}
