package pl.mrugames.commons.router.controllers;

import org.springframework.context.i18n.LocaleContextHolder;
import pl.mrugames.commons.router.RouteInfo;
import pl.mrugames.commons.router.RouterInitializer;
import pl.mrugames.commons.router.annotations.Controller;
import pl.mrugames.commons.router.annotations.PathVar;
import pl.mrugames.commons.router.annotations.Route;

import java.util.Locale;
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

    @Route("locale/{locale}")
    public Locale setLocale(@PathVar("locale") String strLocale) {
        Locale locale = Locale.forLanguageTag(strLocale);
        LocaleContextHolder.setLocale(locale);
        return locale;
    }
}
