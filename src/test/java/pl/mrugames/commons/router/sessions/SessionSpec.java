package pl.mrugames.commons.router.sessions;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import pl.mrugames.commons.router.Response;
import pl.mrugames.commons.router.controllers.Interface;
import pl.mrugames.commons.router.controllers.UserModel;
import pl.mrugames.commons.router.permissions.RoleHolder;

import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(BlockJUnit4ClassRunner.class)
public class SessionSpec {
    private Consumer<Session> destroyMethod;
    private Session session;
    private PublishSubject<Response> subject1;
    private PublishSubject<Response> subject2;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    @SuppressWarnings("unchecked")
    public void before() {
        destroyMethod = mock(Consumer.class);

        session = new Session("asdf", destroyMethod);

        subject1 = PublishSubject.create();
        subject2 = PublishSubject.create();
    }

    @After
    public void after() {
        subject1.onComplete();
        subject2.onComplete();
    }

    @Test
    public void whenSessionDestroy_thenDestroyMethodIsCalled() {
        session.destroy();
        verify(destroyMethod).accept(session);
    }

    @Test
    public void whenAdd_thenShouldBeAbleToGet() {
        UserModel userModel = new UserModel("Session user", 908);
        session.add(userModel);

        assertThat(session.get(UserModel.class).get()).isEqualTo(userModel);
    }

    @Test
    public void whenAddNull_thenNullPointerException() {
        expectedException.expect(NullPointerException.class);
        session.add((Object) null);
    }

    @Test
    public void givenEntityAdded_whenRemove_andGet_thenOptionalEmpty() {
        UserModel userModel = new UserModel("Session user", 908);
        session.add(userModel);
        session.remove(UserModel.class);

        assertThat(session.get(UserModel.class)).isEmpty();
    }

    @Test
    public void givenEntityAdded_whenAddSecondOfSameType_thenFirstIsOverwritten() {
        UserModel userModel1 = new UserModel("Session user 1", 908);
        UserModel userModel2 = new UserModel("Session user 2", 909);

        session.add(userModel1);
        session.add(userModel2);

        assertThat(session.get(UserModel.class).get()).isEqualTo(userModel2);
    }

    @Test
    public void givenValueIsAdded_whenMerge_thenUseProvidedFunction() {
        UserModel userModel1 = new UserModel("Session user 1", 908);
        UserModel userModel2 = new UserModel("Session user 2", 909);

        session.add(userModel1);
        UserModel result1 = session.merge(userModel2, (current, inserted) -> current);

        assertThat(session.get(UserModel.class).get()).isEqualTo(userModel1);
        assertThat(result1).isEqualTo(userModel1);

        UserModel result2 = session.merge(userModel2, (current, inserted) -> inserted);

        assertThat(session.get(UserModel.class).get()).isEqualTo(userModel2);
        assertThat(result2).isEqualTo(userModel2);
    }

    @Test
    public void givenNoValue_whenMerge_thenDoNotCallMerge() {
        UserModel userModel1 = new UserModel("Session user 1", 908);
        UserModel result1 = session.merge(userModel1, (current, inserted) -> {
            fail("should not be called");
            return inserted;
        });

        assertThat(session.get(UserModel.class).get()).isEqualTo(userModel1);
        assertThat(result1).isEqualTo(userModel1);
    }

    @Test
    public void givenEntityIsNotAdded_whenCompute_thenArgumentIsNull() {
        UserModel userModel1 = new UserModel("Session user 1", 908);

        UserModel result = session.compute(UserModel.class, (Class<UserModel> key, UserModel value) -> {
            assertThat(value).isNull();
            return userModel1;
        });

        assertThat(result).isEqualTo(userModel1);
    }

    @Test
    public void givenValueIsAdded_whenCompute_thenArgumentIsNotNull() {
        UserModel userModel1 = new UserModel("Session user 1", 908);
        session.add(userModel1);

        UserModel userModel2 = new UserModel("Session user 2", 909);

        UserModel result = session.compute(UserModel.class, (Class<UserModel> key, UserModel value) -> {
            assertThat(value).isEqualTo(userModel1);
            return userModel2;
        });

        assertThat(result).isEqualTo(userModel2);
    }

