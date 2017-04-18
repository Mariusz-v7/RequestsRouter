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
    private final Map<String, Session> sessions;
    private final long sessionExpireTimeMillis;

    public SessionManager(@Value("${" + RouterProperties.SESSION_EXPIRE_TIME + "}") long sessionExpireTimeMillis) {
        this.sessionExpireTimeMillis = sessionExpireTimeMillis;
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

    void remove(Session session) {
        sessions.remove(session.getId());
    }

    @Scheduled(fixedDelayString = "${" + RouterProperties.SESSION_EXPIRE_TIME + "}")
    private void cleaner() {
        Instant expireTime = Instant.now().minus(sessionExpireTimeMillis, ChronoUnit.MILLIS);

        sessions.entrySet().stream()
                .filter(e -> e.getValue().getLastAccessed().isBefore(expireTime))
                .forEach(e -> sessions.remove(e.getKey()));
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
