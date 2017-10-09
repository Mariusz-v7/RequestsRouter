package pl.mrugames.commons.router.sessions;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.TestConfiguration;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class SessionManagerSpec {
    private final String sessionId = "1123456789012345678901234567890123456789012345678901234567890234567890";

    @Autowired
    private SessionManager sessionManager;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
    }

    @Test
    public void givenNewSessionIsCreated_thenLastAccessTimeIsNotNull_and() {
        Instant now = Instant.now();

        sessionManager.createSession();
        Session session = sessionManager.getSession();
        assertThat(session.getLastAccessed()).isNotNull();

        assertThat(session.getLastAccessed()).isBetween(now, Instant.now());
    }

    @Test
    public void givenSessionExists_whenGetSession_thenLastAccessTimeIsUpdated() throws InterruptedException {
        sessionManager.getSession();
        TimeUnit.MILLISECONDS.sleep(3);

        Instant now = Instant.now();
        Session session = sessionManager.getSession();
        assertThat(session.getLastAccessed()).isBetween(now, Instant.now());
    }

    @Test
    public void givenSessionIsCreated_whenGetLocalSession_thenReturnSameInstance() {
        sessionManager.createSession();
        Session session = sessionManager.getSession();
        Session localSession = Session.getLocalSession().orElse(null);

        assertThat(localSession).isSameAs(session);
    }

    @Test
    public void givenSessionExists_whenGetExistingLocal_thenUpdateLastAccessTime() throws InterruptedException {
        sessionManager.getSession();
        Thread.sleep(10);
        Instant now = Instant.now();
        Session session = Session.getExistingLocalSession();
        assertThat(session.getLastAccessed()).isBetween(now, Instant.now());
    }

    @Test
    public void givenSessionExists_whenGetLocal_thenUpdateLastAccessTime() throws InterruptedException {
        sessionManager.getSession();
        Thread.sleep(10);
        Instant now = Instant.now();
        Optional<Session> session = Session.getLocalSession();
        assertThat(session.get().getLastAccessed()).isBetween(now, Instant.now());
    }
}
