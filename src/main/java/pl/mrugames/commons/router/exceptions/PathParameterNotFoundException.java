package pl.mrugames.commons.router.exceptions;

public class PathParameterNotFoundException extends RouterException {
    public PathParameterNotFoundException(String path, String pattern) {
        super("Invalid path: " + path + " for pattern: " + pattern);
    }
}
