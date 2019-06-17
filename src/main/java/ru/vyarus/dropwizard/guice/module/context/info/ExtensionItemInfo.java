package ru.vyarus.dropwizard.guice.module.context.info;

import ru.vyarus.dropwizard.guice.module.context.info.sign.DisableSupport;
import ru.vyarus.dropwizard.guice.module.context.info.sign.ScanSupport;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;

/**
 * Extension configuration information.
 *
 * @author Vyacheslav Rusakov
 * @since 09.07.2016
 */
public interface ExtensionItemInfo extends ItemInfo, ScanSupport, DisableSupport {

    /**
     * Each extension is always registered by single installer. If extension is recognizable by multiple installers
     * then it will be installed by first matching installer.
     *
     * @return installer installed this extension
     */
    Class<? extends FeatureInstaller> getInstalledBy();

    /**
     * Lazy beans are not registered in guice by default. Some installers could support this flag in a special way.
     *
     * @return true if extension annotated with
     * {@link ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding}, false otherwise
     */
    boolean isLazy();

    /**
     * Indicates extension management by jersey instead of guice.
     *
     * @return true if extension annotated with
     * {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged}, false otherwise
     */
    boolean isJerseyManaged();
}
