package pl.mrugames.commons.router.request_handlers;

import io.reactivex.Observable;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.ResponseStatus;
import pl.mrugames.commons.router.sessions.Session;
import pl.mrugames.commons.router.sessions.SessionManager;

@Component
public class Handler {  // TODO: rename
    private final SessionManager sessionManager;

    private Handler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    Observable<Response> closeStreamRequest(long requestId, String sessionId) {
        Session session = sessionManager.getSession(sessionId);

        session.unregisterEmitter(requestId);
        return Observable.just(new Response(requestId, ResponseStatus.CLOSE, null));
    }

    Observable<Response> standardRequest(String sessionId) {
        Session session = sessionManager.getSession(sessionId);

        return null;
    }
}
