package pl.mrugames.commons.router;

import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

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

        Request request = new Request(1, "session", "GET:app/test/concat", payload);

        RouteInfo routeInfo = routes.get("GET:app/test/concat");

        Map<String, Object> result = resolver.resolve(request, routeInfo.getParameters());

        assertThat(result).containsExactly(
                MapEntry.entry("a", 1),
                MapEntry.entry("b", "str"),
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

        Request request = new Request(1, "session", "GET:app/test/concat", payload);

        RouteInfo routeInfo = routes.get("GET:app/test/concat");

        Map<String, Object> result = resolver.resolve(request, routeInfo.getParameters());

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

        Request request = new Request(1, "session", "GET:app/test/concat", payload);

        RouteInfo routeInfo = routes.get("GET:app/test/concat");

        Map<String, Object> result = resolver.resolve(request, routeInfo.getParameters());

        assertThat(result).containsExactly(
                MapEntry.entry("a", 1),
                MapEntry.entry("b", "str"),
                MapEntry.entry("c", 12.1),
                MapEntry.entry("d", "last")
        );
    }

    @Test
    public void givenMethodHasNoArgAnnotations_thenReturnEmptyMap() {
        String pattern = "GET:app/test/player/{playerId}";
        RouteInfo routeInfo = routes.get(pattern);

        Request request = new Request(1, "session", "GET:app/test/player/1", Collections.emptyMap());
        Map<String, Object> result = resolver.resolve(request, routeInfo.getParameters());

        assertThat(result).isEmpty();
    }

    //TODO: request payload lacks some of fields -> if default value -> use default -> else throw exception
    //TODO: ignore other thatn @ARG
    //TODO: incompatible fields
}
