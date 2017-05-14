package pl.mrugames.commons.router.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.mrugames.commons.router.RouterProperties;
import pl.mrugames.commons.router.sessions.Session;

@Service
public class LoginLogoutService {
    private final AuthenticationManager authenticationManager;
    private final String anonymousKey;
    private final AnonymousUserFactory<?> anonymousUserFactory;

    private LoginLogoutService(AuthenticationManager authenticationManager,
                               @Value("${" + RouterProperties.ANONYMOUS_KEY + "}") String anonymousKey,
                               AnonymousUserFactory<?> anonymousUserFactory) {
        this.authenticationManager = authenticationManager;
        this.anonymousKey = anonymousKey;
        this.anonymousUserFactory = anonymousUserFactory;
    }

    public void initNewContext() {
        SecurityContextHolder.clearContext();

        AnonymousAuthenticationToken token = new AnonymousAuthenticationToken(
                anonymousKey,
                anonymousUserFactory.createAnonymousUser(),
                AuthorityUtils.createAuthorityList(anonymousUserFactory.getRolesForAnonymousUser())
        );

        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public Authentication login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    public void logout() {
        Session.getLocalSession().ifPresent(Session::destroy);
        initNewContext();
    }
}
