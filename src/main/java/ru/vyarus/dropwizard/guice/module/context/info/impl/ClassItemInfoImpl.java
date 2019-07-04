package ru.vyarus.dropwizard.guice.module.context.info.impl;

import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.info.ClassItemInfo;

/**
 * Class item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 04.07.2019
 */
public class ClassItemInfoImpl extends ItemInfoImpl implements ClassItemInfo {

    public ClassItemInfoImpl(final ConfigItem itemType, final Class<?> type) {
        super(itemType, type);
    }

    @Override
    public ConfigScope getRegistrationScopeType() {
        return getRegistrationScopes().isEmpty() ? null : ConfigScope.recognize(getRegistrationScopes().get(0));
    }

    @Override
    public Class<?> getRegistrationScope() {
        return getRegistrationScopes().isEmpty() ? null : getRegistrationScopes().get(0);
    }
}
