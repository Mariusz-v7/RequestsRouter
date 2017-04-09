package pl.mrugames.commons.router;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.controllers.TestController;
import pl.mrugames.commons.router.controllers.UserModel;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    public void shouldNavigateToRoute1() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        Object result = router.route("app/test/route1", RequestMethod.GET, Collections.emptyMap(), Collections.emptyMap());
        assertThat(result).isEqualTo(controller.route1());
    }

    @Test
    public void shouldNavigateToRouteWithPost() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        Object result = router.route("app/test/route1", RequestMethod.POST, Collections.emptyMap(), Collections.emptyMap());
        assertThat(result).isEqualTo(controller.route1WithPost());
    }

    @Test
    public void shouldResolvePathVariable() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        Object result = router.route("app/test/player/26", RequestMethod.GET, Collections.emptyMap(), Collections.emptyMap());
        assertThat(result).isEqualTo(27);
    }

    @Test
    public void shouldResolveBothPathVariables() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Object result = router.route("app/test/player/26/add/0.1", RequestMethod.GET, Collections.emptyMap(), Collections.emptyMap());
        assertThat(result).isEqualTo(26.1);
    }

    @Test
    public void shouldResolveArgs() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Map<String, Object> params = new HashMap<>();
        params.put("a", 1);
        params.put("b", "- HELLO - ");
        params.put("c", 2.4);
        params.put("d", " bye");
        Object result = router.route("app/test/concat", RequestMethod.GET, params, Collections.emptyMap());

        assertThat(result).isEqualTo("1- HELLO - 2.4 bye");
    }

    @Test
    public void shouldResolveArgsAndDefaultArg() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Map<String, Object> params = new HashMap<>();
        params.put("a", 1);
        params.put("b", "- HELLO - ");
        params.put("c", 2.4);
        Object result = router.route("app/test/concat", RequestMethod.GET, params, Collections.emptyMap());

        assertThat(result).isEqualTo("1- HELLO - 2.4last");
    }

    @Test
    public void shouldResolveSessionParameters() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Map<Class<?>, Object> params = new HashMap<>();
        params.put(UserModel.class, new UserModel("Natalia"));

        Object result = router.route("app/test/account/username", RequestMethod.GET, Collections.emptyMap(), params);

        assertThat(result).isEqualTo("Natalia");
    }
}
