package pl.mrugames.commons.router.request_handlers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.ResponseStatus;

/**
 * Used only for deserialization
 */
public class JsonResponse extends Response {
    @JsonCreator
    public JsonResponse(@JsonProperty("id") long id,
                        @JsonProperty("status") ResponseStatus status,
                        @JsonProperty("payload") Object payload) {
        super(id, status, payload);
    }
}
