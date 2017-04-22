package pl.mrugames.commons.router.sessions;

import pl.mrugames.commons.router.permissions.RoleHolder;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Session {
    private final String id;
    private final Consumer<Session> onDestroyMethod;
    private final Map<Class<?>, Object> map;

    private volatile Instant lastAccessed;

    public Session(String id, Consumer<Session> onDestroyMethod) {
        this.id = id;
        this.onDestroyMethod = onDestroyMethod;
        this.map = new ConcurrentHashMap<>();

        lastAccessed = Instant.now();
    }

    @SuppressWarnings("unchecked")
    public <T> T add(T object) {
        return (T) map.put(object.getClass(), object);
    }

    @SuppressWarnings("unchecked")
    public <T> T add(Class<T> type, T object) {
        return (T) map.put(type, object);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(Class<T> type) {
        return Optional.ofNullable((T) map.get(type));
    }

    @SuppressWarnings("unchecked")
    public <T> T remove(Class<T> type) {
        return (T) map.remove(type);
    }

    @SuppressWarnings("unchecked")
    public <T> T merge(T object, BiFunction<T, T, T> remappingFunction) {
        return (T) map.merge(object.getClass(), object, (BiFunction) remappingFunction);
    }

    @SuppressWarnings("unchecked")
    public <T> T compute(Class<T> type, BiFunction<Class<T>, T, T> remappingFunction) {
        return (T) map.compute(type, (BiFunction) remappingFunction);
    }

    public void addAuthenticatedUser(RoleHolder roleHolder) {
        map.put(RoleHolder.class, roleHolder);
    }

    public void destroy() {
        onDestroyMethod.accept(this);
        map.clear();
    }

    Instant getLastAccessed() {
        return lastAccessed;
    }

    void updateLastAccessed(Instant instant) {
        lastAccessed = instant;
    }

    String getId() {
        return id;
    }
}
