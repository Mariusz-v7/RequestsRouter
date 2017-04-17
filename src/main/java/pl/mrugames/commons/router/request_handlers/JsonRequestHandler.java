package pl.mrugames.commons.router.request_handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.Request;
import pl.mrugames.commons.router.Response;

@Component
public class JsonRequestHandler implements RequestHandler<String, String> {
    public static final String JSON_READ_ERROR_RESPONSE = "{\"id\":%d,\"status\":\"INTERNAL_ERROR\",\"payload\":\"JSON read error: %s, %s\"}";
    public static final String JSON_MAPPING_ERROR_RESPONSE = "{\"id\":%d,\"status\":\"INTERNAL_ERROR\",\"payload\":\"JSON mapping error: %s, %s\"}";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ObjectMapper mapper;
    private final ObjectRequestHandler requestHandler;

    JsonRequestHandler(ObjectMapper mapper, ObjectRequestHandler requestHandler) {
        this.mapper = mapper;
        this.requestHandler = requestHandler;
    }

    @Override
    public String handleRequest(String json) {
        Request request;
        try {
            request = mapper.readValue(json, JsonRequest.class);
        } catch (Exception e) {
            logger.error("Failed to read JSON: {}", json, e);
            return ErrorUtil.getErrorResponse(JSON_READ_ERROR_RESPONSE, e, -1);
        }

        Response response = requestHandler.handleRequest(request);

        try {
            return mapper.writeValueAsString(response);
        } catch (Exception e) {
            logger.error("Failed to write to JSON: {}, {}", json, response, e);
            return ErrorUtil.getErrorResponse(JSON_MAPPING_ERROR_RESPONSE, e, request.getId());
        }
    }
}
