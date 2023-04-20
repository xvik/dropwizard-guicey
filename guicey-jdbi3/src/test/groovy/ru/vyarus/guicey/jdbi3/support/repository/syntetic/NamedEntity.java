package ru.vyarus.guicey.jdbi3.support.repository.syntetic;

/**
 * @author Vyacheslav Rusakov
 * @since 24.11.2022
 */
public class NamedEntity extends IdEntity {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
