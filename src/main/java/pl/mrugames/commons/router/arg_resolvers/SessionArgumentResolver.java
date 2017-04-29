package pl.mrugames.commons.router.arg_resolvers;

import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.RouteParameter;
import pl.mrugames.commons.router.sessions.Session;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SessionArgumentResolver {
    private SessionArgumentResolver() {
    }

    public Map<Class<?>, Optional<Object>> resolve(Session session, List<RouteParameter> parameters) {
        return parameters.stream()
                .filter(p -> RouteParameter.ParameterType.NONE == p.getParameterType())
                .map(p -> map(p, session))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<Class<?>, Optional<Object>> map(RouteParameter parameter, Session session) {
        @SuppressWarnings("unchecked")
        Optional<Object> result = session.get((Class) parameter.getType());

        return new AbstractMap.SimpleEntry<>(parameter.getType(), result);
    }
}
