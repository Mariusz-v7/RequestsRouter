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
import pl.mrugames.commons.router.sessions.Session;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class SessionArgumentResolverSpec {
    @Autowired
    private SessionArgumentResolver resolver;

    @Autowired
    private RouterInitializer initializer;

    private Map<String, RouteInfo> routes;
    private Session session;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
        routes = initializer.getRoutes();

        session = new Session();
        session.add(new UserModel("Mruczek", 123));
        session.add("bla bla");
    }

    @Test
    public void givenRouteWithoutSessionArguments_thenReturnEmptyMap() {
        Map<Class<?>, Optional<Object>> result = resolver.resolve(session, routes.get("GET:app/test/concat").getParameters());
        assertThat(result).isEmpty();
    }

    @Test
    public void givenOneArgumentInSession_whenRequest_thenResolveArgumentsWithoutAnnotations() {
        Map<Class<?>, Optional<Object>> result = resolver.resolve(session, routes.get("GET:app/test/account/username").getParameters());
        assertThat(result).containsOnlyKeys(UserModel.class);
        assertThat(result.get(UserModel.class).get()).isEqualTo(new UserModel("Mruczek", 123));
    }

    @Test
    public void givenSessionDoesNotHaveArguments_thenEmptyOptionals() {
        Map<Class<?>, Optional<Object>> result = resolver.resolve(session, routes.get("GET:app/test/session/defaults").getParameters());
        assertThat(result).contains(
                MapEntry.entry(Object.class, Optional.empty()),
                MapEntry.entry(long.class, Optional.empty()),
                MapEntry.entry(int.class, Optional.empty()),
                MapEntry.entry(double.class, Optional.empty()),
                MapEntry.entry(short.class, Optional.empty()),
                MapEntry.entry(byte.class, Optional.empty()),
                MapEntry.entry(boolean.class, Optional.empty())
        );
    }

}
