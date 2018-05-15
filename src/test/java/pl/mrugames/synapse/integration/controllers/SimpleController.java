package pl.mrugames.synapse.integration.controllers;

import pl.mrugames.synapse.RequestMethod;
import pl.mrugames.synapse.annotations.Controller;
import pl.mrugames.synapse.annotations.Route;

@Controller("test/simple-controller")
public class SimpleController {

    @Route("route1")
    public void getRoute1() {
    }

    @Route(value = "route1", method = RequestMethod.POST)
    public void postRoute1() {
    }

    @Route(value = "route1", method = RequestMethod.DELETE)
    public void deleteRoute1() {
    }

    @Route("route2")
    public void getRoute2() {
    }
}
