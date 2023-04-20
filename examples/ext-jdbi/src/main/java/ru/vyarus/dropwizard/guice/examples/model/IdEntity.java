package ru.vyarus.dropwizard.guice.examples.model;

/**
 * @author Vyacheslav Rusakov
 * @since 09.12.2016
 */
public abstract class IdEntity {

    private long id;
    // for optlock mechanism usage see Crud.java (repository base)
    private Integer version;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
