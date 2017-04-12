package pl.mrugames.commons.router.arg_resolvers;

import pl.mrugames.commons.router.RouteInfo;

import java.util.List;
import java.util.Map;

interface PayloadArgumentResolver<T> {
    Map<String, Object> resolve(T input, List<RouteInfo.Parameter> parameters);
}
