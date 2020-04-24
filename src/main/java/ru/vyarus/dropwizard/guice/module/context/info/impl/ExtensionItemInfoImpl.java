package ru.vyarus.dropwizard.guice.module.context.info.impl;

import com.google.common.collect.Sets;
import com.google.inject.Binding;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;

import java.util.Set;

/**
 * Extension item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 06.07.2016
 */
public class ExtensionItemInfoImpl extends ClassItemInfoImpl implements ExtensionItemInfo {

    private Class<? extends FeatureInstaller> installedBy;
    private boolean lazy;
    private boolean jerseyManaged;
    private Binding manualBinding;
    private final Set<ItemId> disabledBy = Sets.newLinkedHashSet();
    // little hack used to preserve installer reference during initialization
    private FeatureInstaller installer;
    private boolean optional;

    public ExtensionItemInfoImpl(final Class<?> type) {
        super(ConfigItem.Extension, type);
    }

    @Override
    public Set<ItemId> getDisabledBy() {
        return disabledBy;
    }

    @Override
    public boolean isEnabled() {
        return disabledBy.isEmpty();
    }

    @Override
    public Class<? extends FeatureInstaller> getInstalledBy() {
        return installedBy;
    }

    @Override
    public boolean isFromScan() {
        return getRegisteredBy().contains(ConfigScope.ClasspathScan.getKey());
    }

    @Override
    public boolean isLazy() {
        return lazy;
    }

    @Override
    public boolean isJerseyManaged() {
        return jerseyManaged;
    }

    @Override
    public boolean isGuiceBinding() {
        return manualBinding != null;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    public void setLazy(final boolean lazy) {
        this.lazy = lazy;
    }

    public void setJerseyManaged(final boolean jerseyManaged) {
        this.jerseyManaged = jerseyManaged;
    }

    public void setManualBinding(final Binding manualBinding) {
        this.manualBinding = manualBinding;
    }

    public Binding getManualBinding() {
        return manualBinding;
    }

    public FeatureInstaller getInstaller() {
        return installer;
    }

    public void setInstaller(final FeatureInstaller installer) {
        this.installer = installer;
        this.installedBy = installer.getClass();
    }

    public void setOptional(final boolean optional) {
        this.optional = optional;
    }
}
