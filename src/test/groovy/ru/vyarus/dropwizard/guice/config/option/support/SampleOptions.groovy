package ru.vyarus.dropwizard.guice.config.option.support

import ru.vyarus.dropwizard.guice.module.context.option.Option

/**
 * @author Vyacheslav Rusakov
 * @since 13.08.2016
 */
enum SampleOptions implements Option {

    BoolFalse(Boolean, false),
    BoolTrue(Boolean, true),
    NullOption(String, null);

    Class type
    Object value

    SampleOptions(Class type, Object value) {
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