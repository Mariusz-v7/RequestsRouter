package pl.mrugames.commons.router.sessions;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.RouterProperties;

import javax.annotation.Nullable;
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

    SessionManager(@Value("${" + RouterProperties.SESSION_EXPIRE_TIME + "}") long sessionExpireTimeMillis,
                   MetricRegistry metricRegistry) {
        this.sessionExpireTimeMillis = sessionExpireTimeMillis;
        sessions = new ConcurrentHashMap<>();

        metricRegistry.register(MetricRegistry.name(SessionManager.class, "sessions_amount"), (Gauge) sessions::size);
    }

    public Session getSession(String sessionId, @Nullable String securityCode) {
        if (sessionId == null || sessionId.length() < SESSION_ID_MIN_LENGTH) {
            throw new IllegalArgumentException("Session id must be at least " + SESSION_ID_MIN_LENGTH + " characters long");
        }

        Session session = sessions.compute(sessionId, this::compute);
        if (session.getSecurityCode() != null && !session.getSecurityCode().equals(securityCode)) {
            throw new IllegalArgumentException("Wrong security code");
        }

        return session;
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
            Session session = new Session(sessionId, this::remove);
            Session.setLocalSession(session);
            return session;
        } else {
            current.updateLastAccessed(Instant.now());
            Session.setLocalSession(current);
            return current;
        }
    }
}
