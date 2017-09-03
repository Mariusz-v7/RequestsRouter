package pl.mrugames.commons.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BlockJUnit4ClassRunner.class)
public class JsonFrameTranslatorSpec {
    private ObjectMapper objectMapper;
    private JsonFrameTranslator jsonFrameTranslator;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
        objectMapper = new ObjectMapper();
        jsonFrameTranslator = new JsonFrameTranslator(objectMapper);
    }

    @Test
    public void whenFrameIsNull_thenException() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Failed to recognize frame: null");
        jsonFrameTranslator.recognize(null);
    }

    @Test
    public void whenRequest_thenRecognizeRequest() throws JsonProcessingException {
        Request request = new Request(1, "", "", "route", RequestMethod.POST, null);
        String str = objectMapper.writeValueAsString(request);
        assertThat(jsonFrameTranslator.recognize(str)).isEqualTo(Request.class);
    }

    @Test
    public void whenResponse_thenRecognizeResponse() throws JsonProcessingException {
        Response response = new Response(1, ResponseStatus.OK, null);
        String str = objectMapper.writeValueAsString(response);
        assertThat(jsonFrameTranslator.recognize(str)).isEqualTo(Response.class);
    }
}