    @Test
    public void givenUserModelIsAdded_whenAddInterface_thenBothKeysAreAssociatedWithOneValue() {
        UserModel userModel1 = new UserModel("Session user 1", 908);

        session.add(userModel1);
        session.add(Interface.class, userModel1);

        assertThat(session.get(UserModel.class).get()).isEqualTo(userModel1);
        assertThat(session.get(Interface.class).get()).isEqualTo(userModel1);
    }

    @Test
    public void whenRegisterAuthenticatedUser_thenRoleOwnerIsAddedToSession() {
        UserModel userModel = new UserModel("Role Holder", 1239);
        session.addAuthenticatedUser(userModel);
        assertThat(session.get(RoleHolder.class).get()).isSameAs(userModel);
    }

    @Test
    public void whenSessionIsCreated_thenItContainsReferenceToItself() {
        Session session = new Session("ASDF", s -> {
        });
        assertThat(session.get(Session.class)).isPresent();
        assertThat(session.get(Session.class).get()).isSameAs(session);
    }

    @Test
    public void whenSessionIsCreated_thenItIsNotDestroyed() {
        Session session = new Session("", s -> {
        });

        assertThat(session.isDestroyed()).isFalse();
    }

    @Test
    public void givenSessionRegistersEmitter_whenDestroy_thenAllObserversAreUnsubscribed() {
        TestObserver<Response> observer1 = TestObserver.create();
        TestObserver<Response> observer2 = TestObserver.create();

        session.registerEmitter(1, subject1);
        session.registerEmitter(2, subject2);

        subject1.subscribe(observer1);
        subject2.subscribe(observer2);

        observer1.assertNotComplete();
        observer2.assertNotComplete();

        session.destroy();

        observer1.assertComplete();
        observer2.assertComplete();
    }

    @Test
    public void givenEmitterIsRegistered_whenUnregister_thenObserversComplete() {
        TestObserver<Response> observer = TestObserver.create();

        session.registerEmitter(1, subject1);
        assertThat(session.getEmittersAmount()).isEqualTo(1);

        subject1.subscribe(observer);

        observer.assertNotComplete();
        session.unregisterEmitter(1);
        observer.assertComplete();

        assertThat(session.getEmittersAmount()).isEqualTo(0);
    }

    @Test
    public void givenEmitterIsRegistered_whenComplete_thenItIsRemovedFromSession() {

        session.registerEmitter(1, subject1);
        assertThat(session.getEmittersAmount()).isEqualTo(1);

        subject1.onComplete();
        assertThat(session.getEmittersAmount()).isEqualTo(0);
    }

    @Test
    public void givenEmitterIsRegistered_whenSecondIsBeingRegisteredWithSameId_thenException() {
        session.registerEmitter(10, subject1);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Emitter with id 10 is already registered");

        session.registerEmitter(10, subject2);
    }

    @Test
    public void givenNewSessionWithoutAuthenticatedUser_whenGetAuthenticatedUser_thenNull() {
        assertThat(session.getAuthenticatedUser()).isEmpty();
    }

    @Test
    public void givenSessionHasAuthenticatedUser_whenGetAuthenticatedUser_thenReturnIt() {
        RoleHolder roleHolder = mock(RoleHolder.class);
        session.addAuthenticatedUser(roleHolder);

        assertThat(session.getAuthenticatedUser().get()).isSameAs(roleHolder);
    }

    @Test
    public void givenVarArgs_whenAdd_thenCallAddMultipleTimes() {
        UserModel userModel = new UserModel("", 1);
        String string = "str";

        session.add(userModel, string);
        assertThat(session.get(UserModel.class).orElse(null)).isSameAs(userModel);
        assertThat(session.get(String.class).orElse(null)).isSameAs(string);
    }
}
