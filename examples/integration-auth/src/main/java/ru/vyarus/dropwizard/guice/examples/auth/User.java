package ru.vyarus.dropwizard.guice.examples.auth;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

/**
 * Authenticated user.
 *
 * @author Vyacheslav Rusakov
 * @since 25.01.2019
 */
public class User implements Principal {

    private String name;
    private List<String> roles;

    public User(String name, String... roles) {
        this.name = name;
        this.roles = Arrays.asList(roles);
    }

    @Override
    public String getName() {
        return name;
    }

    public List<String> getRoles() {
        return roles;
    }
}
