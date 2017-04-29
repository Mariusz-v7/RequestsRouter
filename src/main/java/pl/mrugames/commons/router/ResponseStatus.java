package pl.mrugames.commons.router;

public enum ResponseStatus {
    OK, INTERNAL_ERROR, ERROR, STREAM, CLOSE,
    NOT_AUTHORIZED, ONLY_FOR_NOT_AUTHORIZED, PERMISSION_DENIED,
    BAD_REQUEST
}
