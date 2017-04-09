package pl.mrugames.commons.router;

import com.google.common.primitives.Primitives;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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

    public Object route(String route, RequestMethod requestMethod) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        return route(route, requestMethod, Collections.emptyMap());
    }

    public Object route(String route, RequestMethod requestMethod, Map<String, Object> params) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        route = requestMethod.name() + ":" + route;

        for (Map.Entry<String, RouteInfo> entry : routes.entrySet()) {
            if (pathMatcher.match(entry.getKey(), route)) {
                Map<String, String> pathParameters = pathMatcher.extractUriTemplateVariables(entry.getKey(), route);
                return navigate(entry.getValue(), pathParameters, params);
            }
        }

        throw new IllegalArgumentException("Route not found: " + route);
    }

    private Object navigate(RouteInfo routeInfo, Map<String, String> pathParameters, Map<String, Object> params) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        List<RouteInfo.Parameter> parameters = routeInfo.getParameters();

        List<Object> args = new ArrayList<>(parameters.size());

        for (RouteInfo.Parameter parameter : parameters) {
            switch (parameter.getParameterType()) {
                case PATH_VAR:
                    if (pathParameters.containsKey(parameter.getName())) {
                        args.add(convertFromString(parameter.getType(), pathParameters.get(parameter.getName())));
                    }
                    break;
                case ARG:
                    if (params.containsKey(parameter.getName())) {
                        args.add(params.get(parameter.getName()));
                    } else {
                        args.add(convertFromString(parameter.getType(), parameter.getDefaultValue()));
                    }
                    break;
                case NONE:
                    break;
            }
        }

        return routeInfo.getMethod().invoke(routeInfo.getControllerInstance(), args.toArray());
    }

    private Object convertFromString(Class<?> type, String s) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (type.isPrimitive()) {
            type = Primitives.wrap(type);
        }

        return type.getConstructor(String.class).newInstance(s);
    }
}
