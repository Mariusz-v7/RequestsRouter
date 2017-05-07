package pl.mrugames.commons.router.request_handlers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.ResponseStatus;
import pl.mrugames.commons.router.exceptions.ApplicationException;
import pl.mrugames.commons.router.exceptions.IncompatibleParameterException;
import pl.mrugames.commons.router.exceptions.ParameterNotFoundException;
import pl.mrugames.commons.router.exceptions.RouteConstraintViolationException;
import pl.mrugames.commons.router.sessions.SessionExpiredException;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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
        RouteConstraintViolationException e = new RouteConstraintViolationException(Collections.emptyList());

        Response response = exceptionHandler.handle(1, e).blockingFirst();
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.BAD_PARAMETERS);
    }

    @Test
    public void sessionExpiredException() {
        SessionExpiredException e = new SessionExpiredException();

        Response response = exceptionHandler.handle(1, e).blockingFirst();
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.BAD_REQUEST);
        assertThat(response.getPayload()).isEqualTo("Session expired");
    }

    @Test
    public void applicationException() {
        ApplicationException e1 = new ApplicationException(ResponseStatus.BAD_PARAMETERS, "bla");
        ApplicationException e2 = new ApplicationException(ResponseStatus.BAD_REQUEST, "alb");

        Response response = exceptionHandler.handle(1, e1).blockingFirst();
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.BAD_PARAMETERS);
        assertThat(response.getPayload()).isEqualTo("bla");

        response = exceptionHandler.handle(2, e2).blockingFirst();
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.BAD_REQUEST);
        assertThat(response.getPayload()).isEqualTo("alb");
    }
}