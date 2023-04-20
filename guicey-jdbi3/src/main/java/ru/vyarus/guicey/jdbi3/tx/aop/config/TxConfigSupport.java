package ru.vyarus.guicey.jdbi3.tx.aop.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used with transactional annotations with transaction config (like
 * {@link ru.vyarus.guicey.jdbi3.tx.InTransaction}) in order to apply specified configuration.
 * <p>
 * Note that transactional annotation may not provide any configurations and in this case default
 * transaction configuration will be always used.
 *
 * @author Vyacheslav Rusakov
 * @since 18.09.2018
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface TxConfigSupport {

    /**
     * Note that bean will be obtained from guice context to be able to use injections.
     *
     * @return converter from annotation properties into common tx config object
     */
    Class<? extends TxConfigFactory> value();
}
