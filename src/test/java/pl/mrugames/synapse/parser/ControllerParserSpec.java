package pl.mrugames.synapse.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;
import pl.mrugames.synapse.annotations.*;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ControllerParserSpec {
    private ControllerParser controllerParser;

    @BeforeEach
    void before() {
        controllerParser = new ControllerParser();
    }

    @Test
    void givenClassNotAnnotated_whenGetAnnotation_thenException() {
        class Nothing {
        }

        Nothing nothing = new Nothing();

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> controllerParser.getControllerAnnotation(nothing));
        assertThat(illegalArgumentException.getMessage()).isEqualTo(nothing.getClass().getName() + " is not a controller! (missing annotation)");
    }

    @Test
    void givenClassAnnotated_whenGetAnnotation_thenReturnAnnotation() {
        @Controller("hello world!")
        class Annotated {
        }

        Annotated annotated = new Annotated();
        Controller controllerAnnotation = controllerParser.getControllerAnnotation(annotated);

        assertThat(controllerAnnotation.value()).isEqualTo("hello world!");
    }

    @Test
    void givenClassHasOneMethodAnnotatedRoute_whenGetRoutes_thenReturnIt() throws NoSuchMethodException {
        class OneRoute {
            @Route
            public void route() {
            }
        }

        OneRoute oneRoute = new OneRoute();

        List<Method> methods = controllerParser.getRoutes(oneRoute);

        assertThat(methods).hasSize(1);
        assertThat(methods).containsExactly(oneRoute.getClass().getMethod("route"));
    }

    @Test
    void givenSuperClassHasRoute_whenGetRoutes_thenIncludeSuperClassRoutes() throws NoSuchMethodException {
        class SuperSuper {
            @Route
            public void superSuperRoute() {
            }
        }

        class Super extends SuperSuper {
            @Route
            public void superRoute() {
            }
        }

        class Ctrl extends Super {
            @Route
            public void route() {
            }
        }

        Ctrl ctrl = new Ctrl();

        List<Method> methods = controllerParser.getRoutes(ctrl);

        assertThat(methods).hasSize(3);
        assertThat(methods).containsExactlyInAnyOrder(
                ctrl.getClass().getMethod("superSuperRoute"),
                ctrl.getClass().getMethod("superRoute"),
                ctrl.getClass().getMethod("route")
        );
    }

    @Test
    void givenMethodHasArgArguments_whenGetRouteParameters_thenReturnProperList() throws NoSuchMethodException, NoSuchAlgorithmException, UnsupportedEncodingException {
        class Ctrl {
            public void route(@Arg("arg1") String arg1, @PathVar("arg2") Integer arg2, @SessionVar("arg3") Double arg3, Byte sessionArg) {
            }
        }

        Method method = Ctrl.class.getMethod("route", String.class, Integer.class, Double.class, Byte.class);

        assertThat(method).isNotNull();

        List<RouteParameter> routeParameters = controllerParser.getRouteParameters(method);

        String lastArgName = DigestUtils.md5DigestAsHex(Byte.class.getCanonicalName().getBytes());

        assertThat(routeParameters).containsExactlyInAnyOrder(
                new RouteParameter("arg1", ParameterResolution.PAYLOAD, String.class),
                new RouteParameter("arg2", ParameterResolution.PATH_VAR, Integer.class),
                new RouteParameter("arg3", ParameterResolution.SESSION, Double.class),
                new RouteParameter(lastArgName, ParameterResolution.SESSION, Byte.class)


        );
    }

}
