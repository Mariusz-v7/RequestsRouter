package pl.mrugames.commons.router.controllers;

import pl.mrugames.commons.router.annotations.Controller;
import pl.mrugames.commons.router.annotations.Route;

@Controller("ping")
public class PingController {
    @Route
    public void ping() {

    }
}
