package pl.mrugames.commons.router.arg_resolvers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.RouteInfo;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
class JsonPayloadArgumentResolver implements PayloadArgumentResolver<String> {
    private final ObjectMapper mapper;

    private JsonPayloadArgumentResolver(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Map<String, Object> resolve(String input, List<RouteInfo.Parameter> parameters) {
        try {
            JsonNode rootNode = mapper.readTree(input);

            return parameters.stream()
                    .filter(p -> p.getParameterType() == RouteInfo.ParameterType.ARG)
                    .map(p -> map(p, rootNode))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (IOException e) {
            e.printStackTrace(); // TODO: better exception handling
        }
        return null;
    }

    private Map.Entry<String, Object> map(RouteInfo.Parameter parameter, JsonNode root) {
        JsonNode node = root.get(parameter.getName());
        Object mapped = null;
        try {
            mapped = mapper.readValue(node.toString(), parameter.getType());
        } catch (IOException e) {
            e.printStackTrace(); /// TODO: better exception handling
        }

        return new AbstractMap.SimpleEntry<>(parameter.getName(), mapped);
    }
}
