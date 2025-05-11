package ru.vyarus.dropwizard.guice.module.context.info.impl;

import io.dropwizard.core.ConfiguredBundle;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.info.DropwizardBundleItemInfo;

/**
 * Dropwizard bundle item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 24.07.2019
 */
public class DropwizardBundleItemInfoImpl extends BundleItemInfoImpl<ConfiguredBundle>
        implements DropwizardBundleItemInfo {

    /**
     * Create disable-only bundle info (only disable marker).
     *
     * @param type bundle type
     */
    public DropwizardBundleItemInfoImpl(final Class<? extends ConfiguredBundle> type) {
        super(ConfigItem.DropwizardBundle, type);
    }

    /**
     * Create bundle item info.
     *
     * @param bundle bundle instance
     */
    public DropwizardBundleItemInfoImpl(final ConfiguredBundle bundle) {
        super(ConfigItem.DropwizardBundle, bundle);
    }

    @Override
    public boolean isTransitive() {
        return getRegisteredBy().stream()
                .noneMatch(type -> ConfigScope.recognize(type) != ConfigScope.DropwizardBundle);
    }

    @Override
    public boolean isDropwizard() {
        return true;
    }
}
