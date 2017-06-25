package pl.mrugames.commons.router.arg_resolvers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import pl.mrugames.commons.router.exceptions.RouterException;

import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class JsonPayloadArgumentResolverSpec {
    @Autowired
    private JsonPayloadArgumentResolver resolver;

    @Autowired
    private RouterInitializer initializer;

    @Autowired
    private ObjectMapper mapper;

    private Map<String, RouteInfo> routes;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
        routes = initializer.getRoutes();
    }

    @Test
    public void givenMethodWithArgAnnotations_thenResolveValuesProperly() {
        String json = "{\"a\": 1, \"b\": \"str\", \"c\": 12.2, \"d\": \"xyz\"}";

        RouteInfo routeInfo = routes.get("GET:app/test/concat");

        Map<String, Object> result = resolver.resolve(json, routeInfo.getParameters());

        assertThat(result).containsExactly(
                MapEntry.entry("a", 1),
                MapEntry.entry("b", "str"),
                MapEntry.entry("c", 12.2),
                MapEntry.entry("d", "xyz")
        );
    }

    @Test
    public void givenMethodWithMixedAnnotations_thenResolveOnlyArgs() {
        RouteInfo routeInfo = routes.get("POST:app/test/player/{playerId}");
        String json = "{\"description\": \"Test\"}";

        Map<String, Object> result = resolver.resolve(json, routeInfo.getParameters());

        assertThat(result).containsExactly(MapEntry.entry("description", "Test"));
    }

    @Test
    public void givenRequestDoesNotContainArgument_thenException() {
        RouteInfo routeInfo = routes.get("POST:app/test/player/{playerId}");
        String json = "{\"invalid\": \"Test\"}";

        expectedException.expect(ParameterNotFoundException.class);
        expectedException.expectMessage("Could not find 'description' parameter in the request");

        resolver.resolve(json, routeInfo.getParameters());
    }

    @Test
    public void givenRequestHasIncompatibleTypes_thenException() {
        RouteInfo routeInfo = routes.get("GET:app/test/concat");
        String json = "{\"a\": \"incompatible\", \"b\": \"str\", \"c\": 12.2, \"d\": \"xyz\"}";

        expectedException.expect(IncompatibleParameterException.class);
        expectedException.expectMessage("Could not convert 'a' into 'int'");

        resolver.resolve(json, routeInfo.getParameters());
    }

    @Test
    public void givenRequestDoesNotHaveOneParameter_thenDefaultValueShouldBeUsed() {
        String json = "{\"a\": 1, \"b\": \"str\", \"c\": 12.2}";

        RouteInfo routeInfo = routes.get("GET:app/test/concat");

        Map<String, Object> result = resolver.resolve(json, routeInfo.getParameters());

        assertThat(result).containsExactly(
                MapEntry.entry("a", 1),
                MapEntry.entry("b", "str"),
                MapEntry.entry("c", 12.2),
                MapEntry.entry("d", "last")
        );
    }

    @Test
    public void givenMethodHasNoArgAnnotations_thenReturnEmptyMap() {
        String pattern = "GET:app/test/player/{playerId}";
        RouteInfo routeInfo = routes.get(pattern);

        String json = "";
        Map<String, Object> result = resolver.resolve(json, routeInfo.getParameters());

        assertThat(result).isEmpty();
    }

    @Test
    public void givenInvalidJson_thenException() {
        String pattern = "GET:app/test/player/{playerId}";
        RouteInfo routeInfo = routes.get(pattern);

        String json = "invalid";

        expectedException.expect(RouterException.class);

        resolver.resolve(json, routeInfo.getParameters());
    }

    @Test
    public void shouldResolveComplexType() {
        String pattern = "POST:app/test/player";
        RouteInfo routeInfo = routes.get(pattern);
        String json = "{\"user\": { \"name\": \"Mariusz\", \"id\": 12 } }";

        Map<String, Object> result = resolver.resolve(json, routeInfo.getParameters());

        assertThat(result).containsExactly(MapEntry.entry("user", new UserModel("Mariusz", 12)));
    }

    @Test
    public void nullParametersShouldBeAcceptable() throws JsonProcessingException {
        String pattern = "POST:app/test/player";
        RouteInfo routeInfo = routes.get(pattern);

        ObjectNode node = mapper.createObjectNode();
        node.putNull("user");

        String json = node.toString();

        Map<String, Object> result = resolver.resolve(json, routeInfo.getParameters());
        assertThat(result).containsExactly(MapEntry.entry("user", null));
    }

}
