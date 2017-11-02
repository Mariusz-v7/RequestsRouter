package pl.mrugames.commons.router.sessions;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class SessionManager {
    private final Counter sessionCounter;

    SessionManager(MetricRegistry metricRegistry) {
        sessionCounter = metricRegistry.counter(MetricRegistry.name(SessionManager.class, "sessions_amount"));
    }

    public synchronized Session createSession() {
        Session session = new Session();
        Session.setLocalSession(session);
        sessionCounter.inc();
        return session;
    }

    public synchronized Session getSession() {
        return Session.getExistingLocalSession();
    }

    public synchronized void destroySession() {
        Session.destroyLocalSession();
        sessionCounter.dec();
    }

    /**
     * If session needs to be reused! Does not remove the session from memory!
     * Call destroySession() when you want to finish!
     */
    public synchronized void clearSession() {
        Session.clearLocalSession();
    }
}
