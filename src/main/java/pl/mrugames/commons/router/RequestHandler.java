package pl.mrugames.commons.router;

import org.springframework.stereotype.Component;

@Component
public class RequestHandler {
    private final Router router;

    RequestHandler(Router router) {
        this.router = router;
    }

    //TODO: handle requests from different source e.g. strings (websocket), java objects (java socket)
    public Response handleRequest(Request request) {
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
