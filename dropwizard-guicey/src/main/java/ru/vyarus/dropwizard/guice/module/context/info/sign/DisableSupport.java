package ru.vyarus.dropwizard.guice.module.context.info.sign;

import ru.vyarus.dropwizard.guice.module.context.info.ItemId;

import java.util.Set;

/**
 * Disable sign indicates that item could be disabled.
 *
 * @author Vyacheslav Rusakov
 * @since 06.07.2016
 */
public interface DisableSupport {

    /**
     * Item may be disabled either from root application class or from
     * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle}. For application,
     * {@link io.dropwizard.core.Application} class stored as context and for guicey bundle actual bundle instance id
     * is stored.
     *
     * @return contexts where item was disabled or empty collection
     */
    Set<ItemId> getDisabledBy();

    /**
     * Item is enabled if no one disable it ({@link #getDisabledBy()} is empty).
     *
     * @return true if item enabled, false otherwise
     */
    boolean isEnabled();
}
