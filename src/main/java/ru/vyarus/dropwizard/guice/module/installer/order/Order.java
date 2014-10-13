package ru.vyarus.dropwizard.guice.module.installer.order;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * May be applied to extension in order to order extensions.
 * <p>NOTE: Extension installer must implement {@link Ordered} otherwise annotation will not make any effect.</p>
 *
 * @author Vyacheslav Rusakov
 * @since 12.10.2014
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {

    /**
     * @return order
     */
    int value();
}
