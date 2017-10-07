package pl.mrugames.commons.router.sessions;

import io.reactivex.subjects.PublishSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class SessionDestroySpec {
    private Session session;
    private PublishSubject subject;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
        session = new Session("123", s -> {
        });
        session.destroy();

        expectedException.expect(SessionExpiredException.class);

        subject = PublishSubject.create();
    }

    @After
    public void after() {
        subject.onComplete();
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
        session.registerEmitter(1, subject);
    }

    @Test
    public void addVarArgs() {
        session.add("asdf", 123);
    }

    @Test
    public void setSecurityCode() {
        session.setSecurityCode("124");
    }

    @Test
    public void getSecurityCode() {
        session.getSecurityCode();
    }

    @Test
    public void registerSubscription() {
        session.registerSubscription(1, subject.subscribe());
    }
}
