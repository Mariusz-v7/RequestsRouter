package pl.mrugames.commons.router.arg_resolvers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.RouteParameter;
import pl.mrugames.commons.router.annotations.ArgDefaultValue;
import pl.mrugames.commons.router.exceptions.IncompatibleParameterException;
import pl.mrugames.commons.router.exceptions.ParameterNotFoundException;
import pl.mrugames.commons.router.exceptions.RouterException;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JsonPayloadArgumentResolver implements PayloadArgumentResolver<String> {
    private final ObjectMapper mapper;

    private JsonPayloadArgumentResolver(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Map<String, Object> resolve(String input, List<RouteParameter> parameters) {
        try {
            JsonNode rootNode = mapper.readTree(input);

            return parameters.stream()
                    .filter(p -> p.getParameterType() == RouteParameter.ParameterType.ARG)
                    .map(p -> map(p, rootNode))
                    .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);
        } catch (JsonParseException e) {
            throw new RouterException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RouterException(e.getMessage(), e);
        }
    }

    private Map.Entry<String, Object> map(RouteParameter parameter, JsonNode root) {
        JsonNode node = root.get(parameter.getName());

        String strNode;
        if (node == null) {
            if (ArgDefaultValue.ARG_NULL_DEFAULT_VALUE.equals(parameter.getDefaultValue())) {
                throw new ParameterNotFoundException(parameter.getName());
            }

            if (parameter.getDefaultValue() == null) {
                strNode = null;
            } else {
                strNode = "\"" + parameter.getDefaultValue() + "\"";
            }
        } else {
            strNode = node.toString();
        }

        if (strNode == null) {
            return new AbstractMap.SimpleEntry<>(parameter.getName(), null);
        }

        Object mapped;
        try {
            if (parameter.getGenerics().length > 0) {
                JavaType type = mapper.getTypeFactory().constructParametricType(parameter.getType(), parameter.getGenerics());
                mapped = mapper.readValue(strNode, type);
            } else {
                mapped = mapper.readValue(strNode, parameter.getType());
            }
        } catch (InvalidFormatException e) {
            throw new IncompatibleParameterException(parameter.getName(), e.getTargetType(), e);
        } catch (IOException e) {
            throw new RouterException(e.getMessage(), e);
        }

        return new AbstractMap.SimpleEntry<>(parameter.getName(), mapped);
    }
}
