package pl.mrugames.commons.router.request_handlers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.Request;
import pl.mrugames.commons.router.RequestMethod;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.TestConfiguration;

import java.util.Collections;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class ObjectRequestHandlerSpec {
    @Autowired
    private ObjectRequestHandler handler;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private String generateString(int len) {
        String allowed = "abcdefgh";
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        while (len-- > 0) {
            stringBuilder.append(allowed.charAt(random.nextInt(allowed.length())));
        }

        return stringBuilder.toString();
    }

    @Test
    public void givenHandleRequestIsCalled_thenDelegateToNext() throws Exception {
        Request request1 = new Request(1, generateString(ObjectRequestHandler.SESSION_ID_MIN_LENGTH), "", RequestMethod.GET, Collections.emptyMap());
        Request request2 = new Request(2, generateString(ObjectRequestHandler.SESSION_ID_MIN_LENGTH), "", RequestMethod.POST, Collections.emptyMap());
        Response response1 = new Response(1, Response.Status.OK, "something");
        Response response2 = new Response(2, Response.Status.OK, "something");

        doReturn(response1).when(handler).next(request1);
        doReturn(response2).when(handler).next(request2);

        Response real1 = handler.handleRequest(request1);
        Response real2 = handler.handleRequest(request2);

        verify(handler).next(request1);
        verify(handler).next(request2);

        assertThat(real1).isEqualTo(response1);
        assertThat(real2).isEqualTo(response2);
    }

    @Test
    public void givenNextMethodThrowsException_whenHandleRequest_thenReturnErrorResponse() throws Exception {
        Request request = new Request(100, generateString(ObjectRequestHandler.SESSION_ID_MIN_LENGTH), "", RequestMethod.GET, Collections.emptyMap());
        doThrow(new Exception("test msg")).when(handler).next(request);

        Response response = handler.handleRequest(request);

        doCallRealMethod().when(handler).next(any());

        assertThat(response.getId()).isEqualTo(request.getId());
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_ERROR);
        assertThat((String) response.getPayload()).matches("Error: test msg, [\\S\\s]*");
    }

    @Test
    public void whenRequest_thenResponseWithSameId() throws Exception {
        Request request = new Request(100, generateString(ObjectRequestHandler.SESSION_ID_MIN_LENGTH), "app/test/concat", RequestMethod.GET, Collections.emptyMap());
        Response response = handler.next(request);

        assertThat(response.getId()).isEqualTo(request.getId());
    }

    @Test
    public void givenRequestWithSessionIdLessThan64chars_thenException() throws Exception {
        Request request = new Request(100, generateString(ObjectRequestHandler.SESSION_ID_MIN_LENGTH - 1), "", RequestMethod.GET, Collections.emptyMap());

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Session id must be at least " + ObjectRequestHandler.SESSION_ID_MIN_LENGTH + " characters long");

        handler.next(request);
    }
}
