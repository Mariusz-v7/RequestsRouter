package pl.mrugames.commons.router;

import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.arg_resolvers.PayloadArgumentResolver;

import java.util.List;
import java.util.Map;

@Component
public class JsonPayloadArgumentResolver implements PayloadArgumentResolver<String> {
    @Override
    public Map<String, Object> resolve(String input, List<RouteInfo.Parameter> parameters) {
        return null;
    }
}
