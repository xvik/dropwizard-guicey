package ru.vyarus.guicey.annotations.lifecycle.module;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import ru.vyarus.guice.ext.core.method.AnnotatedMethodTypeListener;
import ru.vyarus.guicey.annotations.lifecycle.PostStartup;
import ru.vyarus.guicey.annotations.lifecycle.module.collector.MethodsCollector;
import ru.vyarus.guicey.annotations.lifecycle.module.collector.SimpleAnnotationProcessor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.lang.annotation.Annotation;

/**
 * Guice module detects all methods annotated with lifecycle annotations. Annotations triggering done by
 * {@link DropwizardLifecycleListener}.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.guicey.annotations.lifecycle.LifecycleAnnotationsBundle
 * @since 26.11.2018
 */
public class LifecycleAnnotationsModule extends AbstractModule {

    private final Matcher<? super TypeLiteral<?>> typeMatcher;
    private final MethodsCollector collector = new MethodsCollector();

    /**
     * Create lifecycle annotations module.
     *
     * @param typeMatcher target types matcher
     */
    public LifecycleAnnotationsModule(final Matcher<? super TypeLiteral<?>> typeMatcher) {
        this.typeMatcher = typeMatcher;
    }

    /**
     * Used for triggering logic.
     *
     * @return collector instance
     */
    public MethodsCollector getCollector() {
        return collector;
    }

    @Override
    protected void configure() {
        register(collector,
                PostConstruct.class,
                PostStartup.class,
                PreDestroy.class
        );
    }

    /**
     * @param collector   collector instance
     * @param annotations annotation types to search in beans
     */
    @SafeVarargs
    private final void register(final MethodsCollector collector,
                                final Class<? extends Annotation>... annotations) {
        for (Class<? extends Annotation> ann : annotations) {
            bindListener(typeMatcher, new AnnotatedMethodTypeListener<>(
                    ann, new SimpleAnnotationProcessor<>(collector)));
        }
    }
}
