package pl.mrugames.commons.router.request_handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.Request;
import pl.mrugames.commons.router.RequestMethod;
import pl.mrugames.commons.router.RequestType;
import pl.mrugames.commons.router.TestConfiguration;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class JsonRequestSpec {
    @Autowired
    private ObjectMapper mapper;

    @Test
    public void givenRequestWithoutRequestType_whenProcess_thenRequestTypeIsStandard() throws IOException {
        Request request = new Request(2, "some/route", RequestMethod.GET, Collections.emptyMap());
        String jsonRequest = mapper.writeValueAsString(request);

        Request result = mapper.readValue(jsonRequest, JsonRequest.class);
        assertThat(result.getRequestType()).isEqualTo(RequestType.STANDARD);
    }

    @Test
    public void givenRequestWithRequestTypeSet_whenProcess_thenRequestTypeIsValid() throws IOException {
        Request request = new Request(2, "some/route", RequestMethod.GET, Collections.emptyMap(), RequestType.CLOSE_STREAM);
        String jsonRequest = mapper.writeValueAsString(request);

        Request result = mapper.readValue(jsonRequest, JsonRequest.class);
        assertThat(result.getRequestType()).isEqualTo(RequestType.CLOSE_STREAM);
    }

    @Test
    public void testNotRequiredFields() throws IOException {
        Request request = new Request(2, "", RequestMethod.GET, null, RequestType.CLOSE_STREAM);
        String jsonRequest = "{\"id\":2,\"requestType\":\"CLOSE_STREAM\"}";

        Request result = mapper.readValue(jsonRequest, JsonRequest.class);
        assertThat(result).isEqualTo(request);
    }

    @Test
    public void testDefaultRequestType() throws IOException {
        String jsonRequest = "{\"id\":2,\"session\":\"asdfgh\"}";

        Request result = mapper.readValue(jsonRequest, JsonRequest.class);
        assertThat(result.getRequestType()).isEqualTo(RequestType.STANDARD);
    }

    @Test
    public void whenNoRequestMethod_thenDefaultToGET() throws IOException {
        String jsonRequest = "{\"id\":2}";

        Request result = mapper.readValue(jsonRequest, JsonRequest.class);
        assertThat(result.getRequestMethod()).isEqualTo(RequestMethod.GET);
    }
}
