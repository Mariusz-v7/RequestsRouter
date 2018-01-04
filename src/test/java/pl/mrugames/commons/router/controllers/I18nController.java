package pl.mrugames.commons.router.controllers;

import pl.mrugames.commons.router.annotations.Controller;
import pl.mrugames.commons.router.annotations.Route;

@Controller("i18n")
public class I18nController {

    @Route("return-string")
    public String returnString() {
        return "${i18n.simple_string}";
    }
}
