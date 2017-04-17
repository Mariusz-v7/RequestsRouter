package pl.mrugames.commons.router.request_handlers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.Request;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.TestConfiguration;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class ObjectRequestHandlerSpec {
    @Autowired
    private ObjectRequestHandler handler;

    @Test
    public void givenHandleRequestIsCalled_thenDelegateToNext() throws Exception {
        Request request1 = new Request(1, "", "", Collections.emptyMap());
        Request request2 = new Request(2, "", "", Collections.emptyMap());
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
        Request request = new Request(100, "", "", Collections.emptyMap());
        doThrow(new Exception("test msg")).when(handler).next(request);

        Response response = handler.handleRequest(request);

        assertThat(response.getId()).isEqualTo(request.getId());
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_ERROR);
        assertThat((String) response.getPayload()).matches("Error: test msg, [\\S\\s]*");
    }
}
