package ru.vyarus.dropwizard.guice.examples.model;

/**
 * @author Vyacheslav Rusakov
 * @since 01.11.2018
 */
public class User extends IdEntity {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
