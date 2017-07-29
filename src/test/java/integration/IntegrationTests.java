package integration;

import io.reactivex.observers.TestObserver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.client.Client;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        Cfg.class
})
public class IntegrationTests {
    @Autowired
    private Client client;

    @Test
    public void shouldNavigateToProperRouteAndReturnProperResult() {
        TestObserver<Object> testObserver = client.send("integration", Collections.singletonMap("a", 1))
                .response.test();

        testObserver.awaitTerminalEvent();
        testObserver.awaitDone(1, TimeUnit.DAYS);
        testObserver.assertValue(true);
        testObserver.assertComplete();
    }
}
