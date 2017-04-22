package pl.mrugames.commons.router.sessions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.TestConfiguration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TestConfiguration.class
})
public class SessionManagerSpec {
    @Autowired
    private SessionManager sessionManager;

    @Test
    public void givenSessionDoesNotExist_whenGetSession_thenNewOneIsCreated() {
        assertThat(sessionManager.contains("1234")).isFalse();
        Session session = sessionManager.getSession("1234");
        assertThat(sessionManager.getAllSessions()).contains(session);
        assertThat(sessionManager.contains("1234")).isTrue();
    }

    @Test
    public void givenSessionIsAdded_whenGetSession_thenSameIsReturned() {
        Session session1 = sessionManager.getSession("2345");
        Session session2 = sessionManager.getSession("2345");

        assertThat(session1).isSameAs(session2);
    }

    @Test
    public void givenNewSessionIsCreated_thenLastAccessTimeIsNotNull_and() {
        Instant now = Instant.now();

        Session session = sessionManager.getSession("10");
        assertThat(session.getLastAccessed()).isNotNull();

        assertThat(session.getLastAccessed()).isBetween(now, Instant.now());
    }

    @Test
    public void givenSessionExists_whenGetSession_thenLastAccessTimeIsUpdated() throws InterruptedException {
        sessionManager.getSession("11");
        TimeUnit.MILLISECONDS.sleep(3);

        Instant now = Instant.now();
        Session session = sessionManager.getSession("11");
        assertThat(session.getLastAccessed()).isBetween(now, Instant.now());
    }

    @Test
    public void givenSessionLastAccessTimeIsExpired_thenItShouldBeDeletedAndDestroyed() throws InterruptedException {
        Session session = sessionManager.getSession("12");
        assertThat(sessionManager.contains("12")).isTrue();

        session.updateLastAccessed(Instant.now().minus(30, ChronoUnit.MINUTES));

        TimeUnit.MILLISECONDS.sleep(200);

        assertThat(sessionManager.contains("12")).isFalse();
        assertThat(session.isDestroyed()).isTrue();
    }

    @Test
    public void givenSessionIsAdded_whenItIsRemoved_thenRemoveFromCollection() {
        Session session = sessionManager.getSession("13");
        sessionManager.remove(session);
        assertThat(sessionManager.contains("13")).isFalse();
    }

    @Test
    public void givenSessionIsAdded_whenItIsRemoved_thenCallDestroyMethod() {
        Session session = sessionManager.getSession("13");
        assertThat(session.isDestroyed()).isFalse();

        sessionManager.remove(session);
        assertThat(session.isDestroyed()).isTrue();
    }

    @Test
    public void givenSessionIsAdded_whenDestroy_thenItIsRemoved() {
        Session session = sessionManager.getSession("13");
        session.destroy();
        assertThat(sessionManager.contains("13")).isFalse();
    }
}
