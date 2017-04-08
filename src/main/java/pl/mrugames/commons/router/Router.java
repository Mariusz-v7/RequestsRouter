package pl.mrugames.commons.router;

import java.util.Map;

public class Router {
    private final Map<String, RouteInfo> routes;

    public Router(Map<String, RouteInfo> routes) {
        this.routes = routes;
    }

    public Object route(String route, Object... args) {
        return null;
    }
}
