package ru.vyarus.dropwizard.guice.config.option.support

import ru.vyarus.dropwizard.guice.module.context.option.Option

/**
 * @author Vyacheslav Rusakov
 * @since 13.08.2016
 */
enum OtherOptions implements Option {

    Opt1(String, "foo"),
    Opt2(String, "bar");

    Class type
    Object value

    OtherOptions(Class type, Object value) {
        this.type = type
        this.value = value
    }

    @Override
    Class getType() {
        return type
    }

    @Override
    Object getDefaultValue() {
        return value
    }
}
