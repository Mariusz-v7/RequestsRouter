package pl.mrugames.synapse.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.util.DigestUtils;
import pl.mrugames.synapse.annotations.*;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ControllerParserSpec {
    private ControllerParser controllerParser;
    private ExpressionParser expressionParser;

    @BeforeEach
    void before() {
        expressionParser = mock(ExpressionParser.class);
        controllerParser = spy(new ControllerParser(expressionParser));
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
            public void route(@Arg(value = "arg1", defaultValue = "a") String arg1,
                              @PathVar("arg2") Integer arg2,
                              @SessionVar(value = "arg3", defaultValue = "b") Double arg3,
                              Byte sessionArg) {
            }
        }

        Method method = Ctrl.class.getMethod("route", String.class, Integer.class, Double.class, Byte.class);
        assertThat(method).isNotNull();

        ///

        doReturn("default 1").when(controllerParser).resolveDefaultValue("a");
        doReturn("default 2").when(controllerParser).resolveDefaultValue("b");

        List<RouteParameter> routeParameters = controllerParser.getRouteParameters(method);

        String lastArgName = DigestUtils.md5DigestAsHex(Byte.class.getCanonicalName().getBytes());

        assertThat(routeParameters).containsExactlyInAnyOrder(
                new RouteParameter("arg1", ParameterResolution.PAYLOAD, String.class, "default 1"),
                new RouteParameter("arg2", ParameterResolution.PATH_VAR, Integer.class, null),
                new RouteParameter("arg3", ParameterResolution.SESSION, Double.class, "default 2"),
                new RouteParameter(lastArgName, ParameterResolution.SESSION, Byte.class, null)
        );
    }

    @Test
    void givenDefaultValueEqualsNullDefaultValueIdentifier_whenResolveDefaultValue_thenReturnNull() {
        assertThat(controllerParser.resolveDefaultValue(NullDefaultValueIdentifier.NULL_DEFAULT_VALUE_IDENTIFIER)).isNull();
    }

    @Test
    void givenSomeExpression_whenResolveValueIdentifier_thenDelegateToExpressionParser() {
        Object r = new Object();

        Expression expression = mock(Expression.class);
        doReturn(expression).when(expressionParser).parseExpression("hello");
        doReturn(r).when(expression).getValue();

        assertThat(controllerParser.resolveDefaultValue("hello")).isSameAs(r);
    }

}
