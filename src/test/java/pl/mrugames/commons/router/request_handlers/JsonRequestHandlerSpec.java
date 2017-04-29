package pl.mrugames.commons.router.request_handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Observable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.*;
import pl.mrugames.commons.router.controllers.UserModel;

import java.io.IOException;
import java.util.HashMap;
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

    @Before
    public void before() throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("arg1", "val1");
        payload.put("arg2", "val2");
        request = new Request(2, "1123456789012345678901234567890123456789012345678901234567890234567890", "app/test/route1", RequestMethod.GET, payload);
        jsonRequest = mapper.writeValueAsString(request);
    }

    @After
    public void after() {
        reset(mapper, requestProcessor);
    }

    @Test
    public void givenStringRequest_thenParseIntoRequestAndCallObjectHandler() throws Exception {
        handler.handleRequest(jsonRequest);
        verify(requestProcessor).standardRequest(any(), eq(request.getId()), eq(request.getSession()), eq(request.getRoute()), eq(request.getRequestMethod()), anyMap());
    }

    @Test
    public void givenObjectHandlerReturnsSomeResponse_thenJsonHandlerReturnsThatResponseAsJsonString() throws Exception {
        Response response = new Response(123, ResponseStatus.CLOSE, new UserModel("Mariusz", 0));
        String jsonResponse = mapper.writeValueAsString(response);

        doReturn(Observable.just(response)).when(requestProcessor).standardRequest(any(), anyLong(), any(), any(), any(), any());
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
}
