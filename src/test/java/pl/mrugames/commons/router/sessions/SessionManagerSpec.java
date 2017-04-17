package pl.mrugames.commons.router.sessions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import pl.mrugames.commons.router.TestConfiguration;

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
}
