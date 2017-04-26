package pl.mrugames.commons.router.sessions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.RouterProperties;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@EnableScheduling
public class SessionManager {
    public final static int SESSION_ID_MIN_LENGTH = 64;

    private final Map<String, Session> sessions;
    private final long sessionExpireTimeMillis;

    SessionManager(@Value("${" + RouterProperties.SESSION_EXPIRE_TIME + "}") long sessionExpireTimeMillis) {
        this.sessionExpireTimeMillis = sessionExpireTimeMillis;
        sessions = new ConcurrentHashMap<>();
    }

    public Session getSession(String sessionId) {
        if (sessionId.length() < SESSION_ID_MIN_LENGTH) {
            throw new IllegalArgumentException("Session id must be at least " + SESSION_ID_MIN_LENGTH + " characters long");
        }

        return sessions.compute(sessionId, this::compute);
    }

    Collection<Session> getAllSessions() {
        return sessions.values();
    }

    boolean contains(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    void remove(Session session) {
        Session s = sessions.remove(session.getId());
        if (s != null) {
            s.destroy();
        }
    }

    @Scheduled(fixedDelayString = "${" + RouterProperties.SESSION_EXPIRE_TIME + "}")
    private void cleaner() {
        Instant expireTime = Instant.now().minus(sessionExpireTimeMillis, ChronoUnit.MILLIS);

        sessions.entrySet().stream()
                .filter(e -> e.getValue().getLastAccessed().isBefore(expireTime))
                .map(Map.Entry::getValue)
                .forEach(this::remove);
    }

    private Session compute(String sessionId, Session current) {
        if (current == null) {
            return new Session(sessionId, this::remove);
        } else {
            current.updateLastAccessed(Instant.now());
            return current;
        }
    }
}
