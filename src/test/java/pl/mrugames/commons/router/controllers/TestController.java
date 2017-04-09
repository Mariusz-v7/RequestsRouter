package pl.mrugames.commons.router.controllers;

import pl.mrugames.commons.router.annotations.Arg;
import pl.mrugames.commons.router.annotations.Controller;
import pl.mrugames.commons.router.annotations.Route;

@Controller("app/test")
public class TestController {

    @Route("route1")
    public String route1() {
        return "route1";
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
}
