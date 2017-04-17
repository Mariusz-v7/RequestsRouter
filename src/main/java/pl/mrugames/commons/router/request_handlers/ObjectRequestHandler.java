package pl.mrugames.commons.router.request_handlers;

import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.Request;
import pl.mrugames.commons.router.Response;

@Component
public class ObjectRequestHandler implements RequestHandler<Request, Response> {

    @Override
    public Response handleRequest(Request request) { // TODO: this method should never throw exception - it should translate all exceptions into error response!
        // if exception, then return response error
        /*
        request = {
            id: long
            session: string 64,
            route: String
            payload: {
                ...
            }
        }
         */

        /*
        todo:
        1. response id same as request id
        2. check whether session has valid length and whether it exists
        3. if session is invalid, then session parameters = emptyMap()
         */

        try {
            return next(request);
        } catch (Exception e) {
            return new Response(request.getId(), Response.Status.INTERNAL_ERROR, String.format("Error: %s, %s", e.getMessage(), ErrorUtil.exceptionStackTraceToString(e)));
        }
    }

    Response next(Request request) throws Exception {

        return null;
    }
}
