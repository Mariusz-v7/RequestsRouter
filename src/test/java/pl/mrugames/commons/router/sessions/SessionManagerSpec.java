package pl.mrugames.commons.router.sessions;

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
import java.time.temporal.ChronoUnit;
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

    @Test
    public void givenSessionDoesNotExist_whenGetSession_thenNewOneIsCreated() {
        assertThat(sessionManager.contains(sessionId)).isFalse();
        Session session = sessionManager.getSession(sessionId, "");
        assertThat(sessionManager.getAllSessions()).contains(session);
        assertThat(sessionManager.contains(sessionId)).isTrue();
    }

    @Test
    public void givenSessionIsAdded_whenGetSession_thenSameIsReturned() {
        Session session1 = sessionManager.getSession(sessionId, "");
        Session session2 = sessionManager.getSession(sessionId, "");

        assertThat(session1).isSameAs(session2);
    }

    @Test
    public void givenNewSessionIsCreated_thenLastAccessTimeIsNotNull_and() {
        Instant now = Instant.now();

        Session session = sessionManager.getSession(sessionId, "");
        assertThat(session.getLastAccessed()).isNotNull();

        assertThat(session.getLastAccessed()).isBetween(now, Instant.now());
    }

    @Test
    public void givenSessionExists_whenGetSession_thenLastAccessTimeIsUpdated() throws InterruptedException {
        sessionManager.getSession(sessionId, "");
        TimeUnit.MILLISECONDS.sleep(3);

        Instant now = Instant.now();
        Session session = sessionManager.getSession(sessionId, "");
        assertThat(session.getLastAccessed()).isBetween(now, Instant.now());
    }

    @Test
    public void givenSessionLastAccessTimeIsExpired_thenItShouldBeDeletedAndDestroyed() throws InterruptedException {
        Session session = sessionManager.getSession(sessionId, "");
        assertThat(sessionManager.contains(sessionId)).isTrue();

        session.updateLastAccessed(Instant.now().minus(30, ChronoUnit.MINUTES));

        TimeUnit.MILLISECONDS.sleep(200);

        assertThat(sessionManager.contains(sessionId)).isFalse();
        assertThat(session.isDestroyed()).isTrue();
    }

    @Test
    public void givenSessionIsAdded_whenItIsRemoved_thenRemoveFromCollection() {
        Session session = sessionManager.getSession(sessionId, "");
        sessionManager.remove(session);
        assertThat(sessionManager.contains(sessionId)).isFalse();
    }

    @Test
    public void givenSessionIsAdded_whenItIsRemoved_thenCallDestroyMethod() {
        Session session = sessionManager.getSession(sessionId, "");
        assertThat(session.isDestroyed()).isFalse();

        sessionManager.remove(session);
        assertThat(session.isDestroyed()).isTrue();
    }

    @Test
    public void givenSessionIsAdded_whenDestroy_thenItIsRemoved() {
        Session session = sessionManager.getSession(sessionId, "");
        session.destroy();
        assertThat(sessionManager.contains(sessionId)).isFalse();
    }

    @Test
    public void whenGetSessionWithTooShortId_thenException() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Session id must be at least " + SessionManager.SESSION_ID_MIN_LENGTH + " characters long");

        sessionManager.getSession("123", "");
    }
}
