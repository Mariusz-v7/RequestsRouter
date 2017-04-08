package pl.mrugames.commons.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import pl.mrugames.commons.router.annotations.Controller;
import pl.mrugames.commons.router.annotations.Route;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


@Component
class RouterInitializer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ApplicationContext context;
    private final AntPathMatcher pathMatcher;
    private final Map<String, RouteInfo> routes;

    private volatile String[] controllers;

    private RouterInitializer(ApplicationContext applicationContext, AntPathMatcher pathMatcher) {
        this.context = applicationContext;
        this.pathMatcher = pathMatcher;
        this.routes = new HashMap<>();
    }

    @PostConstruct
    private void postConstruct() {
        controllers = context.getBeanNamesForAnnotation(Controller.class);
        logger.info("{} controllers found.", controllers.length);

        for (String bean : controllers) {
            Object controller = context.getBean(bean);
            Controller annotation = controller.getClass().getAnnotation(Controller.class);
            String baseRoute = annotation.value();

            for (Method method : controller.getClass().getMethods()) {
                Route route = method.getAnnotation(Route.class);
                if (route == null) {
                    continue;
                }

                String path = pathMatcher.combine(baseRoute, route.value());

                routes.put(path, new RouteInfo(controller, method));

                logger.info("Mapped {} to {}#{}", path, controller.getClass().getSimpleName(), method.getName());
            }
        }
    }

    String[] getControllers() {
        return controllers;
    }

    Map<String, RouteInfo> getRoutes() {
        return routes;
    }
}
