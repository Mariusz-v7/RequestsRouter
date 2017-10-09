package pl.mrugames.commons.router.sessions;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.Subject;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class Session {
    private final static ThreadLocal<Session> localSession = new InheritableThreadLocal<>();

    public static Optional<Session> getLocalSession() {
        Session session = localSession.get();

        if (session != null) {
            session.updateLastAccessed(Instant.now());
        }

        return Optional.ofNullable(session);
    }

    public static Session getExistingLocalSession() {
        Optional<Session> session = getLocalSession();
        if (!session.isPresent()) {
            throw new SessionDoesNotExistException();
        }

        return session.get();
    }

    static void destroyLocalSession() {
        getLocalSession().ifPresent(Session::destroy);
        localSession.remove();
    }

    static synchronized void setLocalSession(Session session) {
        Session current = localSession.get();
        if (current != null && current != session) {
            current.destroy();
        }

        localSession.set(session);
    }

    private final Runnable onDestroyMethod;
    private final Map<Class<?>, Object> map;
    private final Map<Long, Subject<?>> emitters;
    private final Map<Long, Disposable> subscriptions;

    private volatile Instant lastAccessed;
    private volatile boolean isDestroyed;
    private volatile String securityCode;

    public Session(Runnable onDestroyMethod) {
        this.onDestroyMethod = onDestroyMethod;
        this.map = new ConcurrentHashMap<>();
        this.emitters = new ConcurrentHashMap<>();
        this.subscriptions = new ConcurrentHashMap<>();

        lastAccessed = Instant.now();
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T add(T object) {
        if (isDestroyed) {
            throw new SessionExpiredException();
        }

        return (T) map.put(object.getClass(), object);
    }

    public synchronized Object[] add(Object... objects) {
        for (int i = 0; i < objects.length; ++i) {
            objects[i] = add(objects[i]);
        }

        return objects;
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T add(Class<T> type, T object) {
        if (isDestroyed) {
            throw new SessionExpiredException();
        }

        return (T) map.put(type, object);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> Optional<T> get(Class<T> type) {
        if (isDestroyed) {
            throw new SessionExpiredException();
        }

        return Optional.ofNullable((T) map.get(type));
    }

    /**
     * Call it only if you are 100% sure that parameter exists.
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T getExisting(Class<T> type) {
        Optional<T> optional = get(type);
        if (optional.isPresent()) {
            return optional.get();
        }

        throw new IllegalStateException("Parameter " + type + " does not exist");
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T remove(Class<T> type) {
        if (isDestroyed) {
            throw new SessionExpiredException();
        }

        return (T) map.remove(type);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T merge(T object, BiFunction<T, T, T> remappingFunction) {
        if (isDestroyed) {
            throw new SessionExpiredException();
        }

        return (T) map.merge(object.getClass(), object, (BiFunction) remappingFunction);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T compute(Class<T> type, BiFunction<Class<T>, T, T> remappingFunction) {
        if (isDestroyed) {
            throw new SessionExpiredException();
        }

        return (T) map.compute(type, (BiFunction) remappingFunction);
    }

    public synchronized void destroy() {
        if (isDestroyed) {
            return;
        }

        isDestroyed = true;
        onDestroyMethod.run();

        emitters.values()
                .forEach(Subject::onComplete);

        subscriptions.values()
                .forEach(Disposable::dispose);

        map.clear();
        emitters.clear();
        subscriptions.clear();
    }

    public synchronized int getEmittersAmount() {
        return emitters.size();
    }

    public synchronized int getSubscriptionsAmount() {
        return subscriptions.size();
    }

    public synchronized void registerEmitter(long requestId, Subject<?> emitter) {
        if (isDestroyed) {
            throw new SessionExpiredException();
        }

        if (emitters.containsKey(requestId)) {
            throw new IllegalArgumentException("Emitter with id " + requestId + " is already registered");
        }

        emitters.put(requestId, emitter);

        emitter.subscribe(next -> {
        }, err -> {
        }, () -> this.unregisterEmitter(requestId));
    }

    public synchronized void unregisterEmitter(long requestId) {
        Subject<?> subject = emitters.remove(requestId);
        if (subject != null) {
            subject.onComplete();
        }
    }

    public synchronized void registerSubscription(long requestId, Disposable subscription) {
        if (isDestroyed) {
            throw new SessionExpiredException();
        }

        if (subscriptions.containsKey(requestId)) {
            throw new IllegalArgumentException("Subscription with id " + requestId + " is already registered");
        }

        subscriptions.put(requestId, subscription);

        subscriptions.forEach((k, v) -> {
            if (v.isDisposed()) {
                subscriptions.remove(k);
            }
        });
    }

    public synchronized void unregisterSubscription(long requestId) {
        Disposable disposable = subscriptions.remove(requestId);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    synchronized boolean isDestroyed() {
        return isDestroyed;
    }

    synchronized Instant getLastAccessed() {
        return lastAccessed;
    }

    synchronized void updateLastAccessed(Instant instant) {
        lastAccessed = instant;
    }

    public synchronized String getSecurityCode() {
        if (isDestroyed) {
            throw new SessionExpiredException();
        }

        return securityCode;
    }

    public synchronized void setSecurityCode(String securityCode) {
        if (isDestroyed) {
            throw new SessionExpiredException();
        }

        this.securityCode = securityCode;
    }
}
