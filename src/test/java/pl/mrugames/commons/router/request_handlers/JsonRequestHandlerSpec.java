package pl.mrugames.commons.router.request_handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Observable;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.*;
import pl.mrugames.commons.router.controllers.UserModel;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class JsonRequestHandlerSpec {
    @Autowired
    private JsonRequestHandler handler;

    @Autowired
    private RequestProcessor requestProcessor;

    @Autowired
    private ObjectMapper mapper;

    private Request request;
    private String jsonRequest;
    private Map<String, Object> payload = new HashMap<>();

    @Before
    public void before() throws JsonProcessingException {
        payload.put("arg1", "val1");
        payload.put("arg2", "val2");
        request = new Request(2, "1123456789012345678901234567890123456789012345678901234567890234567890", "", "app/test/json", RequestMethod.GET, payload);
        jsonRequest = mapper.writeValueAsString(request);
    }

    @After
    public void after() {
        reset(mapper, requestProcessor);
    }

    private String prepareJsonRequest(String route, String payload) {
        return String.format(
                "{\"id\":2," +
                        "\"session\":\"1123456789012345678901234567890123456789012345678901234567890234567890\"," +
                        "\"route\":\"%s\"," +
                        "\"requestMethod\":\"GET\"," +
                        "\"payload\":{%s}," +
                        "\"requestType\":\"STANDARD\"}",
                route, payload);
    }

    @Test
    public void givenStringRequest_thenParseIntoRequestAndCallObjectHandler() throws Exception {
        handler.handleRequest(jsonRequest);
        verify(requestProcessor).standardRequest(any(), eq(request.getId()), eq(request.getSession()), eq(""), eq(request.getRoute()), eq(request.getRequestMethod()), anyMap());
    }

    @Test
    public void givenObjectHandlerReturnsSomeResponse_thenJsonHandlerReturnsThatResponseAsJsonString() throws Exception {
        Response response = new Response(123, ResponseStatus.CLOSE, new UserModel("Mariusz", 0));
        String jsonResponse = mapper.writeValueAsString(response);

        doReturn(Observable.just(response))
                .when(requestProcessor)
                .standardRequest(any(), anyLong(), any(), any(), any(), any(), any());
        String realResponse = handler.handleRequest(jsonRequest).blockingFirst();

        assertThat(realResponse).isEqualTo(jsonResponse);
    }

    @Test
    public void givenObjectMapperThrowsError_thenReturnReadyErrorResponse() throws JsonProcessingException {
        doThrow(new RuntimeException("mapping exception")).when(mapper).writeValueAsString(any());
        String realResponse = handler.handleRequest(jsonRequest).blockingFirst();

        assertThat(realResponse).matches(
                String.format("\\" + JsonRequestHandler.JSON_MAPPING_ERROR_RESPONSE, request.getId(), "mapping exception", "[\\s\\S]*")
        );
    }

    @Test
    public void givenObjectMapperThrowExceptionOnReadingRequest_thenSendBackErrorMessage() throws IOException {
        doThrow(new RuntimeException("failed to read")).when(mapper).readValue(anyString(), any(Class.class));
        String realResponse = handler.handleRequest(jsonRequest).blockingFirst();

        assertThat(realResponse).matches(
                String.format("\\" + JsonRequestHandler.JSON_READ_ERROR_RESPONSE, -1, "failed to read", "[\\s\\S]*")
        );
    }

    @Test
    public void payloadResolverTest() throws InvocationTargetException, IllegalAccessException {
        handler.handleRequest(jsonRequest).blockingFirst();
        verify(requestProcessor).standardRequest(any(), anyLong(), anyString(), anyString(), anyString(), any(), eq(payload));
    }

    @Test
    public void givenRequestHasMissingPayloadArguments_thenReturnBadRequestResponse() throws IOException {
        String req = prepareJsonRequest("app/test/json", "");
        String realResponse = handler.handleRequest(req).blockingFirst();

        Response response = mapper.readValue(realResponse, JsonResponse.class);

        assertThat(response.getStatus()).isEqualTo(ResponseStatus.BAD_REQUEST);
        assertThat(response.getPayload()).isEqualTo("Could not find 'arg1' parameter in the request");
    }

    @Test
    public void givenRequestWithoutId_thenResponseErrorWithIdMinusOne() throws IOException {
        String req = String.format(
                "{\"session\":\"1123456789012345678901234567890123456789012345678901234567890234567890\"," +
                        "\"route\":\"%s\"," +
                        "\"requestMethod\":\"GET\"," +
                        "\"payload\":{}," +
                        "\"requestType\":\"STANDARD\"}",
                "some/route");

        String realResponse = handler.handleRequest(req).blockingFirst();
        Response response = mapper.readValue(realResponse, JsonResponse.class);

        assertThat(response.getId()).isEqualTo(-1);
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.BAD_REQUEST);
        assertThat(response.getPayload()).isEqualTo("'id' is missing n the request");
    }

    @Test
    public void givenRequestWithDamagedRequestMethod_thenResponseError() throws IOException {
        String req = String.format(
                "{\"id\":99," +
                        "\"session\":\"1123456789012345678901234567890123456789012345678901234567890234567890\"," +
                        "\"route\":\"%s\"," +
                        "\"requestMethod\":\"GE\"," +
                        "\"payload\":{}," +
                        "\"requestType\":\"STANDARD\"}",
                "some/route");

        String realResponse = handler.handleRequest(req).blockingFirst();
        Response response = mapper.readValue(realResponse, JsonResponse.class);

        assertThat(response.getId()).isEqualTo(99);
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.BAD_REQUEST);
        assertThat((String) response.getPayload()).contains("Can not deserialize value of type pl.mrugames.commons.router.RequestMethod from String \"GE\": value not one of declared Enum instance names: [POST, DELETE, GET, PUT, PATCH]");
    }

    @Test
    @SuppressWarnings("unchecked")
    @Ignore
    // TODO: validation will be done by spring. Add test when method invocation throws Constraint violation cause
    public void givenRequestViolatingConstraints_thenErrorResponse() throws IOException {
        String req = prepareJsonRequest("app/test/validation2", "\"a\":-1,\"b\":10");
        String realResponse = handler.handleRequest(req).blockingFirst();
        Response response = mapper.readValue(realResponse, JsonResponse.class);

        assertThat(response.getId()).isEqualTo(2);
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.BAD_PARAMETERS);
        assertThat((List<String>) response.getPayload()).contains(
                "b must be less than or equal to 2",
                "a must be greater than or equal to 0"
        );
    }
}
