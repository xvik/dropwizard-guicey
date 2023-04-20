package ru.vyarus.guicey.annotations.lifecycle.module.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Instance method abstraction. Holds both method and target instance object to easily perform call.
 *
 * @author Vyacheslav Rusakov
 * @since 27.11.2018
 */
public class MethodInstance {
    private final Logger logger = LoggerFactory.getLogger(MethodInstance.class);

    private final Object instance;
    private final Method method;

    public MethodInstance(final Object instance, final Method method) {
        this.instance = instance;
        this.method = method;
    }

    /**
     * Calls method on instance.
     * <p>
     * If method execution fails, exception is propagated.
     */
    public void call() {
        try {
            logger.debug("Executing method {}", this);
            method.invoke(instance);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute method " + this, ex);
        }
    }

    @Override
    public String toString() {
        return instance.getClass().getSimpleName() + "." + method.getName()
                + " of instance " + instance.toString();
    }
}
