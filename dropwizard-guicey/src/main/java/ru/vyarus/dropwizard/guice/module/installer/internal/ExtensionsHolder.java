package ru.vyarus.dropwizard.guice.module.installer.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ru.vyarus.dropwizard.guice.module.context.info.impl.ExtensionItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.OrderComparator;
import ru.vyarus.dropwizard.guice.module.installer.order.Ordered;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bean used to hold found extensions (after scan with installers) to register them in dropwizard after
 * injector creation.
 * <p>
 * Internal api. Use {@link ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo} instead.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
public class ExtensionsHolder {
    private final List<FeatureInstaller> installers;
    private final List<ScanItem> scanExtensions = new ArrayList<>();
    private List<ExtensionItemInfoImpl> extensionsData;
    private final List<Class<? extends FeatureInstaller>> installerTypes;
    private final Map<Class<? extends FeatureInstaller>, List<Class<?>>> extensions = Maps.newHashMap();

    public ExtensionsHolder(final List<FeatureInstaller> installers) {
        this.installers = installers;
        this.installerTypes = Lists.transform(installers, FeatureInstaller::getClass);
    }

    /**
     * @return extensions recognized by classpath scan
     */
    public List<ScanItem> getScanExtensions() {
        return scanExtensions;
    }

    /**
     * Auto scan performed under configuration phase, but actual extensions registration only in run phase
     * because manual extensions could be added at run phase (and manual extensions must be registered in priority).
     *
     * @param candidate potential extension
     */
    public boolean acceptScanCandidate(final Class<?> candidate) {
        final FeatureInstaller installer = ExtensionsSupport.findInstaller(candidate, installers);
        final boolean recognized = installer != null;
        if (recognized) {
            scanExtensions.add(new ScanItem(candidate, installer));
        }
        return recognized;
    }

    /**
     * Prepare known extensions for installation.
     *
     * @param extensionsData extensions data
     */
    public void registerExtensions(final List<ExtensionItemInfoImpl> extensionsData) {
        this.extensionsData = extensionsData;
        // distribute extensions by installer for initialization
        for (ExtensionItemInfoImpl ext : extensionsData) {
            final Class<? extends FeatureInstaller> installer = ext.getInstalledBy();
            Preconditions.checkArgument(installerTypes.contains(installer), "Installer %s not registered",
                    installer.getSimpleName());
            if (!extensions.containsKey(installer)) {
                extensions.put(installer, Lists.<Class<?>>newArrayList());
            }
            extensions.get(installer).add(ext.getType());
        }
    }

    /**
     * @return registered extensions objects
     */
    public List<ExtensionItemInfoImpl> getExtensionsData() {
        return extensionsData;
    }

    /**
     * @return list of all registered installer instances
     */
    public List<FeatureInstaller> getInstallers() {
        return installers;
    }

    /**
     * @return list of all registered installer types
     */
    public List<Class<? extends FeatureInstaller>> getInstallerTypes() {
        return installerTypes;
    }

    /**
     * @param installer installer type
     * @return list of all found extensions for installer or null if nothing found.
     */
    public List<Class<?>> getExtensions(final Class<? extends FeatureInstaller> installer) {
        return extensions.get(installer);
    }

    /**
     * Order extension according to {@link ru.vyarus.dropwizard.guice.module.installer.order.Order} annotation.
     * Installer must implement {@link ru.vyarus.dropwizard.guice.module.installer.order.Ordered} otherwise
     * no order appear.
     */
    public void order() {
        final OrderComparator comparator = new OrderComparator();
        for (Class<? extends FeatureInstaller> installer : installerTypes) {
            if (Ordered.class.isAssignableFrom(installer)) {
                final List<Class<?>> extensions = this.extensions.get(installer);
                if (extensions == null || extensions.size() <= 1) {
                    continue;
                }
                extensions.sort(comparator);
            }
        }
    }

    /**
     * Extension item, detected with classpath scan.
     */
    public static class ScanItem {
        private final Class<?> type;
        private final FeatureInstaller installer;

        public ScanItem(final Class<?> type, final FeatureInstaller installer) {
            this.type = type;
            this.installer = installer;
        }

        public Class<?> getType() {
            return type;
        }

        public FeatureInstaller getInstaller() {
            return installer;
        }
    }
}
