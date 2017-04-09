package pl.mrugames.commons.router.controllers;

public class UserModel {
    private final String name;

    public UserModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
