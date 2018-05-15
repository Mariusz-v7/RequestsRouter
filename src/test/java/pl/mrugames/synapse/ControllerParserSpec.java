package pl.mrugames.synapse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.mrugames.synapse.annotations.Controller;

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

}
