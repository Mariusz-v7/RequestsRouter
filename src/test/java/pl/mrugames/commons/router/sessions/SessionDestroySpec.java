package pl.mrugames.commons.router.sessions;

import io.reactivex.subjects.PublishSubject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import pl.mrugames.commons.router.controllers.UserModel;

@RunWith(BlockJUnit4ClassRunner.class)
public class SessionDestroySpec {
    private Session session;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
        session = new Session("123", s -> {
        });
        session.destroy();

        expectedException.expect(SessionExpiredException.class);
    }

    @Test
    public void add() {
        session.add("asdf");
    }

    @Test
    public void get() {
        session.get(String.class);
    }

    @Test
    public void addWithType() {
        session.add(String.class, "asdf");
    }

    @Test
    public void addAuthenticatedUser() {
        session.addAuthenticatedUser(new UserModel("", 0));
    }

    @Test
    public void compute() {
        session.compute(String.class, (a, b) -> "");
    }

    @Test
    public void merge() {
        session.merge("", (a, b) -> "");
    }

    @Test
    public void remove() {
        session.remove(String.class);
    }

    @Test
    public void registerEmitter() {
        session.registerEmitter(1, PublishSubject.create());
    }
}
