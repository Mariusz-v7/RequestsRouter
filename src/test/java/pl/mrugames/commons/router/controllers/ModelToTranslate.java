package pl.mrugames.commons.router.controllers;

import pl.mrugames.social.i18n.Translatable;

import java.util.Arrays;
import java.util.List;

public class ModelToTranslate implements Translatable {
    private String value = "${i18n.one}";
    private double justTest = 123; // only strings should be replaced
    private final String regularString = "regularString"; // fields without placeholders (${...}) may be final
    private final NestedModelToTranslate nestedModelToTranslate = new NestedModelToTranslate();
    private final List<NestedModelToTranslate> list = Arrays.asList(new NestedModelToTranslate(), new NestedModelToTranslate());


    public String getValue() {
        return value;
    }

    public NestedModelToTranslate getNestedModelToTranslate() {
        return nestedModelToTranslate;
    }

    public List<NestedModelToTranslate> getList() {
        return list;
    }
}
