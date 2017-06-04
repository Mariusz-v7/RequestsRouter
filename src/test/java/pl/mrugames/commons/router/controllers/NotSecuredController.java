package pl.mrugames.commons.router.controllers;

import pl.mrugames.commons.router.annotations.Controller;
import pl.mrugames.commons.router.annotations.Route;

@Controller("app/not-secured")
public class NotSecuredController {
    @Route("route")
    public String route() {
        return "route";
    }
}
