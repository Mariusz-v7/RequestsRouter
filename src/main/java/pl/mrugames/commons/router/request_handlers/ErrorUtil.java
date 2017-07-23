package pl.mrugames.commons.router.request_handlers;

import java.io.PrintWriter;
import java.io.StringWriter;

class ErrorUtil {
    static String getErrorResponse(String s, Throwable e, long requestId) {
        return String.format(s, requestId, e.getMessage(), exceptionStackTraceToString(e));
    }

    static String exceptionStackTraceToString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
