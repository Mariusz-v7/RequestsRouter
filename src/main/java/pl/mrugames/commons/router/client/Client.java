package pl.mrugames.commons.router.client;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.mrugames.commons.router.RequestMethod;
import pl.mrugames.commons.router.RequestType;
import pl.mrugames.commons.router.Response;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class Client {
    private final static Logger logger = LoggerFactory.getLogger(Client.class);

    private final long defaultTimeout;
    private final Map<Long, Subject<Object>> buffer;
    private final AtomicLong id;
    private final Connector connector;

    public Client(long defaultTimeout,
                  Connector connector) {
        this.defaultTimeout = defaultTimeout;
        this.buffer = new ConcurrentHashMap<>();
        this.id = new AtomicLong();
        this.connector = connector;
        connector.onResponseReceive(this::onFrameReceive);
    }

    public void closeStream(long id) {
        _send(null, null, null, defaultTimeout, id, RequestType.CLOSE_STREAM);
    }

    public ResponseHandle<Object> send(String route) {
        return _send(route, null, RequestMethod.GET, defaultTimeout);
    }

    public ResponseHandle<Object> send(String route, Object payload) {
        return _send(route, payload, RequestMethod.GET, defaultTimeout);
    }

    public ResponseHandle<Object> send(String route, Object payload, RequestMethod requestMethod) {
        return _send(route, payload, requestMethod, defaultTimeout);
    }

    /**
     * @param timeout [ms]
     */
    public ResponseHandle<Object> send(String route, Object payload, RequestMethod requestMethod, long timeout) {
        return _send(route, payload, requestMethod, timeout);
    }

    void onFrameReceive(Response response) {
        Subject<Object> subject = buffer.get(response.getId());
        if (subject == null) {
            logger.warn("Unknown frame received: {}", response);
            return;
        }

        switch (response.getStatus()) {
            case OK:
                if (response.getPayload() != null) {
                    subject.onNext(response.getPayload());
                }
                clear(response.getId());
                break;
            case STREAM:
                if (response.getPayload() != null) {
                    subject.onNext(response.getPayload());
                }
                break;
            case CLOSE:
                clear(response.getId());
                break;
            default:
                subject.onError(new ErrorResponseException(response.getStatus(),
                        response.getPayload() != null ? response.getPayload().toString() : response.getPayload().getClass().getName())
                );
                clear(response.getId());
                break;
        }
    }

    ResponseHandle<Object> _send(String route, Object payload, RequestMethod requestMethod, long timeout) {
        long id = this.id.incrementAndGet();
        return _send(route, payload, requestMethod, timeout, id, RequestType.STANDARD);
    }

    Map<Long, Subject<Object>> getBuffer() {
        return buffer;
    }

    private ResponseHandle<Object> _send(String route, Object payload, RequestMethod requestMethod, long timeout, long id, RequestType requestType) {
        Subject<Object> subject = PublishSubject.create();
        buffer.put(id, subject);

        Observable<Object> observable = subject.timeout(timeout, TimeUnit.MILLISECONDS);

        observable.subscribe(n -> {
        }, e -> {
            if (e instanceof TimeoutException) {
                subject.onError(e);
                clear(id);
            }
        });

        connector.send(id, route, payload, requestMethod, requestType);

        return new ResponseHandle<>(id, subject.hide());
    }

    private void clear(long id) {
        Subject<?> subject = buffer.remove(id);
        if (subject != null) {
            subject.onComplete();
        }
    }

    public boolean isRunning() {
        return connector.isRunning();
    }

}
