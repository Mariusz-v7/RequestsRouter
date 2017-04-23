package pl.mrugames.commons.router;

import java.io.Serializable;
import java.util.Map;

public class Request implements Serializable {
    private final long id;
    private final String session;
    private final String route;
    private final RequestMethod requestMethod;
    private final Map<String, Object> payload;
    private final RequestType requestType;

    public Request(long id, String session, String route, RequestMethod requestMethod, Map<String, Object> payload) {
        this.id = id;
        this.session = session;
        this.route = route;
        this.requestMethod = requestMethod;
        this.payload = payload;
        this.requestType = RequestType.STANDARD;
    }

    public Request(long id, String session, String route, RequestMethod requestMethod, Map<String, Object> payload, RequestType requestType) {
        this.id = id;
        this.session = session;
        this.route = route;
        this.requestMethod = requestMethod;
        this.payload = payload;
        this.requestType = requestType;
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

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;

        Request request = (Request) o;

        if (id != request.id) return false;
        if (!session.equals(request.session)) return false;
        if (!route.equals(request.route)) return false;
        if (requestMethod != request.requestMethod) return false;
        if (payload != null ? !payload.equals(request.payload) : request.payload != null) return false;
        return requestType == request.requestType;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + session.hashCode();
        result = 31 * result + route.hashCode();
        result = 31 * result + requestMethod.hashCode();
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        result = 31 * result + requestType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", session='" + session + '\'' +
                ", route='" + route + '\'' +
                ", requestMethod=" + requestMethod +
                ", payload=" + payload +
                ", requestType=" + requestType +
                '}';
    }
}
