package pl.mrugames.commons.router.permissions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import pl.mrugames.commons.router.ResponseStatus;
import pl.mrugames.commons.router.sessions.Session;

import java.util.Collections;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(BlockJUnit4ClassRunner.class)
@SuppressWarnings("unchecked")
public class PermissionCheckerSpec {
    private PermissionChecker permissionChecker;
    private Session session;

    @Before
    public void before() {
        session = new Session("mock", mock(Consumer.class));
        permissionChecker = new PermissionChecker();
    }

    @Test
    public void givenSessionHasNoUserLogged_andRouteRequiresLoggedUsers_whenCheckPermissions_thenDeny() {
        ResponseStatus status = permissionChecker.checkPermissions(session, AccessType.ONLY_LOGGED_IN, Collections.emptyList()).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.NOT_AUTHORIZED);
    }

    @Test
    public void givenSessionHasUserLogged_andRouteRequiresIt_whenCheckPermissions_thenOk() {
        session.add(RoleHolder.class, mock(RoleHolder.class));

        ResponseStatus status = permissionChecker.checkPermissions(session, AccessType.ONLY_LOGGED_IN, Collections.emptyList()).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.OK);
    }

    @Test
    public void givenSessionHasNoUserLogged_andRouteRequiresNoLogged_whenCheckPermissions_thenOk() {
        ResponseStatus status = permissionChecker.checkPermissions(session, AccessType.ONLY_NOT_LOGGED_IN, Collections.emptyList()).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.OK);
    }

    @Test
    public void givenSessionHasUserLogged_andRouteRequiresNoLogged_thenDeny() {
        session.add(RoleHolder.class, mock(RoleHolder.class));

        ResponseStatus status = permissionChecker.checkPermissions(session, AccessType.ONLY_NOT_LOGGED_IN, Collections.emptyList()).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.ONLY_FOR_NOT_AUTHORIZED);
    }

    @Test
    public void givenSessionHasNoUserLogged_andRouteRequiresRoles_thenDeny() {
        ResponseStatus status = permissionChecker.checkPermissions(session, AccessType.ONLY_WITH_SPECIFIC_ROLES, Collections.emptyList()).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.PERMISSION_DENIED);
    }

    @Test
    public void givenSessionHasUserWithoutPermissions_thenDeny() {
        RoleHolder roleHolder = mock(RoleHolder.class);
        doReturn(Collections.singletonList("user")).when(roleHolder).getRoles();
        session.add(RoleHolder.class, roleHolder);

        ResponseStatus status = permissionChecker.checkPermissions(session, AccessType.ONLY_WITH_SPECIFIC_ROLES, Collections.singletonList("admin")).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.PERMISSION_DENIED);
    }

    @Test
    public void givenSessionHasUserWithPermissions_thenOk() {
        RoleHolder roleHolder = mock(RoleHolder.class);
        doReturn(Collections.singletonList("admin")).when(roleHolder).getRoles();
        session.add(RoleHolder.class, roleHolder);

        ResponseStatus status = permissionChecker.checkPermissions(session, AccessType.ONLY_WITH_SPECIFIC_ROLES, Collections.singletonList("admin")).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.OK);
    }

    @Test
    public void givenSessionHasNoUser_andRouteHasAllAllowed_thenOk() {
        ResponseStatus status = permissionChecker.checkPermissions(session, AccessType.ALL_ALLOWED, Collections.emptyList()).getResponseStatus();
        assertThat(status).isEqualTo(ResponseStatus.OK);
    }

}
