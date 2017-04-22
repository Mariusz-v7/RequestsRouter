package pl.mrugames.commons.router;

public class Mono<T> {
    public static Mono<Void> OK = new Mono<>(ResponseStatus.OK);

    private final ResponseStatus responseStatus;
    private final T payload;

    public Mono(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
        payload = null;
    }

    public Mono(ResponseStatus responseStatus, T payload) {
        this.responseStatus = responseStatus;
        this.payload = payload;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public T getPayload() {
        return payload;
    }
}
