package ru.vyarus.dropwizard.guice.module.installer.bundle;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import ru.vyarus.dropwizard.guice.module.installer.internal.InstallerConfig;

import java.util.List;

/**
 * Bundle context contains data configured by bundles and used during bundles resolution.
 *
 * @author Vyacheslav Rusakov
 * @since 20.06.2016
 */
@SuppressWarnings("checkstyle:VisibilityModifier")
public final class BundleContext {
    /**
     * Registered guice modules.
     */
    public List<Module> modules = Lists.newArrayList();
    /**
     * Installer and manual extensions configuration.
     */
    public InstallerConfig installerConfig = new InstallerConfig();
    /**
     * Bundles resolved during bundle lookup.
     */
    public List<GuiceyBundle> lookupBundles = Lists.newArrayList();
    /**
     * Initially contains directly registered bundles by {@link ru.vyarus.dropwizard.guice.GuiceBundle}.
     * Then it will contain bundles registered by processed bundles (iterative).
     */
    public List<GuiceyBundle> bundles = Lists.newArrayList();
    /**
     * Types of all installed guicey bundles.
     */
    public List<Class<? extends GuiceyBundle>> installedBundles = Lists.newArrayList();
}
