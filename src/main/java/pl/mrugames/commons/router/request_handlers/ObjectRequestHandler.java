package pl.mrugames.commons.router.request_handlers;

import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.Request;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.RouteInfo;
import pl.mrugames.commons.router.Router;
import pl.mrugames.commons.router.arg_resolvers.PathArgumentResolver;
import pl.mrugames.commons.router.arg_resolvers.RequestPayloadArgumentResolver;
import pl.mrugames.commons.router.arg_resolvers.SessionArgumentResolver;
import pl.mrugames.commons.router.permissions.RoleHolder;
import pl.mrugames.commons.router.sessions.Session;
import pl.mrugames.commons.router.sessions.SessionManager;

import java.util.List;
import java.util.Optional;

@Component
public class ObjectRequestHandler implements RequestHandler<Request, Response> {
    public final static int SESSION_ID_MIN_LENGTH = 64;

    private final SessionManager sessionManager;
    private final Router router;
    private final PathArgumentResolver pathArgumentResolver;
    private final RequestPayloadArgumentResolver requestPayloadArgumentResolver;
    private final SessionArgumentResolver sessionArgumentResolver;

    ObjectRequestHandler(SessionManager sessionManager,
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

    @Override
    public Response handleRequest(Request request) {
        try {
            return next(request);
        } catch (Exception e) {
            return new Response(request.getId(), Response.Status.INTERNAL_ERROR, String.format("Error: %s, %s", e.getMessage(), ErrorUtil.exceptionStackTraceToString(e)));
        }
    }

    Response next(Request request) throws Exception {
        if (request.getSession().length() < SESSION_ID_MIN_LENGTH) {
            throw new IllegalArgumentException("Session id must be at least " + ObjectRequestHandler.SESSION_ID_MIN_LENGTH + " characters long");
        }

        Session session = sessionManager.getSession(request.getSession());

        RouteInfo routeInfo = router.findRoute(request.getRoute(), request.getRequestMethod());

        Object returnValue = router.navigate(routeInfo,
                pathArgumentResolver.resolve(request.getRequestMethod() + ":" + request.getRoute(), routeInfo.getRoutePattern(), routeInfo.getParameters()),
                requestPayloadArgumentResolver.resolve(request.getPayload(), routeInfo.getParameters()),
                sessionArgumentResolver.resolve(session, routeInfo.getParameters())
        );

        //todo: check instanceof and act appropriately

        return new Response(request.getId(), null, null);
    }

    Response.Status checkPermissions(Session session, RouteInfo routeInfo) {
        switch (routeInfo.getAccessType()) {
            case ONLY_LOGGED_IN:
                return session.get(RoleHolder.class).isPresent() ? Response.Status.OK : Response.Status.NOT_AUTHORIZED;
            case ONLY_NOT_LOGGED_IN:
                return session.get(RoleHolder.class).isPresent() ? Response.Status.ONLY_FOR_NOT_AUTHORIZED : Response.Status.OK;
            case ONLY_WITH_SPECIFIC_ROLES:
                Optional<RoleHolder> roleHolder = session.get(RoleHolder.class);
                if (roleHolder.isPresent()) {
                    List<String> roles = roleHolder.get().getRoles();
                    for (String role : roles) {
                        if (routeInfo.getAllowedRoles().contains(role)) {
                            return Response.Status.OK;
                        }
                    }
                }
                return Response.Status.PERMISSION_DENIED;
            case ALL_ALLOWED:
                return Response.Status.OK;
            default:
                return Response.Status.INTERNAL_ERROR;
        }
    }
}
