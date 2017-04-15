package pl.mrugames.commons.router.arg_resolvers;

import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.RouteInfo;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
class SessionArgumentResolver {
    Map<Class<?>, Optional<Object>> resolve(Map<Class<?>, Object> sessionArguments, List<RouteInfo.Parameter> parameters) {
        return parameters.stream()
                .filter(p -> RouteInfo.ParameterType.NONE == p.getParameterType())
                .map(p -> map(p, sessionArguments))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<Class<?>, Optional<Object>> map(RouteInfo.Parameter parameter, Map<Class<?>, Object> sessionArguments) {
        Object result;

        if (sessionArguments.containsKey(parameter.getType())) {
            result = sessionArguments.get(parameter.getType());
        } else {
            result = null;
        }

        return new AbstractMap.SimpleEntry<>(parameter.getType(), Optional.ofNullable(result));
    }
}
