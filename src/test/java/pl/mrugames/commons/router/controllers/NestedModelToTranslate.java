package pl.mrugames.commons.router.controllers;

import pl.mrugames.commons.router.annotations.Translate;

@Translate
public class NestedModelToTranslate {
    private String value = "${i18n.two}";

    public String getValue() {
        return value;
    }
}
