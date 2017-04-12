package pl.mrugames.commons.router;

import java.io.Serializable;

class Response implements Serializable {
    enum Status {
        OK, ERROR, STREAM, CLOSE
    }

    private final long id;
    private final Status status;
    private final Object payload;

    public Response(long id, Status status, Object payload) {
        this.id = id;
        this.status = status;
        this.payload = payload;
    }

    public long getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public Object getPayload() {
        return payload;
    }
}