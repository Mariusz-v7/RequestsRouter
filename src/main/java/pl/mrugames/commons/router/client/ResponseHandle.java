package pl.mrugames.commons.router.client;

import io.reactivex.Observable;

public class ResponseHandle<T> {
    public final long id;
    public final Observable<T> response;

    public ResponseHandle(long id, Observable<T> response) {
        this.id = id;
        this.response = response;
    }
}
