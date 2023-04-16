package ru.vyarus.dropwizard.guice.examples.model;

import jakarta.persistence.*;

/**
 * Sample hibernate entity.
 *
 * @author Vyacheslav Rusakov
 * @since 12.06.2016
 */
@Entity
@Table(name = "Sample")
public class Sample {

    @Id
    @GeneratedValue
    private long id;
    @Version
    private long version;

    private String name;

    public Sample() {
    }

    public Sample(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
