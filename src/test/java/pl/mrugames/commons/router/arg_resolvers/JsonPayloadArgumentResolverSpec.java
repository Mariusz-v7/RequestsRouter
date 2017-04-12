package pl.mrugames.commons.router.arg_resolvers;

import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.RouteInfo;
import pl.mrugames.commons.router.RouterInitializer;
import pl.mrugames.commons.router.TestConfiguration;

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

    private Map<String, RouteInfo> routes;

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
}
