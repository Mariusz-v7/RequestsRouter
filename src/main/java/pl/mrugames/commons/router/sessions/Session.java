package pl.mrugames.commons.router.sessions;

import java.time.Instant;

public class Session {
    private final String id;
    private volatile Instant lastAccessed;

    Session(String id) {
        this.id = id;
        lastAccessed = Instant.now();
    }

    public Instant getLastAccessed() {
        return lastAccessed;
    }

    public void destroy() {

    }

    void updateLastAccessed(Instant instant) {
        lastAccessed = instant;
    }

    String getId() {
        return id;
    }
}
