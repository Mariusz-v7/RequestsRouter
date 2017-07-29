package pl.mrugames.commons.router.client;

import pl.mrugames.commons.router.ResponseStatus;

public class ErrorResponseException extends RuntimeException {
    private final ResponseStatus responseStatus;

    public ErrorResponseException(ResponseStatus responseStatus, String message) {
        super(message);
        this.responseStatus = responseStatus;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorResponseException)) return false;

        ErrorResponseException that = (ErrorResponseException) o;

        return responseStatus == that.responseStatus;

    }

    @Override
    public int hashCode() {
        return responseStatus != null ? responseStatus.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ErrorResponseException{" +
                "message=" + getMessage() +
                "responseStatus=" + responseStatus +
                '}';
    }
}
