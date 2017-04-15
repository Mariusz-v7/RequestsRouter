package pl.mrugames.commons.router.request_handlers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import pl.mrugames.commons.router.Request;

import java.util.Map;

public class JsonRequest extends Request {
    @JsonCreator
    public JsonRequest(@JsonProperty("id") long id,
                       @JsonProperty("session") String session,
                       @JsonProperty("route") String route,
                       @JsonProperty("payload") Map<String, Object> payload) {
        super(id, session, route, payload);
    }
}
