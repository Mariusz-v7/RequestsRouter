package pl.mrugames.commons.router;

import java.util.function.Function;

public class Mono<T> {
    public static Mono<Void> OK = new Mono<>(ResponseStatus.OK);

    public static Mono<Void> of(ResponseStatus status) {
        return new Mono<>(status);
    }

    public static <T> Mono<T> error(ResponseStatus status, String error) {
        if (status == ResponseStatus.OK) {
            throw new IllegalStateException("Status should be an error");
        }

        return new Mono<>(status, error);
    }

    public static <T> Mono<T> ok(T payload) {
        return new Mono<>(ResponseStatus.OK, payload);
    }

    private final ResponseStatus responseStatus;
    private final T payload;
    private final String error;

    private Mono(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
        payload = null;
        error = null;
    }

    private Mono(ResponseStatus responseStatus, T payload) {
        this.responseStatus = responseStatus;
        this.payload = payload;
        this.error = null;
    }

    private Mono(ResponseStatus responseStatus, String error) {
        this.responseStatus = responseStatus;
        this.error = error;
        this.payload = null;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public T getPayload() {
        return payload;
    }

    public String getError() {
        return error;
    }

    public <M> Mono<M> map(Function<T, M> mapper) {
        if (responseStatus != ResponseStatus.OK) {
            return new Mono<>(responseStatus, error);
        } else {
            return new Mono<>(responseStatus, mapper.apply(payload));
        }
    }
}
