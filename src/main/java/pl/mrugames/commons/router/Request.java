package pl.mrugames.commons.router;

import java.io.Serializable;
import java.util.Map;

public class Request implements Serializable {
    private final long id;
    private final String session;
    private final String route;
    private final Map<String, Object> payload;

    public Request(long id, String session, String route, Map<String, Object> payload) {
        this.id = id;
        this.session = session;
        this.route = route;
        this.payload = payload;
    }

    public long getId() {
        return id;
    }

    public String getSession() {
        return session;
    }

    public String getRoute() {
        return route;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}
