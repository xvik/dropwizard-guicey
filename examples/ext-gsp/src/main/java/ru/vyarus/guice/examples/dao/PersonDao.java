package ru.vyarus.guice.examples.dao;

import ru.vyarus.guice.examples.model.Person;

/**
 * @author Vyacheslav Rusakov
 * @since 23.10.2019
 */
public class PersonDao {

    public Person find(int id) {
        return new Person("John Doe " + id);
    }
}
