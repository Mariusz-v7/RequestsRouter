package pl.mrugames.commons.router;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.controllers.TestController;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class RouterInitializerSpec {
    @Autowired
    private RouterInitializer initializer;

    @Autowired
    private TestController testController;

    @Test
    public void givenApplicationStart_thenInitializerIsNotNull() {
        assertThat(initializer).isNotNull();
    }

    @Test
    public void givenApplicationStart_thenTestControllerIsNotNull() {
        assertThat(testController).isNotNull();
    }

    @Test
    public void givenApplicationStart_thenControllersContainsTestController() {
        assertThat(initializer.getControllers()).contains("app/test");
    }

    @Test
    public void givenApplicationStart_thenMapShouldContainRouteToTestController() {
        assertThat(initializer.getRoutes()).containsKeys("app/test/route1");
    }

    @Test
    public void givenApplicaitonStart_whenInvokeFirstRoute_thenItShouldReturnValueFromController() throws InvocationTargetException, IllegalAccessException {
        RouteInfo routeInfo = initializer.getRoutes().get("app/test/route1");
        assertThat(routeInfo.getMethod().invoke(routeInfo.getControllerInstance())).isEqualTo("route1");
    }
}
