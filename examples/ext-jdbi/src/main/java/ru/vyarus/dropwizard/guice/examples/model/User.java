package ru.vyarus.dropwizard.guice.examples.model;

/**
 * @author Vyacheslav Rusakov
 * @since 09.12.2016
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
