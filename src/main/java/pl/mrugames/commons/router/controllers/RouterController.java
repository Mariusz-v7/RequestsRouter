package pl.mrugames.commons.router.controllers;

import pl.mrugames.commons.router.RouteInfo;
import pl.mrugames.commons.router.RouterInitializer;
import pl.mrugames.commons.router.annotations.Controller;
import pl.mrugames.commons.router.annotations.Route;

import java.util.Map;

@Controller("system/router")
class RouterController {
    private final RouterInitializer routerInitializer;

    private RouterController(RouterInitializer routerInitializer) {
        this.routerInitializer = routerInitializer;
    }

    @Route("routes")
    public Map<String, RouteInfo> getRoutes() {
        return routerInitializer.getRoutes();
    }
}
