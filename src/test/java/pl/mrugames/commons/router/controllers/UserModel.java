package pl.mrugames.commons.router.controllers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import pl.mrugames.commons.router.permissions.RoleHolder;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class UserModel implements Serializable, Interface, RoleHolder {
    private final int id;
    private final String name;

    @JsonCreator
    public UserModel(@JsonProperty("name") String name, @JsonProperty("id") int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserModel userModel = (UserModel) o;

        if (id != userModel.id) return false;
        return name != null ? name.equals(userModel.name) : userModel.name == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public List<String> getRoles() {
        return Collections.emptyList();
    }
}
