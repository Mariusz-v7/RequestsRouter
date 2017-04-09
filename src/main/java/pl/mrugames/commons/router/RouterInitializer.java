package pl.mrugames.commons.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import pl.mrugames.commons.router.annotations.Arg;
import pl.mrugames.commons.router.annotations.Controller;
import pl.mrugames.commons.router.annotations.Route;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

                List<RouteInfo.Parameter> parameters = new ArrayList<>(method.getParameterCount());
                for (Parameter parameter : method.getParameters()) {
                    String name = null;
                    String defaultValue = null;

                    Arg arg = parameter.getAnnotation(Arg.class);
                    if (arg != null) {
                        name = arg.value();
                        defaultValue = arg.defaultValue();
                    }

                    parameters.add(new RouteInfo.Parameter(name, parameter.getType(), defaultValue));
                }

                String path = route.method().name() + ":" + pathMatcher.combine(baseRoute, route.value());

                RouteInfo routeInfo = new RouteInfo(controller, method, parameters);

                if (routes.containsKey(path)) {
                    RouteInfo colliding = routes.get(path);
                    throw new IllegalStateException("Route " + path + " was already defined. Controller: " +
                            controller.getClass().getName() + "#" + method.getName() +
                            " and " +
                            colliding.getControllerInstance().getClass().getName() + "#" + colliding.getMethod().getName()
                    );
                }

                routes.put(path, routeInfo);
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
