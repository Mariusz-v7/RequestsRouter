package pl.mrugames.commons.router;

import com.google.common.primitives.Primitives;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
class Router {
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

    Object route(String route, RequestMethod requestMethod, Map<String, Object> payloadParams, Map<Class<?>, Object> sessionParams) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        route = requestMethod.name() + ":" + route;

        for (Map.Entry<String, RouteInfo> entry : routes.entrySet()) {
            if (pathMatcher.match(entry.getKey(), route)) {
                Map<String, String> pathParameters = pathMatcher.extractUriTemplateVariables(entry.getKey(), route);
                return navigate(entry.getValue(), pathParameters, payloadParams, sessionParams);
            }
        }

        throw new IllegalArgumentException("Route not found: " + route);
    }

    private Object navigate(RouteInfo routeInfo, Map<String, String> pathParameters, Map<String, Object> payloadParams, Map<Class<?>, Object> sessionParams) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
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
                    if (payloadParams.containsKey(parameter.getName())) {
                        args.add(payloadParams.get(parameter.getName()));
                    } else {
                        args.add(convertFromString(parameter.getType(), parameter.getDefaultValue()));
                    }
                    break;
                case NONE:
                    args.add(sessionParams.get(parameter.getType()));
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
