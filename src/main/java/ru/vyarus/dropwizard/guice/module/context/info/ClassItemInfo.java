package ru.vyarus.dropwizard.guice.module.context.info;

import ru.vyarus.dropwizard.guice.module.context.ConfigScope;

/**
 * Base interface for class configurations (extensions, installer).
 *
 * @author Vyacheslav Rusakov
 * @since 04.07.2019
 */
public interface ClassItemInfo extends ItemInfo {

    /**
     * Shortcut for {@link #getRegistrationScopes()}. Useful because class items may have not more then one
     * registration scope.
     *
     * @return registration scope type or null
     */
    ConfigScope getRegistrationScopeType();

    /**
     * Shortcut for {@link #getRegistrationScopes()}. Useful because class items may have not more then one
     * registration scope.
     *
     * @return registration scope or null
     */
    Class<?> getRegistrationScope();
}
