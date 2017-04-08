package pl.mrugames.commons.router;

import java.lang.reflect.Method;

class RouteInfo {
    private final Object controllerInstance;
    private final Method method;

    RouteInfo(Object controllerInstance, Method method) {
        this.controllerInstance = controllerInstance;
        this.method = method;
    }

    Object getControllerInstance() {
        return controllerInstance;
    }

    Method getMethod() {
        return method;
    }
}
