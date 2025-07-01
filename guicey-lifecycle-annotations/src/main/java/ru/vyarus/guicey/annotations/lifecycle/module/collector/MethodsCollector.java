package ru.vyarus.guicey.annotations.lifecycle.module.collector;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Registry for detected annotated methods. Used to collect and then process all found methods by annotation.
 *
 * @author Vyacheslav Rusakov
 * @since 27.11.2018
 */
public class MethodsCollector {
    private final Logger logger = LoggerFactory.getLogger(MethodsCollector.class);

    private final Multimap<Class<? extends Annotation>, MethodInstance> listeners = LinkedListMultimap.create();

    // first it prevents duplicate lifecycle call
    // second it used to detect late registrations for immediate execution
    private final List<Class<? extends Annotation>> processed = new ArrayList<>();

    /**
     * Register lifecycle method.
     *
     * @param annotation lifecycle annotation
     * @param instance   object instance
     * @param method     annotated method
     */
    public void register(final Class<? extends Annotation> annotation,
                         final Object instance,
                         final Method method) {
        final MethodInstance methodInstance = new MethodInstance(instance, method);
        listeners.put(annotation, methodInstance);
        // could appear due to JIT (when bean not registered and being instantiated after injector creation (on demand))
        if (processed.contains(annotation)) {
            logger.warn("@{} listener registered after event processing: {}. "
                    + "This could happen when bean is not registered and instantiated on demand "
                    + "(by guice JIT). "
                    + "To avoid this warning register bean directly in guice module. "
                    + "Immediate initialization will be performed.", annotation.getSimpleName(), methodInstance);

            callInstance(annotation, methodInstance, true);
        }
    }

    /**
     * Called to process all methods annotated with provided annotation.
     * In case of exception, it would be propagated.
     *
     * @param annotation target method annotation
     */
    public void call(final Class<? extends Annotation> annotation) {
        doCall(annotation, false);
    }

    /**
     * Called to process all methods annotated with provided annotation.
     * If method execution fails, the exception is just logged without propagation. So all annotated methods would
     * be called.
     *
     * @param annotation target method annotation
     */
    public void safeCall(final Class<? extends Annotation> annotation) {
        doCall(annotation, true);
    }

    private void doCall(final Class<? extends Annotation> annotation, final boolean safe) {
        Preconditions.checkState(!processed.contains(annotation),
                "Lifecycle @%s methods were already processed", annotation.getSimpleName());
        processed.add(annotation);

        final Collection<MethodInstance> methods = listeners.get(annotation);
        if (!methods.isEmpty()) {
            logger.debug("Executing @{} lifecycle methods", annotation.getSimpleName());
            for (MethodInstance method : methods) {
                callInstance(annotation, method, safe);
            }
        }
    }

    private void callInstance(final Class<? extends Annotation> annotation,
                              final MethodInstance method,
                              final boolean safe) {
        try {
            method.call();
        } catch (Exception ex) {
            if (safe) {
                // method name would be contained in the exception
                logger.error("Failed to process @" + annotation.getSimpleName() + " annotated method", ex);
            } else {
                throw ex;
            }
        }
    }
}
