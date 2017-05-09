package pl.mrugames.commons.router.request_handlers;

import com.sun.istack.internal.Nullable;
import io.reactivex.Observable;
import pl.mrugames.commons.router.sessions.Session;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class RouterResult<T extends Serializable> {
    @Nullable
    private final Session session;

    @NotNull
    private final Observable<T> response;

    RouterResult(@Nullable Session session, @NotNull Observable<T> response) {
        this.session = session;
        this.response = response;
    }

    public Session getSession() {
        return session;
    }

    public Observable<T> getResponse() {
        return response;
    }
}
