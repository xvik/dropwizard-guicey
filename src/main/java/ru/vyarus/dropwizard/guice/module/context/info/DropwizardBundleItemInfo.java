package ru.vyarus.dropwizard.guice.module.context.info;

import ru.vyarus.dropwizard.guice.module.context.info.sign.DisableSupport;

/**
 * Dropwizard bundle configuration information.
 * <p>
 * Note that only directly registered dropwizard bundles (through guicey api) are tracked!
 *
 * @author Vyacheslav Rusakov
 * @since 24.07.2019
 */
public interface DropwizardBundleItemInfo extends InstanceItemInfo, DisableSupport {
}
