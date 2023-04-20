package ru.vyarus.guicey.jdbi.support

import io.dropwizard.Configuration
import io.dropwizard.db.DataSourceFactory

import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * @author Vyacheslav Rusakov
 * @since 05.12.2016
 */
class SampleConfiguration extends Configuration {

    @Valid
    @NotNull
    DataSourceFactory database = new DataSourceFactory();
}
