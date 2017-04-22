package pl.mrugames.commons.router;

import java.io.Serializable;

public class Response implements Serializable {

    private final long id;
    private final ResponseStatus status;
    private final Object payload;

    public Response(long id, ResponseStatus status, Object payload) {
        this.id = id;
        this.status = status;
        this.payload = payload;
    }

    public long getId() {
        return id;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Response{" +
                "id=" + id +
                ", status=" + status +
                ", payload=" + payload +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Response)) return false;

        Response response = (Response) o;

        if (id != response.id) return false;
        if (status != response.status) return false;
        return payload != null ? payload.equals(response.payload) : response.payload == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + status.hashCode();
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        return result;
    }
}
