package ru.vyarus.guicey.annotations.lifecycle;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for marking initialization methods on guice beans to be called after server startup. Note that
 * this method will not be called under guicey lightweight test or environment command start.
 * <p>
 * In contrast to {@link javax.annotation.PostConstruct} which is called on
 * {@link io.dropwizard.lifecycle.Managed#start()} (during server initialization),
 * annotated methods are called only after complete server startup (when application is ready to serve requests).
 *
 * @author Vyacheslav Rusakov
 * @since 08.11.2018
 * @see LifecycleAnnotationsBundle
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface PostStartup {
}
