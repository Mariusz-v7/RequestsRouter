package pl.mrugames.commons.router.sessions;

import java.time.Instant;

public class Session {
    private volatile Instant lastAccessed;

    Session() {
        lastAccessed = Instant.now();
    }

    public Instant getLastAccessed() {
        return lastAccessed;
    }

    void updateLastAccessed(Instant instant) {
        lastAccessed = instant;
    }
}
