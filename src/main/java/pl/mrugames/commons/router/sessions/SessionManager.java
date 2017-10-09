package pl.mrugames.commons.router.sessions;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
@EnableScheduling
public class SessionManager {
    private final Counter sessionCounter;

    SessionManager(MetricRegistry metricRegistry) {
        sessionCounter = metricRegistry.counter(MetricRegistry.name(SessionManager.class, "sessions_amount"));
    }

    public Session createSession() {
        Session session = new Session(sessionCounter::dec);
        Session.setLocalSession(session);
        sessionCounter.inc();
        return session;
    }

    public Session getSession(@Nullable String securityCode) {
        Session session = Session.getExistingLocalSession();
        if (session.getSecurityCode() != null && !session.getSecurityCode().equals(securityCode)) {
            throw new IllegalArgumentException("Wrong security code");
        }

        return session;
    }
}
