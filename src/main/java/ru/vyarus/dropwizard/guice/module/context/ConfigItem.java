package ru.vyarus.dropwizard.guice.module.context;

import io.dropwizard.ConfiguredBundle;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.impl.*;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

/**
 * Guicey configurable item types.
 *
 * @author Vyacheslav Rusakov
 * @since 06.07.2016
 */
public enum ConfigItem {
    /**
     * Installer.
     */
    Installer(false),
    /**
     * Extension (everything that is installed by installers (like resource, health check etc).
     */
    Extension(false),
    /**
     * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle}.
     * Note that guicey bundle installs other items and all of them are tracked too.
     */
    Bundle(true),
    /**
     * {@link io.dropwizard.ConfiguredBundle}. Only bundles registered through guicey api are tracked.
     */
    DropwizardBundle(true),
    /**
     * Guice module.
     * Note that only direct modules are tracked (if module registered by other guice module in it's configure
     * method it would not be tracked - it's pure guice staff).
     */
    Module(true),
    /**
     * Dropwizard command. Commands could be resolved with classpath scan and installed (by default disabled).
     */
    Command(false);

    private boolean instanceConfig;

    ConfigItem(final boolean instanceConfig) {
        this.instanceConfig = instanceConfig;
    }

    /**
     * @return true if instances used for configuration, false when configured with class
     */
    public boolean isInstanceConfig() {
        return instanceConfig;
    }

    /**
     * Creates info container for configuration item.
     *
     * @param item item instance or item class (for class based configurations)
     * @param <T>  type of required info container
     * @return info container instance
     */
    @SuppressWarnings("unchecked")
    public <T extends ItemInfoImpl> T newContainer(final Object item) {
        final ItemInfo res;
        switch (this) {
            case Installer:
                res = new InstallerItemInfoImpl((Class) item);
                break;
            case Extension:
                res = new ExtensionItemInfoImpl((Class) item);
                break;
            case Bundle:
                res = item instanceof Class
                        ? new BundleItemInfoImpl((Class<GuiceyBundle>) item)
                        : new BundleItemInfoImpl((GuiceyBundle) item);
                break;
            case DropwizardBundle:
                res = item instanceof Class
                        ? new DropwizardBundleItemInfoImpl((Class<ConfiguredBundle>) item)
                        : new DropwizardBundleItemInfoImpl((ConfiguredBundle) item);
                break;
            case Command:
                res = new CommandItemInfoImpl((Class) item);
                break;
            case Module:
                res = item instanceof Class
                        ? new ModuleItemInfoImpl((Class<com.google.inject.Module>) item)
                        : new ModuleItemInfoImpl((com.google.inject.Module) item);
                break;
            default:
                res = new ItemInfoImpl(this, ItemId.from(item));
                break;
        }
        return (T) res;
    }
}
