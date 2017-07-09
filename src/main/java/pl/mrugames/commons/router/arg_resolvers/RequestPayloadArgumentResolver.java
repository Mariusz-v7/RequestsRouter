package pl.mrugames.commons.router.arg_resolvers;

import com.google.common.primitives.Primitives;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.RouteParameter;
import pl.mrugames.commons.router.annotations.ArgDefaultValue;
import pl.mrugames.commons.router.exceptions.IncompatibleParameterException;
import pl.mrugames.commons.router.exceptions.ParameterNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RequestPayloadArgumentResolver implements PayloadArgumentResolver<Object> {

    private RequestPayloadArgumentResolver() {
    }

    @Override
    public Map<String, Object> resolve(Object payload, List<RouteParameter> parameters) {
        return parameters.stream()
                .filter(p -> RouteParameter.ParameterType.ARG == p.getParameterType())
                .map(p -> map(p, payload))
                .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);
    }

    private Map.Entry<String, Object> map(RouteParameter parameter, Object payload) {
        Object result;

        if (payload instanceof Map) {
            Map payloadMap = (Map) payload;
            if (payloadMap.containsKey(parameter.getName())) {
                result = payloadMap.get(parameter.getName());
            } else {
                if (ArgDefaultValue.ARG_NULL_DEFAULT_VALUE.equals(parameter.getDefaultValue())) {
                    throw new ParameterNotFoundException(parameter.getName());
                }

                result = parameter.getDefaultValue();
            }
        } else {
            try {
                String getterName = "get" + parameter.getName().substring(0, 1).toUpperCase() + parameter.getName().substring(1);
                Method getter = payload.getClass().getMethod(getterName);
                result = getter.invoke(payload);
            } catch (NoSuchMethodException e) {
                if (ArgDefaultValue.ARG_NULL_DEFAULT_VALUE.equals(parameter.getDefaultValue())) {
                    throw new ParameterNotFoundException(parameter.getName());
                }

                result = parameter.getDefaultValue();
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new ParameterNotFoundException(parameter.getName(), e);
            }
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
