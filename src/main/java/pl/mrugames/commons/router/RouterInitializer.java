package pl.mrugames.commons.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import pl.mrugames.commons.router.annotations.*;
import pl.mrugames.commons.router.permissions.AccessType;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Component
public class RouterInitializer {
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

                AccessType accessType = AccessType.ONLY_LOGGED_IN;
                AllowedRoles allowedRoles = method.getAnnotation(AllowedRoles.class);
                List<String> allowedRolesList = Collections.emptyList();

                if (allowedRoles != null) {
                    accessType = AccessType.ONLY_WITH_SPECIFIC_ROLES;
                    allowedRolesList = Arrays.asList(allowedRoles.value());
                } else if (method.getAnnotation(OnlyNotLoggedAllowed.class) != null) {
                    accessType = AccessType.ONLY_NOT_LOGGED_IN;
                } else if (method.getAnnotation(AllAllowed.class) != null) {
                    accessType = AccessType.ALL_ALLOWED;
                }

                List<RouteInfo.Parameter> parameters = new ArrayList<>(method.getParameterCount());
                for (Parameter parameter : method.getParameters()) {
                    String name = null;
                    String defaultValue = null;
                    RouteInfo.ParameterType parameterType = RouteInfo.ParameterType.NONE;

                    Arg arg = parameter.getAnnotation(Arg.class);
                    PathVar pathVar = parameter.getAnnotation(PathVar.class);

                    if (arg != null && pathVar != null) {
                        throw new IllegalStateException("Both Arg and PathVar annotations are not allowed. Found on: " +
                                controller.getClass() + "#" + method.getName()
                        );
                    }

                    if (arg != null) {
                        name = arg.value();
                        defaultValue = arg.defaultValue();
                        parameterType = RouteInfo.ParameterType.ARG;
                    } else if (pathVar != null) {
                        name = pathVar.value();
                        parameterType = RouteInfo.ParameterType.PATH_VAR;
                    }

                    parameters.add(new RouteInfo.Parameter(name, parameter.getType(), defaultValue, parameterType));
                }

                String path = route.method().name() + ":" + pathMatcher.combine(baseRoute, route.value());

                RouteInfo routeInfo = new RouteInfo(controller, method, parameters, path, accessType, allowedRolesList);

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

    public Map<String, RouteInfo> getRoutes() {
        return routes;
    }
}
