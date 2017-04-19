package pl.mrugames.commons.router.request_handlers;

import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.Request;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.sessions.SessionManager;

@Component
public class ObjectRequestHandler implements RequestHandler<Request, Response> {
    public final static int SESSION_ID_MIN_LENGTH = 64;

    private final SessionManager sessionManager;

    ObjectRequestHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
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
            throw new IllegalArgumentException("Session id must at least " + ObjectRequestHandler.SESSION_ID_MIN_LENGTH + " characters long");
        }

        return new Response(request.getId(), null, null);
    }
}
