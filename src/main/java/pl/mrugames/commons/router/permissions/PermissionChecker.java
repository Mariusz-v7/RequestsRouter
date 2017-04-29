package pl.mrugames.commons.router.permissions;

import org.springframework.stereotype.Service;
import pl.mrugames.commons.router.Mono;
import pl.mrugames.commons.router.ResponseStatus;
import pl.mrugames.commons.router.sessions.Session;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionChecker {
    PermissionChecker() {
    }

    public Mono<?> checkPermissions(Session session, AccessType accessType, List<String> allowedRoles) {
        switch (accessType) {
            case ONLY_LOGGED_IN:
                return session.get(RoleHolder.class).isPresent() ? Mono.OK : Mono.of(ResponseStatus.NOT_AUTHORIZED);
            case ONLY_NOT_LOGGED_IN:
                return session.get(RoleHolder.class).isPresent() ? Mono.of(ResponseStatus.ONLY_FOR_NOT_AUTHORIZED) : Mono.OK;
            case ONLY_WITH_SPECIFIC_ROLES:
                Optional<RoleHolder> roleHolder = session.get(RoleHolder.class);
                if (roleHolder.isPresent()) {
                    return checkRoles(roleHolder.get(), allowedRoles);
                }

                return Mono.of(ResponseStatus.PERMISSION_DENIED);
            case ALL_ALLOWED:
                return Mono.OK;
            default:
                return Mono.of(ResponseStatus.INTERNAL_ERROR);
        }
    }

    private Mono<?> checkRoles(RoleHolder roleHolder, List<String> allowedRoles) {
        List<String> roles = roleHolder.getRoles();
        for (String role : roles) {
            if (allowedRoles.contains(role)) {
                return Mono.OK;
            }
        }

        return Mono.of(ResponseStatus.PERMISSION_DENIED);
    }
}
