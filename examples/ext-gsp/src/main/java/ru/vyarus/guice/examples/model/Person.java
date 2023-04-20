package ru.vyarus.guice.examples.model;

/**
 * @author Vyacheslav Rusakov
 * @since 23.10.2019
 */
public class Person {

    private String name;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
