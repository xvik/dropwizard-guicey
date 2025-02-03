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
public interface ExtensionItemInfo extends ClassItemInfo, ScanSupport, DisableSupport {

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
     * <p>
     * Soft deprecation: try to avoid hk2 direct usage if possible, someday HK2 support will be removed
     *
     * @return true if extension annotated with
     * {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged}, false otherwise
     */
    boolean isJerseyManaged();

    /**
     * Indicates extensions, recognized from guice modules. Extension might be found both by classpath scan
     * (or registered manually) and be detected as binding declaration and in this case no automatic guice
     * binding would be performed.
     *
     * @return true if extension detected in guice bindings, false otherwise (for direct-only extension)
     */
    boolean isGuiceBinding();

    /**
     * Optional extensions are registered directly in guicey bundles. These extensions automatically become
     * disabled if no installer recognize it (instead of throwing exception).
     *
     * @return true if extension is optional
     */
    boolean isOptional();
}
