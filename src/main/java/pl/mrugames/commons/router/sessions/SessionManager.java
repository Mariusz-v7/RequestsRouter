package pl.mrugames.commons.router.sessions;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {
    private final Map<String, Session> sessions;

    public SessionManager() {
        sessions = new ConcurrentHashMap<>();
    }

    public Session getSession(String sessionId) {
        return sessions.computeIfAbsent(sessionId, this::createNewSession);
    }

    Collection<Session> getAllSessions() {
        return sessions.values();
    }

    boolean contains(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    private Session createNewSession(String sessionId) {
        return new Session();
    }
}
