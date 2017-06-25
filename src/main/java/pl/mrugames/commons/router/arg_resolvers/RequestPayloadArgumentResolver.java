package pl.mrugames.commons.router.arg_resolvers;

import com.google.common.primitives.Primitives;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.RouteParameter;
import pl.mrugames.commons.router.annotations.ArgDefaultValue;
import pl.mrugames.commons.router.exceptions.IncompatibleParameterException;
import pl.mrugames.commons.router.exceptions.ParameterNotFoundException;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RequestPayloadArgumentResolver implements PayloadArgumentResolver<Map<String, Object>> {

    private RequestPayloadArgumentResolver() {
    }

    @Override
    public Map<String, Object> resolve(Map<String, Object> payload, List<RouteParameter> parameters) {
        return parameters.stream()
                .filter(p -> RouteParameter.ParameterType.ARG == p.getParameterType())
                .map(p -> map(p, payload))
                .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);
    }

    private Map.Entry<String, Object> map(RouteParameter parameter, Map<String, Object> payload) {
        Object result;
        if (payload.containsKey(parameter.getName())) {
            result = payload.get(parameter.getName());
        } else {
            if (ArgDefaultValue.ARG_NULL_DEFAULT_VALUE.equals(parameter.getDefaultValue())) {
                throw new ParameterNotFoundException(parameter.getName());
            }

            result = parameter.getDefaultValue();
        }

        if (result == null) {
            return new AbstractMap.SimpleEntry<>(parameter.getName(), null);
        }

        Class<?> type = parameter.getType().isPrimitive() ? Primitives.wrap(parameter.getType()) : parameter.getType();
        if (!type.isInstance(result)) {
            throw new IncompatibleParameterException(parameter.getName(), parameter.getType(), result.getClass());
        }

        return new AbstractMap.SimpleEntry<>(parameter.getName(), result);
    }
}
