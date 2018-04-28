package ru.vyarus.dropwizard.guice.module.installer.feature.jersey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller;
import ru.vyarus.dropwizard.guice.module.installer.option.InstallerOptionsSupport;

import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.HkExtensionsManagedByGuice;
import static ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.isHK2Managed;

/**
 * Base class for jersey installers ({@link JerseyInstaller}). Provides common utilities.
 *
 * @param <T> extensions type
 * @author Vyacheslav Rusakov
 * @since 28.04.2018
 */
public abstract class AbstractJerseyInstaller<T> extends InstallerOptionsSupport implements
        FeatureInstaller<T>,
        JerseyInstaller<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Checks if lazy flag could be counted (only when extension is managed by guice). Prints warning in case
     * of incorrect lazy marker usage.
     *
     * @param type extension type
     * @param lazy lazy marker (annotation presence)
     * @return lazy marker if guice managed type and false when hk managed.
     */
    protected boolean isLazy(final Class<?> type, final boolean lazy) {
        if (isHkExtension(type) && lazy) {
            logger.warn("@LazyBinding is ignored, because @HK2Managed set: {}", type.getName());
            return false;
        }
        return lazy;
    }

    /**
     * @param type extension type
     * @return true if extension should be managed by hk, false to manage by guice
     */
    protected boolean isHkExtension(final Class<?> type) {
        return isHK2Managed(type, option(HkExtensionsManagedByGuice));
    }
}
