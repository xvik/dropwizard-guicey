package ru.vyarus.dropwizard.guice.module.context.info;

import io.dropwizard.ConfiguredBundle;

/**
 * Dropwizard bundle configuration information.
 * <p>
 * Note that only directly registered dropwizard bundles (through guicey api) are tracked! This means that
 * bundles registered directly into dropwizard bootstrap object (and their transitives) are not visible.
 *
 * @author Vyacheslav Rusakov
 * @since 24.07.2019
 */
public interface DropwizardBundleItemInfo extends BundleItemInfo<ConfiguredBundle> {

}
