package pl.mrugames.commons.router;

public class Mono<T> {
    public static Mono<Void> OK = new Mono<>(ResponseStatus.OK);

    public static Mono<Void> of(ResponseStatus status) {
        return new Mono<>(status);
    }

    public static <T> Mono<T> of(ResponseStatus status, T payload) {
        return new Mono<>(status, payload);
    }

    public static <T> Mono<T> ok(T payload) {
        return new Mono<>(ResponseStatus.OK, payload);
    }

    private final ResponseStatus responseStatus;
    private final T payload;

    private Mono(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
        payload = null;
    }

    private Mono(ResponseStatus responseStatus, T payload) {
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
