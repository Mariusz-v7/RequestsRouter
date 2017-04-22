package pl.mrugames.commons.router.controllers;

import pl.mrugames.commons.router.RequestMethod;
import pl.mrugames.commons.router.annotations.*;

@Controller("app/test")
public class TestController {

    @Route("route1")
    public String route1() {
        return "route1";
    }

    @Route(value = "route1", method = RequestMethod.POST)
    public String route1WithPost() {
        return "route1_post";
    }

    @Route("sum")
    public int routeWithParams(int a, int b) {
        return a + b;
    }

    @Route("concat")
    public String concatenate(@Arg("a") int a,
                              @Arg("b") String b,
                              @Arg("c") Double c,
                              @Arg(value = "d", defaultValue = "last") String d) {
        return a + b + c + d;
    }

    @Route("player/{playerId}")
    public int incrementPlayerId(@PathVar("playerId") int id) {
        return ++id;
    }

    @Route("player/{playerId}/add/{exp}")
    public double incrementExp(@PathVar("playerId") int id, @PathVar(value = "exp") double exp) {
        return id + exp;
    }

    @Route("account/username")
    public String getUseName(UserModel userModel) {
        return userModel.getName();
    }

    @Route(value = "player/{playerId}", method = RequestMethod.POST)
    public void setDescription(@PathVar("playerId") int id, @Arg("description") String description) {

    }

    @Route(value = "player", method = RequestMethod.POST)
    public void addUser(@Arg("user") UserModel userModel) {

    }

    @Route(value = "session/defaults")
    public void sessionPrimitives(long l, double d, float f, int i, short s, byte b, boolean y, Object o) {

    }

    @Route("only-logged")
    public void onlyLogged() {

    }

    @Route("only-not-logged")
    @OnlyNotLoggedAllowed
    public void onlyNotLogged() {

    }

    @Route("all-allowed")
    @AllAllowed
    public void allAllowed() {

    }

    @Route("admin")
    @AllowedRoles({"admin", "superuser"})
    public void admin() {

    }

    @Route("bad-perms")
    @AllowedRoles("admin")
    @AllAllowed
    @OnlyNotLoggedAllowed
    public void badPerms() {

    }
}
