package ru.vyarus.guicey.annotations.lifecycle.module.collector;

import ru.vyarus.guice.ext.core.method.MethodPostProcessor;
import ru.vyarus.guice.ext.core.util.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Post processor implementation for registration of all found annotated methods inside collector.
 *
 * @param <T> annotation type
 * @author Vyacheslav Rusakov
 * @since 27.11.2018
 */
public class SimpleAnnotationProcessor<T extends Annotation> implements MethodPostProcessor<T> {

    private final MethodsCollector collector;

    /**
     * Create annotation processor.
     *
     * @param collector annotated methods collector
     */
    public SimpleAnnotationProcessor(final MethodsCollector collector) {
        this.collector = collector;
    }

    @Override
    public void process(final T annotation, final Method method, final Object instance) throws Exception {
        Utils.checkNoParams(method);
        collector.register(annotation.annotationType(), instance, method);
    }
}
