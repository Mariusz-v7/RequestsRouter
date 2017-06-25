package pl.mrugames.commons.router.arg_resolvers;

import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.RouteInfo;
import pl.mrugames.commons.router.RouterInitializer;
import pl.mrugames.commons.router.TestConfiguration;
import pl.mrugames.commons.router.controllers.UserModel;
import pl.mrugames.commons.router.exceptions.IncompatibleParameterException;
import pl.mrugames.commons.router.exceptions.ParameterNotFoundException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class RequestPayloadArgumentResolverSpec {
    @Autowired
    private RequestPayloadArgumentResolver resolver;

    @Autowired
    private RouterInitializer initializer;

    private Map<String, RouteInfo> routes;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
        routes = initializer.getRoutes();
    }

    @Test
    public void givenMethodWithOneArgAnnotation_thenReturnValueFromPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("a", 1);
        payload.put("b", "str");
        payload.put("c", 12.1);
        payload.put("d", "xxx");

        RouteInfo routeInfo = routes.get("GET:app/test/concat");

        Map<String, Object> result = resolver.resolve(payload, routeInfo.getParameters());

        assertThat(result).containsExactly(
                MapEntry.entry("a", 1),
                MapEntry.entry("b", "str"),
                MapEntry.entry("c", 12.1),
                MapEntry.entry("d", "xxx")
        );
    }

    @Test
    public void givenRequestContainsNull_whenResolve_thenPassNull() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("a", 1);
        payload.put("b", null);
        payload.put("c", 12.1);
        payload.put("d", "xxx");

        RouteInfo routeInfo = routes.get("GET:app/test/concat");

        Map<String, Object> result = resolver.resolve(payload, routeInfo.getParameters());

        assertThat(result).containsExactly(
                MapEntry.entry("a", 1),
                MapEntry.entry("b", null),
                MapEntry.entry("c", 12.1),
                MapEntry.entry("d", "xxx")
        );
    }

    @Test
    public void givenRequestWithAdditionalPayloadFields_thenIgnoreThem() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("a", 1);
        payload.put("b", "str");
        payload.put("c", 12.1);
        payload.put("d", "xxx");
        payload.put("additional", "yyy");

        RouteInfo routeInfo = routes.get("GET:app/test/concat");

        Map<String, Object> result = resolver.resolve(payload, routeInfo.getParameters());

        assertThat(result).containsExactly(
                MapEntry.entry("a", 1),
                MapEntry.entry("b", "str"),
                MapEntry.entry("c", 12.1),
                MapEntry.entry("d", "xxx")
        );
    }

    @Test
    public void givenMethodHasDefaultArguments_whenRequestLacksThem_thenUseDefault() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("a", 1);
        payload.put("b", "str");
        payload.put("c", 12.1);

        RouteInfo routeInfo = routes.get("GET:app/test/concat");

        Map<String, Object> result = resolver.resolve(payload, routeInfo.getParameters());

        assertThat(result).containsExactly(
                MapEntry.entry("a", 1),
                MapEntry.entry("b", "str"),
                MapEntry.entry("c", 12.1),
                MapEntry.entry("d", "last")
        );
    }

    @Test
    public void givenRequestDoesNotHaveAllArguments_thenException() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("a", 1);

        RouteInfo routeInfo = routes.get("GET:app/test/concat");

        expectedException.expect(ParameterNotFoundException.class);
        expectedException.expectMessage("Could not find 'b' parameter in the request");

        resolver.resolve(payload, routeInfo.getParameters());
    }

    @Test
    public void givenMethodHasNoArgAnnotations_thenReturnEmptyMap() {
        String pattern = "GET:app/test/player/{playerId}";
        RouteInfo routeInfo = routes.get(pattern);

        Map<String, Object> result = resolver.resolve(Collections.emptyMap(), routeInfo.getParameters());

        assertThat(result).isEmpty();
    }

    @Test
    public void givenMixedAnnotations_thenOnlyArgAreResolved() {
        String pattern = "POST:app/test/player/{playerId}";
        RouteInfo routeInfo = routes.get(pattern);

        Map<String, Object> result = resolver.resolve(Collections.singletonMap("description", "Test"), routeInfo.getParameters());

        assertThat(result).containsExactly(MapEntry.entry("description", "Test"));
    }

    @Test
    public void givenRequestHasIncompatibleTypes_thenException() {
        String pattern = "POST:app/test/player/{playerId}";
        RouteInfo routeInfo = routes.get(pattern);

        expectedException.expect(IncompatibleParameterException.class);
        expectedException.expectMessage("Incompatible parameter: 'description'. Expected: '" + String.class + "', but actual was: '" + UserModel.class + "'");

        resolver.resolve(Collections.singletonMap("description", new UserModel("name", 1)), routeInfo.getParameters());
    }
}
