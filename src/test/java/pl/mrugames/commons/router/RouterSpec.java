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
public class RouterSpec {
    @Autowired
    private Router router;

    @Autowired
    private TestController controller;

    @Test
    public void shouldNavigateToRoute1() throws InvocationTargetException, IllegalAccessException {
        Object result = router.route("app/test/route1", RequestMethod.GET);
        assertThat(result).isEqualTo(controller.route1());
    }

    @Test
    public void shouldNavigateToRouteWithPost() throws InvocationTargetException, IllegalAccessException {
        Object result = router.route("app/test/route1", RequestMethod.POST);
        assertThat(result).isEqualTo(controller.route1WithPost());
    }

}
