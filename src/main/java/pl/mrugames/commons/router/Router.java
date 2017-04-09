package pl.mrugames.commons.router;

import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

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

    public Object route(String route, RequestMethod requestMethod, Object... args) throws InvocationTargetException, IllegalAccessException {
        route = requestMethod.name() + ":" + route;

        for (Map.Entry<String, RouteInfo> entry : routes.entrySet()) {
            if (pathMatcher.match(entry.getKey(), route)) {
                RouteInfo routeInfo = entry.getValue();

                return routeInfo.getMethod().invoke(routeInfo.getControllerInstance());
            }
        }

        return null;
    }
}
