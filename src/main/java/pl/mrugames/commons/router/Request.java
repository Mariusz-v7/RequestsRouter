package pl.mrugames.commons.router;

import java.io.Serializable;

public class Request implements Serializable {
    private final long id;
    private final String session;
    private final String securityCode;
    private final String route;
    private final RequestMethod requestMethod;
    private final Object payload;
    private final RequestType requestType;

    /**
     * @param payload: can be an instance of Map<String, Object> or Object.
     *                 In case of Object, the class has to contain proper getters.
     *                 e.g. if parameter name is "a" then getter is "getA",
     *                 when parameter name is "helloWorld" then getter is "getHelloWorld".
     */
    public Request(long id, String session, String securityCode, String route, RequestMethod requestMethod, Object payload) {
        this.id = id;
        this.session = session;
        this.securityCode = securityCode;
        this.route = route;
        this.requestMethod = requestMethod;
        this.payload = payload;
        this.requestType = RequestType.STANDARD;
    }

    public Request(long id, String session, String securityCode, String route, RequestMethod requestMethod, Object payload, RequestType requestType) {
        this.id = id;
        this.session = session;
        this.securityCode = securityCode;
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

    public Object getPayload() {
        return payload;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", session='" + session + '\'' +
                ", securityCode='" + securityCode + '\'' +
                ", route='" + route + '\'' +
                ", requestMethod=" + requestMethod +
                ", payload=" + payload +
                ", requestType=" + requestType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;

        Request request = (Request) o;

        if (id != request.id) return false;
        if (session != null ? !session.equals(request.session) : request.session != null) return false;
        if (securityCode != null ? !securityCode.equals(request.securityCode) : request.securityCode != null)
            return false;
        if (route != null ? !route.equals(request.route) : request.route != null) return false;
        if (requestMethod != request.requestMethod) return false;
        if (payload != null ? !payload.equals(request.payload) : request.payload != null) return false;
        return requestType == request.requestType;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (session != null ? session.hashCode() : 0);
        result = 31 * result + (securityCode != null ? securityCode.hashCode() : 0);
        result = 31 * result + (route != null ? route.hashCode() : 0);
        result = 31 * result + (requestMethod != null ? requestMethod.hashCode() : 0);
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        result = 31 * result + (requestType != null ? requestType.hashCode() : 0);
        return result;
    }

}
