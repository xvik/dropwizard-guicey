package ru.vyarus.dropwizard.guice.yaml.support

import com.google.inject.name.Named
import io.dropwizard.Configuration

/**
 * @author Vyacheslav Rusakov
 * @since 23.11.2023
 */
class AnnotatedConfig extends Configuration {

    @Named("test")
    String prop

    @Named("test2")
    String prop2

    @Named("test2")
    Integer prop3

    @CustQualifier
    String custom

}
