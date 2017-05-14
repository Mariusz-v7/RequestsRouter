package pl.mrugames.commons.router.auth;

public interface AnonymousUserFactory<T> {
    T createAnonymousUser();

    String[] getRolesForAnonymousUser();
}
