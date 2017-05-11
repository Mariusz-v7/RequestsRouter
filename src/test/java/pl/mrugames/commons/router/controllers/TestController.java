package pl.mrugames.commons.router.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import pl.mrugames.commons.router.RequestMethod;
import pl.mrugames.commons.router.annotations.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Controller("app/test")
public class TestController {

    @Route("route1")
    @AllAllowed
    public String route1() {
        return "route1";
    }

    @AllAllowed
    @Route(value = "route1", method = RequestMethod.POST)
    public String route1WithPost() {
        return "route1_post";
    }

    @AllAllowed
    @Route("sum")
    public int routeWithParams(int a, int b) {
        return a + b;
    }

    @Route("concat")
    @AllAllowed
    public String concatenate(@Arg("a") int a,
                              @Arg("b") String b,
                              @Arg("c") Double c,
                              @Arg(value = "d", defaultValue = "last") String d) {
        return a + b + c + d;
    }

    @AllAllowed
    @Route("player/{playerId}")
    public int incrementPlayerId(@PathVar("playerId") int id) {
        return ++id;
    }

    @AllAllowed
    @Route("player/{playerId}/add/{exp}")
    public double incrementExp(@PathVar("playerId") int id, @PathVar(value = "exp") double exp) {
        return id + exp;
    }

    @Route("account/username")
    @AllAllowed
    public String getUseName(UserModel userModel) {
        return userModel.getName();
    }

    @Route(value = "player/{playerId}", method = RequestMethod.POST)
    @AllAllowed
    public void setDescription(@PathVar("playerId") int id, @Arg("description") String description) {

    }

    @AllAllowed
    @Route(value = "player", method = RequestMethod.POST)
    public void addUser(@Arg("user") UserModel userModel) {

    }

    @AllAllowed
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

    @Route("re-return-obj")
    public UserModel reReturnObj(UserModel userModel) {
        return userModel;
    }

    @Route("re-return-int")
    public int reReturnPrimitive(int i) {
        return i;
    }

    @Route("re-return-bool")
    public boolean reReturnPrimitiveBool(boolean i) {
        return i;
    }

    @AllAllowed
    @Route("validation/{a}/{b}")
    public void validation(@Min(0) @PathVar("a") int a,
                           @Max(2) @PathVar("b") int b) {

    }

    @AllAllowed
    @Route("validation2")
    public void validation2(@Min(0) @Arg("a") int a,
                            @Max(2) @Arg("b") int b) {

    }

    @Route("json")
    public void json(@Arg("arg1") String a, @Arg("arg2") String b) {

    }

    @PreAuthorize("denyAll")
    @Route("deny")
    public void deny() {

    }
}
