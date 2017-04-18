package pl.mrugames.commons.router.arg_resolvers;

import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.RouteInfo;
import pl.mrugames.commons.router.sessions.Session;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
class SessionArgumentResolver {
    Map<Class<?>, Optional<Object>> resolve(Session session, List<RouteInfo.Parameter> parameters) {
        return parameters.stream()
                .filter(p -> RouteInfo.ParameterType.NONE == p.getParameterType())
                .map(p -> map(p, session))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<Class<?>, Optional<Object>> map(RouteInfo.Parameter parameter, Session session) {
        @SuppressWarnings("unchecked")
        Optional<Object> result = session.get((Class) parameter.getType());

        return new AbstractMap.SimpleEntry<>(parameter.getType(), result);
    }
}
