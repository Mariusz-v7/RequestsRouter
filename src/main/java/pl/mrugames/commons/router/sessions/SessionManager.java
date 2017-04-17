package pl.mrugames.commons.router.sessions;

import org.springframework.stereotype.Component;

import java.time.Instant;
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
        return sessions.compute(sessionId, this::compute);
    }

    Collection<Session> getAllSessions() {
        return sessions.values();
    }

    boolean contains(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    private Session compute(String sessionId, Session current) {
        if (current == null) {
            return new Session();
        } else {
            current.updateLastAccessed(Instant.now());
            return current;
        }
    }
}
