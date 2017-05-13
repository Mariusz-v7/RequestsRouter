package pl.mrugames.commons.router.sessions;

import io.reactivex.subjects.Subject;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Session {
    private final static ThreadLocal<Session> localSession = new ThreadLocal<>();

    public static Optional<Session> getLocalSession() {
        return Optional.of(localSession.get());
    }

    static void setLocalSession(Session session) {
        Session current = localSession.get();
        if (current != null && current != session) {
            current.destroy();
        }

        localSession.set(session);
    }

    private final String id;
    private final Consumer<Session> onDestroyMethod;
    private final Map<Class<?>, Object> map;
    private final Map<Long, Subject<?>> emitters;

    private volatile Instant lastAccessed;
    private volatile boolean isDestroyed;
    private volatile String securityCode;

    public Session(String id, Consumer<Session> onDestroyMethod) {
        this.id = id;
        this.onDestroyMethod = onDestroyMethod;
        this.map = new ConcurrentHashMap<>();
        this.emitters = new ConcurrentHashMap<>();

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
        onDestroyMethod.accept(this);

        emitters.entrySet().stream()
                .map(Map.Entry::getValue)
                .forEach(Subject::onComplete);

        map.clear();
        emitters.clear();
    }

    public synchronized int getEmittersAmount() {
        return emitters.size();
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

    synchronized boolean isDestroyed() {
        return isDestroyed;
    }

    synchronized Instant getLastAccessed() {
        return lastAccessed;
    }

    synchronized void updateLastAccessed(Instant instant) {
        lastAccessed = instant;
    }

    synchronized String getId() {
        return id;
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
