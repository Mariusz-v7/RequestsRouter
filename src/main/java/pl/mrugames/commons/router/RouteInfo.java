package pl.mrugames.commons.router;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class RouteInfo implements Serializable {
    private final Object controllerInstance;
    private final Method method;
    private final List<RouteParameter> parameters;
    private final String routePattern;
    private final List<String> allowedRoles;

    public RouteInfo(Object controllerInstance,
                     Method method,
                     List<RouteParameter> parameters,
                     String routePattern,
                     List<String> allowedRoles) {
        this.controllerInstance = controllerInstance;
        this.method = method;
        this.parameters = parameters;
        this.routePattern = routePattern;
        this.allowedRoles = Collections.unmodifiableList(allowedRoles);
    }

    Object getControllerInstance() {
        return controllerInstance;
    }

    Method getMethod() {
        return method;
    }

    public List<RouteParameter> getParameters() {
        return parameters;
    }

    public String getRoutePattern() {
        return routePattern;
    }

    public List<String> getAllowedRoles() {
        return allowedRoles;
    }
}
