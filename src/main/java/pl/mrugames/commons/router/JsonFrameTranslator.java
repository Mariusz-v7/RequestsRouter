package pl.mrugames.commons.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import pl.mrugames.commons.router.request_handlers.JsonRequest;
import pl.mrugames.commons.router.request_handlers.JsonResponse;

import java.io.IOException;
import java.io.Serializable;

@Service
public class JsonFrameTranslator implements FrameTranslator {
    private final ObjectMapper objectMapper;

    JsonFrameTranslator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    Class<?> recognize(String frame) {
        try {
            if (frame != null) {
                JsonNode node = objectMapper.readTree(frame);
                if (node.has("route") && node.has("requestMethod")) {
                    return Request.class;
                }

                if (node.has("status")) {
                    return Response.class;
                }
            }

            throw new IllegalArgumentException("Failed to recognize frame: " + frame);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to recognize frame: " + frame, e);
        }
    }

    @Override
    public Object translateToRequestOrResponse(Serializable serializable) {
        String json = (String) serializable;
        Class<?> type = recognize(json);

        try {
            if (type.equals(Response.class)) {
                return objectMapper.readValue(json, JsonResponse.class);
            }

            if (type.equals(Request.class)) {
                JsonRequest jsonRequest = objectMapper.readValue(json, JsonRequest.class);
                jsonRequest.setRawJson(json);
                return jsonRequest;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to serialize frame", e);
        }

        throw new IllegalArgumentException("Unknown frame type: " + serializable);
    }

    @Override
    public Serializable translateFromRequest(Request request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize request", e);
        }
    }
}
