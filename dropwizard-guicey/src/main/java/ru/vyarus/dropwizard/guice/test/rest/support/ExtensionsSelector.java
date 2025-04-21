package ru.vyarus.dropwizard.guice.test.rest.support;

import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.Filters;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class to implement extension selector from guicey info.
 *
 * @author Vyacheslav Rusakov
 * @since 25.02.2025
 */
public class ExtensionsSelector {

    private final GuiceyConfigurationInfo info;

    public ExtensionsSelector(final GuiceyConfigurationInfo info) {
        this.info = info;
    }

    /**
     * @return list of enabled rest resources
     */
    public List<Class<?>> getResources() {
        return info.getData().getItems(Filters.extensions()
                        .and(Filters.installedBy(ResourceInstaller.class))
                        .and(Filters.enabled()))
                .stream()
                .map(ItemId::getType)
                .sorted(Comparator.comparing(Class::getSimpleName))
                .collect(Collectors.toList());
    }

    /**
     * @return count of disabled rest resources
     */
    public int getDisabledResourcesCount() {
        return info.getData().getItems(Filters.extensions()
                .and(Filters.installedBy(ResourceInstaller.class))
                .and(Filters.disabled())).size();
    }

    /**
     * @return list of enabled jersey extensions
     */
    public List<Class<?>> getExtensions() {
        return info.getData().getItems(Filters.extensions()
                        .and(Filters.jerseyExtension()
                                .and(Filters.installedBy(ResourceInstaller.class).negate()))
                        .and(Filters.enabled()))
                .stream()
                .map(ItemId::getType)
                .sorted(Comparator.comparing(Class::getSimpleName))
                .collect(Collectors.toList());
    }

    /**
     * @return count of disabled jersey extensions
     */
    public int getDisabledExtensionsCount() {
        return info.getData().getItems(Filters.extensions()
                .and(Filters.jerseyExtension()
                        .and(Filters.installedBy(ResourceInstaller.class).negate()))
                .and(Filters.disabled())).size();
    }
}
