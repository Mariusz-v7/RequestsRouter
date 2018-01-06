package pl.mrugames.commons.router.controllers;


import pl.mrugames.social.i18n.Translatable;

public class NestedModelToTranslate implements Translatable {
    private String value = "${i18n.two}";

    public String getValue() {
        return value;
    }
}
