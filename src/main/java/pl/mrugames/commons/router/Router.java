package pl.mrugames.commons.router;

import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class Router {
    private final Map<String, RouteInfo> routes;
    private final RouterInitializer initializer;
    private final AntPathMatcher pathMatcher;

    private Router(RouterInitializer initializer, AntPathMatcher antPathMatcher) {
        this.initializer = initializer;
        this.routes = new HashMap<>();
        this.pathMatcher = antPathMatcher;
    }

    @PostConstruct
    private void postConstruct() {
        routes.putAll(initializer.getRoutes());
    }

    public RouteInfo findRoute(String route, RequestMethod requestMethod) {
        route = requestMethod.name() + ":" + route;

        for (Map.Entry<String, RouteInfo> entry : routes.entrySet()) {
            if (pathMatcher.match(entry.getKey(), route)) {
                return entry.getValue();
            }
        }

        throw new IllegalArgumentException("Route not found: " + route);
    }

    public Object navigate(RouteInfo routeInfo, Map<String, Object> pathParameters, Map<String, Object> payloadParameters, Map<Class<?>, Optional<Object>> sessionParameters) {
        return null; //TODO
    }

}
