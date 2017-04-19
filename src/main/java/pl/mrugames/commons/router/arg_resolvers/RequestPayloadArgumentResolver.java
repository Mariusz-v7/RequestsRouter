package pl.mrugames.commons.router.arg_resolvers;

import com.google.common.primitives.Primitives;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.RouteInfo;
import pl.mrugames.commons.router.annotations.ArgDefaultValue;
import pl.mrugames.commons.router.exceptions.IncompatibleParameterException;
import pl.mrugames.commons.router.exceptions.ParameterNotFoundException;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RequestPayloadArgumentResolver implements PayloadArgumentResolver<Map<String, Object>> {

    private RequestPayloadArgumentResolver() {
    }

    @Override
    public Map<String, Object> resolve(Map<String, Object> payload, List<RouteInfo.Parameter> parameters) {
        return parameters.stream()
                .filter(p -> RouteInfo.ParameterType.ARG == p.getParameterType())
                .map(p -> map(p, payload))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<String, Object> map(RouteInfo.Parameter parameter, Map<String, Object> payload) {
        Object result = payload.get(parameter.getName());
        if (result == null) {
            if (ArgDefaultValue.ARG_NULL_DEFAULT_VALUE.equals(parameter.getDefaultValue())) {
                throw new ParameterNotFoundException(parameter.getName());
            }

            result = parameter.getDefaultValue();
        }

        Class<?> type = parameter.getType().isPrimitive() ? Primitives.wrap(parameter.getType()) : parameter.getType();
        if (!type.isInstance(result)) {
            throw new IncompatibleParameterException(parameter.getName(), parameter.getType(), result.getClass());
        }

        return new AbstractMap.SimpleEntry<>(parameter.getName(), result);
    }
}
