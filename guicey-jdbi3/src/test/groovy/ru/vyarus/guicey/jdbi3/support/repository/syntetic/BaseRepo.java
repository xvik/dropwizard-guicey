package ru.vyarus.guicey.jdbi3.support.repository.syntetic;

/**
 * @author Vyacheslav Rusakov
 * @since 23.11.2022
 */
public interface BaseRepo<T extends IdEntity> {

    long save(T sample);

    T get(long id);
}
