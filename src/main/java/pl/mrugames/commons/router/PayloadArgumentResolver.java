package pl.mrugames.commons.router;

import java.util.List;
import java.util.Map;

public interface PayloadArgumentResolver<T> {
    Map<String, Object> resolve(T input, List<RouteInfo.Parameter> parameters);
}
