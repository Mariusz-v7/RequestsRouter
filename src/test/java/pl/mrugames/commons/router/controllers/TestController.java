package pl.mrugames.commons.router.controllers;

import pl.mrugames.commons.router.RequestMethod;
import pl.mrugames.commons.router.annotations.Arg;
import pl.mrugames.commons.router.annotations.Controller;
import pl.mrugames.commons.router.annotations.Route;

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
    public int incrementPlayerId(@Arg("playerId") int id) {
        return ++id;
    }

    @Route("player/{playerId}/add/{exp}")
    public double incrementExp(@Arg("playerId") int id, @Arg(value = "exp") double exp) {
        return id + exp;
    }

}
