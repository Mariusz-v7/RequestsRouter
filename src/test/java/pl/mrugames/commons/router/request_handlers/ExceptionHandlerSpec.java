package pl.mrugames.commons.router.request_handlers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.ResponseStatus;
import pl.mrugames.commons.router.exceptions.IncompatibleParameterException;
import pl.mrugames.commons.router.exceptions.ParameterNotFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(BlockJUnit4ClassRunner.class)
public class ExceptionHandlerSpec {
    private ExceptionHandler exceptionHandler;

    @Before
    public void before() {
        exceptionHandler = new ExceptionHandler();
    }

    @Test
    public void parameterNotFound() {
        Exception e = new ParameterNotFoundException("xxx");
        Response response = exceptionHandler.handle(1, e).blockingFirst();
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.BAD_REQUEST);
        assertThat(response.getPayload()).isEqualTo(e.getMessage());
    }

    @Test
    public void illegalArgument() {
        Exception e = new IllegalArgumentException("xxx");
        Response response = exceptionHandler.handle(1, e).blockingFirst();
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.BAD_REQUEST);
        assertThat(response.getPayload()).isEqualTo(e.getMessage());
    }

    @Test
    public void incompatibleParameter() {
        Exception e = new IncompatibleParameterException("asdf", String.class, Integer.class);
        Response response = exceptionHandler.handle(1, e).blockingFirst();
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.BAD_REQUEST);
        assertThat(response.getPayload()).isEqualTo(e.getMessage());
    }

    @Test
    public void constraintViolationException() {
        ConstraintViolationException e = new ConstraintViolationException("xxx", Collections.<ConstraintViolation<?>>singleton(mock(ConstraintViolation.class)));

        Response response = exceptionHandler.handle(1, e).blockingFirst();
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.BAD_PARAMETERS);
    }
}
