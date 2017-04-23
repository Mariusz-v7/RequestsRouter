package pl.mrugames.commons.router.request_handlers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import pl.mrugames.commons.router.Request;
import pl.mrugames.commons.router.RequestMethod;
import pl.mrugames.commons.router.RequestType;

import java.util.Map;

class JsonRequest extends Request {
    @JsonCreator
    JsonRequest(@JsonProperty("id") long id,
                @JsonProperty("session") String session,
                @JsonProperty("route") String route,
                @JsonProperty("requestMethod") RequestMethod requestMethod,
                @JsonProperty("payload") Map<String, Object> payload,
                @JsonProperty(value = "requestType", defaultValue = "STANDARD") RequestType requestType) {
        super(id, session, route, requestMethod, payload, requestType);
    }
}
