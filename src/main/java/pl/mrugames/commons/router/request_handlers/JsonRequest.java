package pl.mrugames.commons.router.request_handlers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import pl.mrugames.commons.router.Request;
import pl.mrugames.commons.router.RequestMethod;
import pl.mrugames.commons.router.RequestType;

import java.util.Map;

class JsonRequest extends Request {
    @JsonCreator
    JsonRequest(@JsonProperty("id") Long id,
                @JsonProperty("session") String session,
                @JsonProperty("securityCode") String securityCode,
                @JsonProperty("route") String route,
                @JsonProperty("requestMethod") RequestMethod requestMethod,
                @JsonProperty(value = "requestType") RequestType requestType) {
        super(id == null ? -1 : id,
                session == null ? "" : session,
                securityCode == null ? "" : securityCode,
                route == null ? "" : route,
                requestMethod == null ? RequestMethod.GET : requestMethod,
                null,
                requestType == null ? RequestType.STANDARD : requestType);
    }

    @Override
    public Map<String, Object> getPayload() {
        throw new UnsupportedOperationException();
    }
}
