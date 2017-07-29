package integration;

import pl.mrugames.commons.router.annotations.Arg;
import pl.mrugames.commons.router.annotations.Controller;
import pl.mrugames.commons.router.annotations.Route;

@Controller("integration")
public class IntegController {

    @Route
    public boolean basicTest(@Arg("a") int a) {
        return a < 10;
    }
}
