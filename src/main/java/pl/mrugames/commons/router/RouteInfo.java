package pl.mrugames.commons.router;

import pl.mrugames.commons.router.permissions.AccessType;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class RouteInfo {
    private final Object controllerInstance;
    private final Method method;
    private final List<RouteParameter> parameters;
    private final String routePattern;
    private final AccessType accessType;
    private final List<String> allowedRoles;

    public RouteInfo(Object controllerInstance,
                     Method method,
                     List<RouteParameter> parameters,
                     String routePattern,
                     AccessType accessType,
                     List<String> allowedRoles) {
        this.controllerInstance = controllerInstance;
        this.method = method;
        this.parameters = parameters;
        this.routePattern = routePattern;
        this.accessType = accessType;
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

    public AccessType getAccessType() {
        return accessType;
    }

    public List<String> getAllowedRoles() {
        return allowedRoles;
    }
}
