package ru.vyarus.dropwizard.guice.module.yaml.bind;

import com.google.inject.internal.Annotations;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for using {@link Config} annotation as guice bindings qualifier.
 * Supposed internal usage only ({@link ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule}).
 * <p>
 * Implementation is the same as core guice's {@code com.google.inject.name.NamedImpl}.
 *
 * @author Vyacheslav Rusakov
 * @since 05.05.2018
 */
public class ConfigImpl implements Config, Serializable {

    private static final long serialVersionUID = 0;

    /**
     * Value.
     */
    private final String val;

    /**
     * Create configuration qualifier.
     *
     * @param val qualifier value
     */
    public ConfigImpl(final String val) {
        this.val = checkNotNull(val, "name");
    }

    @Override
    public String value() {
        return this.val;
    }

    @Override
    public int hashCode() {
        // This is specified in java.lang.Annotation.
        return (127 * "value".hashCode()) ^ val.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Config)) {
            return false;
        }

        final Config other = (Config) o;
        return val.equals(other.value());
    }

    @Override
    public String toString() {
        return "@" + Config.class.getName() + "(value=" + Annotations.memberValueString(val) + ")";
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Config.class;
    }
}
