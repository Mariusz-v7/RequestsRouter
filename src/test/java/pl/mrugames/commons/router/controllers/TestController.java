package pl.mrugames.commons.router.controllers;

import pl.mrugames.commons.router.annotations.Controller;
import pl.mrugames.commons.router.annotations.Route;

@Controller("app/test")
public class TestController {

    @Route("route1")
    public String route1() {
        return "route1";
    }
}
