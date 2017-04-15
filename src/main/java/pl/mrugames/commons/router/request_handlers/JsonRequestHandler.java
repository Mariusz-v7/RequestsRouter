package pl.mrugames.commons.router.request_handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.Request;
import pl.mrugames.commons.router.Response;

import java.io.PrintWriter;
import java.io.StringWriter;

@Component
public class JsonRequestHandler implements RequestHandler<String, String> {
    public static final String JSON_READ_ERROR_RESPONSE = "{\"id\":%d,\"status\":\"ERROR\",\"payload\":\"JSON read error: %s, %s\"}";
    public static final String JSON_MAPPING_ERROR_RESPONSE = "{\"id\":%d,\"status\":\"ERROR\",\"payload\":\"JSON mapping error: %s, %s\"}";

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
            return getErrorResponse(JSON_READ_ERROR_RESPONSE, e, -1);
        }

        Response response = requestHandler.handleRequest(request);

        try {
            return mapper.writeValueAsString(response);
        } catch (Exception e) {
            logger.error("Failed to write to JSON: {}, {}", json, response, e);
            return getErrorResponse(JSON_MAPPING_ERROR_RESPONSE, e, request.getId());
        }
    }

    private String getErrorResponse(String s, Exception e, long requestId) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return String.format(s, requestId, e.getMessage(), sw.toString());
    }
}
