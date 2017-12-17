package pl.mrugames.commons.router.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import pl.mrugames.commons.router.RequestMethod;
import pl.mrugames.commons.router.annotations.Arg;
import pl.mrugames.commons.router.annotations.Controller;
import pl.mrugames.commons.router.annotations.PathVar;
import pl.mrugames.commons.router.annotations.Route;
import pl.mrugames.commons.router.arg_resolvers.ExampleType;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Controller("app/test")
public class TestController {

    public static class ConcatRouteDTO {
        private final int a;
        private final String b;
        private final Double c;
        private final String d;

        public ConcatRouteDTO(int a, String b, double c, String d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        public int getA() {
            return a;
        }

        public String getB() {
            return b;
        }

        public double getC() {
            return c;
        }

        public String getD() {
            return d;
        }
    }


    public static class ConcatRouteWithOptionalDTO {
        private final int a;
        private final String b;
        private final Double c;

        public ConcatRouteWithOptionalDTO(int a, String b, Double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public int getA() {
            return a;
        }

        public String getB() {
            return b;
        }

        public Double getC() {
            return c;
        }
    }

    public static class ConcatRouteInvalidDTO {
        private final int a;
        private final String b;

        public ConcatRouteInvalidDTO(int a, String b) {
            this.a = a;
            this.b = b;
        }

        public int getA() {
            return a;
        }

        public String getB() {
            return b;
        }
    }

    @Autowired
    private ValidateMe validateMe;

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
    public void onlyNotLogged() {

    }

    @Route("all-allowed")
    public void allAllowed() {

    }

    @Route("admin")
    public void admin() {

    }

    @Route("bad-perms")
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

    @Route("validation/{a}/{b}")
    public void validation(@Min(0) @PathVar("a") int a,
                           @Max(2) @PathVar("b") int b) {

    }

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

    @Route("validate-deeper/{a}")
    public void validateDeeper(@PathVar("a") int value) {
        validateMe.validateMe(true, value);
    }

    @Route("validate-deeper2/{a}/{b}")
    public void validateDeeper(@PathVar("a") int a, @PathVar("b") int b) {
        validateMe.v2(b, a);
    }

    @Route("one-optional")
    public String oneOptional(@Arg(value = "one", defaultValue = "") String one) {
        return one;
    }

    @Route("generic-list")
    public void genericList(@Arg("list") List<ExampleType> list) {

    }
}
