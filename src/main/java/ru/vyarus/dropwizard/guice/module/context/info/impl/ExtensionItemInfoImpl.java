package ru.vyarus.dropwizard.guice.module.context.info.impl;

import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;

/**
 * Extension item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 06.07.2016
 */
public class ExtensionItemInfoImpl extends ItemInfoImpl implements ExtensionItemInfo {

    private Class<? extends FeatureInstaller> installedBy;
    private boolean lazy;
    private boolean hk2Managed;

    public ExtensionItemInfoImpl(final Class<?> type) {
        super(ConfigItem.Extension, type);
    }

    @Override
    public Class<? extends FeatureInstaller> getInstalledBy() {
        return installedBy;
    }

    @Override
    public boolean isFromScan() {
        return getRegisteredBy().contains(ClasspathScanner.class);
    }

    @Override
    public boolean isLazy() {
        return lazy;
    }

    @Override
    public boolean isHk2Managed() {
        return hk2Managed;
    }

    public void setLazy(final boolean lazy) {
        this.lazy = lazy;
    }

    public void setHk2Managed(final boolean hk2Managed) {
        this.hk2Managed = hk2Managed;
    }

    public void setInstalledBy(final Class<? extends FeatureInstaller> installedBy) {
        this.installedBy = installedBy;
    }
}
