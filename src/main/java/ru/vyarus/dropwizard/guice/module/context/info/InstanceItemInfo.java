package ru.vyarus.dropwizard.guice.module.context.info;

import java.util.List;

/**
 * Base interface for instance configurations (bundles, modules). Multiple instances could be actually registered
 * and so item info have to hold all registered instances (because otherwise it would be impossible to preserve
 * information about duplicates and actually used items).
 *
 * @author Vyacheslav Rusakov
 * @since 03.07.2019
 */
public interface InstanceItemInfo extends ItemInfo {

    /**
     * Note: all affected scopes could be revealed with {@link #getRegistrationScopes()}.
     *
     * @param scope items scope (see {@link ru.vyarus.dropwizard.guice.module.context.ConfigScope})
     * @return list of registered item instances or empty list if no items registered in scope
     */
    List<Object> getRegistrationsByScope(Class<?> scope);

    /**
     * Note: all affected scopes could be revealed with {@link #getRegistrationScopes()}.
     *
     * @param scope items scope (see {@link ru.vyarus.dropwizard.guice.module.context.ConfigScope})
     * @return list of duplicate (not installed) item instances or empty list if no duplicates in scope
     */
    List<Object> getDuplicatesByScope(Class<?> scope);

    /**
     * @return count of registered items (without duplicates)
     */
    int getRegistrations();
}
