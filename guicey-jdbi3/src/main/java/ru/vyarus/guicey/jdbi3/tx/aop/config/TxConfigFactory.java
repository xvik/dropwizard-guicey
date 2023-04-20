package ru.vyarus.guicey.jdbi3.tx.aop.config;

import ru.vyarus.guicey.jdbi3.tx.TxConfig;

import java.lang.annotation.Annotation;

/**
 * Factory converts transaction parameters from annotation into common tx config object.
 * Declared with {@link TxConfigSupport} directly on target annotation class.
 * <p>
 * IMPORTANT: Factory is obtained from guice context so prefer annotating it with {@link javax.inject.Singleton}
 * to avoid redundant instantiations (if factory is stateless).
 * <p>
 * Resolved config is cached for target method to avoid duplicate resolutions.
 *
 * @param <T> annotation type
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.guicey.jdbi3.tx.aop.TransactionalInterceptor
 * @since 18.09.2018
 */
public interface TxConfigFactory<T extends Annotation> {

    /**
     * @param annotation annotation to read configuration from
     * @return tx config object (may be null)
     */
    TxConfig build(T annotation);
}
