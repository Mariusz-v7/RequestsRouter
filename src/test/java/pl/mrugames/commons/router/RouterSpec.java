package pl.mrugames.commons.router;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.controllers.UserModel;
import pl.mrugames.commons.router.exceptions.RouteConstraintViolationException;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class RouterSpec {
    @Autowired
    private Router router;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenFindRoute_thenReturnProperRouteInfo() {
        RouteInfo routeInfo = router.findRoute("app/test/route1", RequestMethod.GET);
        assertThat(routeInfo.getParameters()).isEmpty();
        assertThat(routeInfo.getRoutePattern()).isEqualTo("GET:app/test/route1");
    }

    @Test
    public void givenNoRoute_whenFindRoute_thenException() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Route not found: DELETE:xxx");

        router.findRoute("xxx", RequestMethod.DELETE);
    }

    @Test
    public void givenRouteWithoutArguments_whenNavigate_thenReturnValueFromController() throws InvocationTargetException, IllegalAccessException {
        RouteInfo routeInfo = router.findRoute("app/test/route1", RequestMethod.GET);

        Object result = router.navigate(routeInfo, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        assertThat(result).isEqualTo("route1");
    }

    @Test
    public void givenRouteWithPathArgs_whenNavigate_thenExtractParameters() throws InvocationTargetException, IllegalAccessException {
        RouteInfo routeInfo = router.findRoute("app/test/player/10", RequestMethod.GET);

        Object result = router.navigate(routeInfo, Collections.singletonMap("playerId", 10), Collections.emptyMap(), Collections.emptyMap());
        assertThat(result).isEqualTo(11);
    }

    @Test
    public void givenRouteWithPathArg_whenNavigateWithMissingParameter_thenException() throws InvocationTargetException, IllegalAccessException {
        RouteInfo routeInfo = router.findRoute("app/test/player/10", RequestMethod.GET);

        expectedException.expect(IllegalArgumentException.class);
        router.navigate(routeInfo, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Test
    public void givenRouteWithArgs_whenRequest_thenResolveArgs() throws InvocationTargetException, IllegalAccessException {
        RouteInfo routeInfo = router.findRoute("app/test/concat", RequestMethod.GET);
        Map<String, Object> payload = new HashMap<>();
        payload.put("a", 6);
        payload.put("b", "a string");
        payload.put("c", 0.2);
        payload.put("d", "end");

        Object result = router.navigate(routeInfo, Collections.emptyMap(), payload, Collections.emptyMap());

        assertThat(result).isEqualTo("6a string0.2end");
    }

    @Test
    public void givenRouteWithArgsAndDefaultValue_whenRequestWithoutDefaultArguments_thenResolveDefaults() throws InvocationTargetException, IllegalAccessException {
        RouteInfo routeInfo = router.findRoute("app/test/concat", RequestMethod.GET);
        Map<String, Object> payload = new HashMap<>();
        payload.put("a", 6);
        payload.put("b", "a string");
        payload.put("c", 0.2);

        Object result = router.navigate(routeInfo, Collections.emptyMap(), payload, Collections.emptyMap());

        assertThat(result).isEqualTo("6a string0.2last");
    }

    @Test
    public void givenRouteWithArgs_whenRequestWithoutArgs_thenException() throws InvocationTargetException, IllegalAccessException {
        RouteInfo routeInfo = router.findRoute("app/test/concat", RequestMethod.GET);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Missing argument: a");

        router.navigate(routeInfo, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Test
    public void givenRouteWithSessionArgs_whenRequest_thenArgumentsAreResolved() throws InvocationTargetException, IllegalAccessException {
        RouteInfo routeInfo = router.findRoute("app/test/account/username", RequestMethod.GET);
        Map<Class<?>, Optional<Object>> session = new HashMap<>();
        session.put(UserModel.class, Optional.of(new UserModel("Mariusz", 1)));

        Object result = router.navigate(routeInfo, Collections.emptyMap(), Collections.emptyMap(), session);

        assertThat(result).isEqualTo("Mariusz");
    }

    @Test
    public void givenRouteWithSessionArgs_whenSessionDoesNotHaveObject_thenResolveWithNull() throws InvocationTargetException, IllegalAccessException {
        RouteInfo routeInfo = router.findRoute("app/test/re-return-obj", RequestMethod.GET);
        Map<Class<?>, Optional<Object>> session = new HashMap<>();
        session.put(UserModel.class, Optional.empty());

        Object result = router.navigate(routeInfo, Collections.emptyMap(), Collections.emptyMap(), session);

        assertThat(result).isNull();
    }

    @Test
    public void givenRouteWithSessionArgs_whenSessionDoesNotHavePrimitive_thenResolveWithDefault() throws InvocationTargetException, IllegalAccessException {
        RouteInfo routeInfo = router.findRoute("app/test/re-return-int", RequestMethod.GET);
        Map<Class<?>, Optional<Object>> session = new HashMap<>();
        session.put(int.class, Optional.empty());
        session.put(boolean.class, Optional.empty());

        Object result = router.navigate(routeInfo, Collections.emptyMap(), Collections.emptyMap(), session);

        assertThat(result).isEqualTo(0);

        routeInfo = router.findRoute("app/test/re-return-bool", RequestMethod.GET);
        result = router.navigate(routeInfo, Collections.emptyMap(), Collections.emptyMap(), session);
        assertThat(result).isEqualTo(false);
    }

    @Test
    public void validationTest() throws InvocationTargetException, IllegalAccessException {
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("a", -1);
        pathParams.put("b", 3);

        RouteInfo routeInfo = router.findRoute("app/test/validation/-1/3", RequestMethod.GET);

        expectedException.expect(RouteConstraintViolationException.class);

        try {
            router.navigate(routeInfo, pathParams, Collections.emptyMap(), Collections.emptyMap());
        } catch (RouteConstraintViolationException e) {
            assertThat(e.getMessages()).containsExactlyInAnyOrder(
                    "a: must be greater than or equal to 0",
                    "b: must be less than or equal to 2"
            );

            throw e;
        }
    }

    @Test
    @WithMockUser("admin")
    public void givenMethodHasDenyAllAnnotation_andUserIsLogged_whenNavigate_thenException() throws IllegalAccessException {
        RouteInfo routeInfo = router.findRoute("app/test/deny", RequestMethod.GET);

        expectedException.expect(AccessDeniedException.class);

        router.navigate(routeInfo, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Test
    public void givenControllerIsNotValidated_butServiceIs_whenPassWrongArgs_thenExceptionWithProperMessage() throws IllegalAccessException {
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("a", -1);

        RouteInfo routeInfo = router.findRoute("app/test/validate-deeper/-1", RequestMethod.GET);

        expectedException.expect(RouteConstraintViolationException.class);

        try {
            router.navigate(routeInfo, pathParams, Collections.emptyMap(), Collections.emptyMap());
        } catch (RouteConstraintViolationException e) {
            assertThat(e.getMessages()).containsExactly(
                    "value '-1' must be greater than or equal to 0"
            );

            throw e;
        }
    }

    @Test
    public void givenNestedServiceIsValidated_whenPassWrongArguments_thenException() throws IllegalAccessException {
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("a", 1);
        pathParams.put("b", -1);

        RouteInfo routeInfo = router.findRoute("app/test/validate-deeper2/1/-1", RequestMethod.GET);

        expectedException.expect(RouteConstraintViolationException.class);

        try {
            router.navigate(routeInfo, pathParams, Collections.emptyMap(), Collections.emptyMap());
        } catch (RouteConstraintViolationException e) {
            assertThat(e.getMessages()).containsExactlyInAnyOrder(
                    "value '1' must be less than or equal to 0",
                    "value '-1' must be greater than or equal to 0"
            );

            throw e;
        }
    }
}
