package pl.mrugames.commons.router;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith(BlockJUnit4ClassRunner.class)
public class RequestHandlerSpec {
    private Router router;
    private RequestHandler requestHandler;

    @Before
    public void before() {
        router = mock(Router.class);
        requestHandler = spy(new RequestHandler(router));
    }
}
