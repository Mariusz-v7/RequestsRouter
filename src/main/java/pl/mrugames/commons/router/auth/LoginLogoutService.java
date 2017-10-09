package pl.mrugames.commons.router.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.mrugames.commons.router.RouterProperties;
import pl.mrugames.commons.router.sessions.SessionManager;

@Service
public class LoginLogoutService {
    private final AuthenticationManager authenticationManager;
    private final String anonymousKey;
    private final String rememberMeKey;
    private final AnonymousUserFactory<?> anonymousUserFactory;
    private final SessionManager sessionManager;

    private LoginLogoutService(AuthenticationManager authenticationManager,
                               @Value("${" + RouterProperties.ANONYMOUS_KEY + "}") String anonymousKey,
                               @Value("${" + RouterProperties.REMEMBER_ME_KEY + "}") String rememberMeKey,
                               AnonymousUserFactory<?> anonymousUserFactory,
                               SessionManager sessionManager) {
        this.authenticationManager = authenticationManager;
        this.anonymousKey = anonymousKey;
        this.rememberMeKey = rememberMeKey;
        this.anonymousUserFactory = anonymousUserFactory;
        this.sessionManager = sessionManager;

        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
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

    public Authentication login(AbstractAuthenticationToken authenticationToken) {
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    public Authentication loginRemembered(UserDetails userDetails) {
        RememberMeAuthenticationToken rememberMeAuthenticationToken = new RememberMeAuthenticationToken(
                rememberMeKey,
                userDetails,
                userDetails.getAuthorities()
        );

        Authentication authentication = authenticationManager.authenticate(rememberMeAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    public void logout() {
        sessionManager.destroySession();
        initNewContext();
    }
}
