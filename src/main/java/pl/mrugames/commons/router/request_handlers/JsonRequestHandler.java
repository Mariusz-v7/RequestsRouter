package pl.mrugames.commons.router.request_handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.mrugames.commons.router.Request;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.RouteInfo;
import pl.mrugames.commons.router.Router;
import pl.mrugames.commons.router.arg_resolvers.JsonPayloadArgumentResolver;

import java.util.Map;

@Component
public class JsonRequestHandler implements RequestHandler<String, String> {
    public static final String JSON_READ_ERROR_RESPONSE = "{\"id\":%d,\"status\":\"INTERNAL_ERROR\",\"payload\":\"JSON read error: %s, %s\"}";
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
    public Observable<String> handleRequest(String json) {
        Request request;
        try {
            request = mapper.readValue(json, JsonRequest.class);
        } catch (Exception e) {
            logger.error("Failed to read JSON: {}", json, e);
            return Observable.just(ErrorUtil.getErrorResponse(JSON_READ_ERROR_RESPONSE, e, -1));
        }

        Observable<Response> response;
        try {
            switch (request.getRequestType()) {
                case STANDARD:
                    RouteInfo routeInfo = router.findRoute(request.getRoute(), request.getRequestMethod());

                    String payloadJson = mapper.readTree(json).get("payload").toString();

                    Map<String, Object> payload = argResolver.resolve(payloadJson, routeInfo.getParameters());
                    response = requestProcessor.standardRequest(routeInfo, request.getId(), request.getSession(), request.getRoute(), request.getRequestMethod(), payload);
                    break;
                case CLOSE_STREAM:
                    response = requestProcessor.closeStreamRequest(request.getId(), request.getSession());
                    break;
                default:
                    throw new IllegalStateException("Unknown request type: " + request.getRequestType());
            }
        } catch (Exception e) {
            response = exceptionHandler.handle(request.getId(), e);
        }

        return response.map(r -> responseToString(r, json, request));
    }

    private String responseToString(Response response, String json, Request request) {
        try {
            return mapper.writeValueAsString(response);
        } catch (Exception e) {
            logger.error("Failed to write to JSON: {}, {}", json, response, e);
            return ErrorUtil.getErrorResponse(JSON_MAPPING_ERROR_RESPONSE, e, request.getId());
        }
    }
}
