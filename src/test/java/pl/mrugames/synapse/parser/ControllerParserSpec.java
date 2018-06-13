package pl.mrugames.synapse.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.DigestUtils;
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

    @Test
    void noAnnotationArgumentParsing() throws NoSuchMethodException {
        class Example {
            public void route(String arg) {
            }
        }

        Method route = Example.class.getMethod("route", String.class);
        Parameter[] parameters = route.getParameters();
        RouteParameter routeParameter = controllerParser.parseParameter(parameters[0]);

        String name = DigestUtils.md5DigestAsHex(String.class.getCanonicalName().getBytes());

        assertThat(routeParameter.getDefaultValue()).isNull();
        assertThat(routeParameter.getResolution()).isEqualTo(ParameterResolution.SESSION);
        assertThat(routeParameter.isRequired()).isTrue();
        assertThat(routeParameter.getName()).isEqualTo(name);
        assertThat(routeParameter.getType()).isEqualTo(String.class);
    }

    @Test
    void givenPathVarAnnotation_whenParse_thenReturnProperData() throws NoSuchMethodException {
        class Example {
            public void route(@PathVar("path-var-name") Integer arg) {
            }
        }

        Method route = Example.class.getMethod("route", Integer.class);
        Parameter[] parameters = route.getParameters();
        RouteParameter routeParameter = controllerParser.parseParameter(parameters[0]);

        assertThat(routeParameter.getDefaultValue()).isNull();
        assertThat(routeParameter.getResolution()).isEqualTo(ParameterResolution.PATH_VAR);
        assertThat(routeParameter.isRequired()).isTrue();
        assertThat(routeParameter.getName()).isEqualTo("path-var-name");
        assertThat(routeParameter.getType()).isEqualTo(Integer.class);
    }

    @Test
    void givenPathVarAnnotationOnObjectType_whenParse_thenException() throws NoSuchMethodException {
        class Example {
            public void route(@PathVar("path-var-name") Object arg) {
            }
        }

        Method route = Example.class.getMethod("route", Object.class);
        Parameter[] parameters = route.getParameters();
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> controllerParser.parseParameter(parameters[0]));
        assertThat(illegalArgumentException.getMessage())
                .isEqualTo("Failed to parse parameter: 'path-var-name'. Only primitive types can be set for @PathVar annotation. Type: " + Object.class.getCanonicalName());
    }

    @Test
    void givenPathVarAnnotationWithEmptyName_whenParse_thenException() throws NoSuchMethodException {
        class Example {
            public void route(@PathVar("") Object arg) {
            }
        }

        Method route = Example.class.getMethod("route", Object.class);
        Parameter[] parameters = route.getParameters();
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> controllerParser.parseParameter(parameters[0]));
        assertThat(illegalArgumentException.getMessage())
                .isEqualTo("Failed to parse parameter. Empty value cannot be provided for @PathVar annotation.");
    }

    @Test
        // value should match regexp
    void givenPathVarAnnotationWithBadValue_whenParse_thenException() throws NoSuchMethodException {
        class Example {
            public void route(@PathVar("{'/") Object arg) {
            }
        }

        Method route = Example.class.getMethod("route", Object.class);
        Parameter[] parameters = route.getParameters();
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> controllerParser.parseParameter(parameters[0]));
        assertThat(illegalArgumentException.getMessage())
                .isEqualTo("Failed to parse parameter. Value '{'/' is not allowed.");
    }

    @Test
    void expressionParserShouldResolveToNull() {
        expressionParser = new SpelExpressionParser();
        Expression expression = expressionParser.parseExpression("null");

        assertThat(expression.getValue()).isNull();
    }

    @Test
    void givenArgAnnotation_whenParse_thenReturnProperData() throws NoSuchMethodException {
        class Example {
            public void route(@Arg("argument") Object arg) {
            }
        }

        doReturn(null).when(controllerParser).resolveDefaultValue("null");

        Method route = Example.class.getMethod("route", Object.class);
        Parameter[] parameters = route.getParameters();
        RouteParameter routeParameter = controllerParser.parseParameter(parameters[0]);

        assertThat(routeParameter.getResolution()).isEqualTo(ParameterResolution.PAYLOAD);
        assertThat(routeParameter.getDefaultValue()).isNull();
        assertThat(routeParameter.isRequired()).isTrue();
        assertThat(routeParameter.getType()).isEqualTo(Object.class);
        assertThat(routeParameter.getName()).isEqualTo("argument");
    }

    @Test
    void givenArgAnnotationWithEmptyName_whenParse_thenException() throws NoSuchMethodException {
        class Example {
            public void route(@Arg("") Object arg) {
            }
        }

        Method route = Example.class.getMethod("route", Object.class);
        Parameter[] parameters = route.getParameters();
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> controllerParser.parseParameter(parameters[0]));

        assertThat(illegalArgumentException.getMessage()).isEqualTo("Failed to parse parameter. Name has to be provided for @Arg annotation");
    }
}
