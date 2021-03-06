package pl.mrugames.commons.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import pl.mrugames.commons.router.annotations.*;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

            List<Method> methods = new ArrayList<>();
            methods.addAll(Arrays.asList(controller.getClass().getMethods()));
            methods.addAll(Arrays.asList(controller.getClass().getSuperclass().getMethods()));

            for (Method method : methods) {
                Route route = method.getAnnotation(Route.class);
                if (route == null) {
                    continue;
                }

                List<RouteParameter> parameters = new ArrayList<>(method.getParameterCount());
                for (Parameter parameter : method.getParameters()) {
                    String name = null;
                    String defaultValue = null;
                    RouteParameter.ParameterType parameterType = RouteParameter.ParameterType.NONE;

                    Arg arg = parameter.getAnnotation(Arg.class);
                    PathVar pathVar = parameter.getAnnotation(PathVar.class);

                    if (arg != null && pathVar != null) {
                        throw new IllegalStateException("Both Arg and PathVar annotations are not allowed. Found on: " +
                                controller.getClass() + "#" + method.getName()
                        );
                    }

                    if (arg != null) {
                        name = arg.value();
                        if (!arg.required() && arg.defaultValue().equals(ArgDefaultValue.ARG_NULL_DEFAULT_VALUE)) {
                            defaultValue = null;
                        } else {
                            defaultValue = arg.defaultValue();
                        }
                        parameterType = RouteParameter.ParameterType.ARG;
                    } else if (pathVar != null) {
                        name = pathVar.value();
                        parameterType = RouteParameter.ParameterType.PATH_VAR;
                    }

                    Class<?>[] generics;
                    if (parameter.getParameterizedType() instanceof ParameterizedType) {
                        Type[] genericTypes = ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments();
                        generics = new Class[genericTypes.length];

                        for (int i = 0; i < generics.length; ++i) {
                            generics[i] = (Class<?>) genericTypes[i];
                        }
                    } else {
                        generics = new Class[0];
                    }

                    parameters.add(new RouteParameter(name, parameter.getType(), defaultValue, parameterType, generics));
                }

                String path = route.method().name() + ":" + pathMatcher.combine(baseRoute, route.value());

                RouteInfo routeInfo = new RouteInfo(controller, method, parameters, path);

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
