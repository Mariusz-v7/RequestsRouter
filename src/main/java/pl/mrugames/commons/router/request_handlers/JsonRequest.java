package pl.mrugames.commons.router.request_handlers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import pl.mrugames.commons.router.Request;
import pl.mrugames.commons.router.RequestMethod;
import pl.mrugames.commons.router.RequestType;

import java.util.Map;

/**
 * Used only for deserialization
 */
public class JsonRequest extends Request {
    private volatile String rawJson;

    @JsonCreator
    public JsonRequest(@JsonProperty("id") Long id,
                @JsonProperty("route") String route,
                @JsonProperty("requestMethod") RequestMethod requestMethod,
                @JsonProperty(value = "requestType") RequestType requestType) {
        super(id == null ? -1 : id,
                route == null ? "" : route,
                requestMethod == null ? RequestMethod.GET : requestMethod,
                null,
                requestType == null ? RequestType.STANDARD : requestType);
    }

    @Override
    public Map<String, Object> getPayload() {
        throw new UnsupportedOperationException();
    }

    public String getRawJson() {
        return rawJson;
    }

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }
}
