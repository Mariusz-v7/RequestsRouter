package pl.mrugames.commons.router;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
class RequestPayloadArgumentResolver implements PayloadArgumentResolver<Request> {
    @Override
    public Map<String, Object> resolve(Request input, List<RouteInfo.Parameter> parameters) {
        return input.getPayload();
    }
}
