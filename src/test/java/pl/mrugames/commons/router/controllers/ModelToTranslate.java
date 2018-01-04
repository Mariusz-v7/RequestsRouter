package pl.mrugames.commons.router.controllers;

import pl.mrugames.commons.router.annotations.Translate;

@Translate
public class ModelToTranslate {
    private String value = "${i18n.one}";
    private double justTest = 123; // only strings should be replaced
    private final String regularString = "regularString"; // fields without placeholders (${...}) may be final
    private final NestedModelToTranslate nestedModelToTranslate = new NestedModelToTranslate();


    public String getValue() {
        return value;
    }

    public NestedModelToTranslate getNestedModelToTranslate() {
        return nestedModelToTranslate;
    }
}
