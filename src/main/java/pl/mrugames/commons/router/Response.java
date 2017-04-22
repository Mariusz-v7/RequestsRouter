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
}
