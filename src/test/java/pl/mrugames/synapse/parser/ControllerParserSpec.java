package pl.mrugames.synapse.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import pl.mrugames.synapse.annotations.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
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
    void givenSomeExpression_whenResolveValueIdentifier_thenDelegateToExpressionParser() {
        Object r = new Object();

        Expression expression = mock(Expression.class);
        doReturn(expression).when(expressionParser).parseExpression("hello");
        doReturn(r).when(expression).getValue();

        assertThat(controllerParser.resolveDefaultValue("hello")).isSameAs(r);
    }

    @Test
    void itShouldRecognizeAllParameterTypes_andReturnProperList() throws NoSuchMethodException {
        class Example {
            public void route(String normal, @PathVar("") String pathVar, @Arg("") String arg, @SessionVar("") String sessionVar) {
            }
        }

        Method route = Example.class.getMethod("route", String.class, String.class, String.class, String.class);

        List<RouteParameter> expected = Arrays.asList(
                mock(RouteParameter.class),
                mock(RouteParameter.class),
                mock(RouteParameter.class),
                mock(RouteParameter.class)
        );

        doReturn(expected.get(0), expected.get(1), expected.get(2), expected.get(3)).when(controllerParser).parseParameter(any());

        List<RouteParameter> result = controllerParser.getRouteParameters(route);
        assertThat(result).containsExactlyInAnyOrder(expected.toArray(new RouteParameter[expected.size() - 1]));

        ArgumentCaptor<Parameter> captor = ArgumentCaptor.forClass(Parameter.class);
        verify(controllerParser, times(expected.size())).parseParameter(captor.capture());

        List<Parameter> allValues = captor.getAllValues();
        assertThat(allValues).hasSize(expected.size());

        assertThat(allValues.get(0).getAnnotations()).hasSize(0);
        assertThat(allValues.get(1).isAnnotationPresent(PathVar.class)).isTrue();
        assertThat(allValues.get(2).isAnnotationPresent(Arg.class)).isTrue();
        assertThat(allValues.get(3).isAnnotationPresent(SessionVar.class)).isTrue();
    }

}
