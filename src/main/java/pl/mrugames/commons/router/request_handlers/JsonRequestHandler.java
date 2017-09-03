package pl.mrugames.commons.router.request_handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.RouteInfo;
import pl.mrugames.commons.router.Router;
import pl.mrugames.commons.router.arg_resolvers.JsonPayloadArgumentResolver;

import java.util.Map;

@Component
public class JsonRequestHandler implements RequestHandler<JsonRequest, String> {
    public static final String JSON_READ_ERROR_RESPONSE = "{\"id\":%d,\"status\":\"INTERNAL_ERROR\",\"payload\":\"JSON read error: %s, %s\"}"; //todo: build json using json mapper
    public static final String JSON_MAPPING_ERROR_RESPONSE = "{\"id\":%d,\"status\":\"INTERNAL_ERROR\",\"payload\":\"JSON mapping error: %s, %s\"}";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ObjectMapper mapper;
    private final Router router;
    private final RequestProcessor requestProcessor;
    private final JsonPayloadArgumentResolver argResolver;
    private final ExceptionHandler exceptionHandler;

    JsonRequestHandler(ObjectMapper mapper,
                       Router router,
                       RequestProcessor requestProcessor,
                       JsonPayloadArgumentResolver argResolver,
                       ExceptionHandler exceptionHandler) {
        this.mapper = mapper;
        this.router = router;
        this.requestProcessor = requestProcessor;
        this.argResolver = argResolver;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public Observable<String> handleRequest(JsonRequest jsonRequest) {
        Observable<Response> response;
        try {
            if (jsonRequest.getId() == -1) {
                throw new IllegalArgumentException("'id' is missing in the request");
            }

            switch (jsonRequest.getRequestType()) {
                case STANDARD:
                    RouteInfo routeInfo = router.findRoute(jsonRequest.getRoute(), jsonRequest.getRequestMethod());

                    String payloadJson = mapper.readTree(jsonRequest.getRawJson()).get("payload").toString();

                    Map<String, Object> payload = argResolver.resolve(payloadJson, routeInfo.getParameters());
                    response = requestProcessor.standardRequest(routeInfo,
                            jsonRequest.getId(),
                            jsonRequest.getSession(),
                            jsonRequest.getSecurityCode(),
                            jsonRequest.getRoute(),
                            jsonRequest.getRequestMethod(),
                            payload);
                    break;
                case CLOSE_STREAM:
                    response = requestProcessor.closeStreamRequest(jsonRequest.getId(), jsonRequest.getSession(), jsonRequest.getSecurityCode());
                    break;
                default:
                    throw new IllegalStateException("Unknown request type: " + jsonRequest.getRequestType());
            }
        } catch (Exception e) {
            response = Observable.just(exceptionHandler.handle(jsonRequest.getId(), e));
        }

        return response.map(r -> responseToString(r, jsonRequest.getRawJson(), jsonRequest.getId()));
    }

    private String responseToString(Response response, String json, long requestId) {
        try {
            return mapper.writeValueAsString(response);
        } catch (Exception e) {
            logger.error("Failed to write to JSON: {}, {}", json, response, e);
            return ErrorUtil.getErrorResponse(JSON_MAPPING_ERROR_RESPONSE, e, requestId);
        }
    }
}
