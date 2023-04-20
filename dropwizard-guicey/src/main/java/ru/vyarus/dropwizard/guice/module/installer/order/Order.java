package ru.vyarus.dropwizard.guice.module.installer.order;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * May be applied to extension in order to order extensions.
 * <p>NOTE: Extension installer must implement {@link Ordered} otherwise annotation will not make any effect.</p>
 * <p>Also, may be applied to installer to order installers. Installers ordering may be useful if you have some
 * edge case, which is normally handled by core installer. Without ordering you will have to manually register
 * all installers to place your installer before. With ordered you could simply annotate your installer and it
 * will be executed before default installers. Also, may be used to order your own installers order.
 * Core installers are ordered from 10 to 100 with gap 10 (to simplify injection of custom installers between them).</p>
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
