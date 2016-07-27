package ru.vyarus.dropwizard.guice.module.context;

import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.impl.*;

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
    Installer,
    /**
     * Extension (everything that is installed by installers (like resource, health check etc).
     */
    Extension,
    /**
     * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle}.
     * Note that guicey bundle installs other items and all of the, are tracked too.
     */
    Bundle,
    /**
     * Guice module.
     * Note that only directly modules are tracked (if module register other guice module in it's configure
     * method it would not be tracked - it's pure guice staff).
     */
    Module,
    /**
     * Dropwizard command. Commands could be resolved with classpath scan and installed (by default disabled).
     */
    Command;

    /**
     * Creates info container for configuration item.
     *
     * @param type item class
     * @param <T>  type of required info container
     * @return info container instance
     */
    @SuppressWarnings("unchecked")
    public <T extends ItemInfoImpl> T newContainer(final Class<?> type) {
        final ItemInfo res;
        switch (this) {
            case Installer:
                res = new InstallerItemInfoImpl(type);
                break;
            case Extension:
                res = new ExtensionItemInfoImpl(type);
                break;
            case Bundle:
                res = new BundleItemInfoImpl(type);
                break;
            case Command:
                res = new CommandItemInfoImpl(type);
                break;
            default:
                res = new ItemInfoImpl(this, type);
                break;
        }
        return (T) res;
    }
}
