package pl.mrugames.commons.router.request_handlers;

import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.Request;
import pl.mrugames.commons.router.Response;

@Component
public class ObjectRequestHandler implements RequestHandler<Request, Response> {

    //TODO: handle requests from different source e.g. strings (websocket), java objects (java socket)
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

        return null;
    }
}
