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
import pl.mrugames.commons.router.exceptions.IncompatibleParameterException;
import pl.mrugames.commons.router.exceptions.PathParameterNotFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class PathArgumentResolverSpec {
    @Autowired
    private PathArgumentResolver resolver;

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
    public void givenMethodWithoutPathVarAnnotations_thenReturnEmptyMap() {
        String path = "GET:app/test/concat";
        RouteInfo route = routes.get(path);
        assertThat(route).isNotNull();

        assertThat(resolver.resolve(path, path, route.getParameters())).isEmpty();
    }

    @Test
    public void givenMethodWithOnePathVar_thenResolveWithValueFromPath() {
        String pattern = "GET:app/test/player/{playerId}";
        RouteInfo route = routes.get(pattern);
        assertThat(route).isNotNull();

        assertThat(resolver.resolve("GET:app/test/player/345", pattern, route.getParameters()))
                .containsExactly(MapEntry.entry("playerId", 345));
    }

    @Test
    public void givenMethodWithMixedParameters_thenResolveOnlyPathVar() {
        String pattern = "POST:app/test/player/{playerId}";
        RouteInfo route = routes.get(pattern);
        assertThat(route).isNotNull();

        assertThat(resolver.resolve("POST:app/test/player/345", pattern, route.getParameters()))
                .containsExactly(MapEntry.entry("playerId", 345));
    }

    @Test
    public void givenIncompatibleTypes_thenThrowException() {
        String pattern = "POST:app/test/player/{playerId}";
        RouteInfo route = routes.get(pattern);
        assertThat(route).isNotNull();

        expectedException.expect(IncompatibleParameterException.class);
        expectedException.expectMessage("Could not convert 'playerId' into '" + int.class + "'");

        resolver.resolve("POST:app/test/player/incompatible", pattern, route.getParameters());
    }

    @Test
    public void givenKeyNotPresentInPath_thenException() {
        String pattern = "POST:app/test/player/{playerId}";
        RouteInfo route = routes.get(pattern);
        assertThat(route).isNotNull();

        expectedException.expect(PathParameterNotFoundException.class);
        expectedException.expectMessage("Invalid path: POST:app/test/player for pattern: " + pattern);

        resolver.resolve("POST:app/test/player", pattern, route.getParameters());
    }

}
