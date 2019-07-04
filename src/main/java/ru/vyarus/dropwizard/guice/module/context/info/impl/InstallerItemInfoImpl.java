package ru.vyarus.dropwizard.guice.module.context.info.impl;

import com.google.common.collect.Sets;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.info.InstallerItemInfo;

import java.util.Set;

/**
 * Installer item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 06.07.2016
 */
public class InstallerItemInfoImpl extends ClassItemInfoImpl implements InstallerItemInfo {
    private final Set<Class<?>> disabledBy = Sets.newLinkedHashSet();

    public InstallerItemInfoImpl(final Class<?> type) {
        super(ConfigItem.Installer, type);
    }

    @Override
    public Set<Class<?>> getDisabledBy() {
        return disabledBy;
    }

    @Override
    public boolean isEnabled() {
        return disabledBy.isEmpty();
    }

    @Override
    public boolean isFromScan() {
        return getRegisteredBy().contains(ConfigScope.ClasspathScan.getType());
    }
}
