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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;

        Request request = (Request) o;

        if (id != request.id) return false;
        if (!session.equals(request.session)) return false;
        if (!route.equals(request.route)) return false;
        return payload.equals(request.payload);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + session.hashCode();
        result = 31 * result + route.hashCode();
        result = 31 * result + payload.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", session='" + session + '\'' +
                ", route='" + route + '\'' +
                ", payload=" + payload +
                '}';
    }
}
