package ru.vyarus.dropwizard.guice.module.context.info.impl;

import com.google.common.collect.Sets;
import io.dropwizard.ConfiguredBundle;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.info.DropwizardBundleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;

import java.util.Set;

/**
 * Dropwizard bundle item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 24.07.2019
 */
public class DropwizardBundleItemInfoImpl extends InstanceItemInfoImpl implements DropwizardBundleItemInfo {

    private final Set<ItemId> disabledBy = Sets.newLinkedHashSet();

    // disable only
    public DropwizardBundleItemInfoImpl(final Class<? extends ConfiguredBundle> type) {
        super(ConfigItem.DropwizardBundle, type);
    }

    public DropwizardBundleItemInfoImpl(final ConfiguredBundle bundle) {
        super(ConfigItem.DropwizardBundle, bundle);
    }

    @Override
    public Set<ItemId> getDisabledBy() {
        return disabledBy;
    }

    @Override
    public boolean isEnabled() {
        return disabledBy.isEmpty();
    }

}
