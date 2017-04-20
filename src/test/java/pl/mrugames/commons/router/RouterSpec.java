package pl.mrugames.commons.router;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

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
}
