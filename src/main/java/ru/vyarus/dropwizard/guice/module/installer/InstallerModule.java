package ru.vyarus.dropwizard.guice.module.installer;

import com.google.inject.AbstractModule;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.impl.ExtensionItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsHolder;
import ru.vyarus.dropwizard.guice.module.installer.internal.FeatureInstallerExecutor;

/**
 * Module performs auto configuration using classpath scanning or manually predefined installers and beans.
 * First search provided packages for registered installers
 * {@link FeatureInstaller}. Then scan classpath one more time with installers to apply extensions.
 * <p>
 * Feature installers can be disabled from bundle config.
 * <p>
 * NOTE: Classpath scan will load all found classes in configured packages. Try to reduce scan scope
 * as much as possible.
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
public class InstallerModule extends AbstractModule {
    private final ConfigurationContext context;

    public InstallerModule(final ConfigurationContext context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configure() {
        // called just after injector creation to process instance installers
        bind(FeatureInstallerExecutor.class).asEagerSingleton();

        bind(ExtensionsHolder.class).toInstance(context.getExtensionsHolder());

        for (Class<?> ext : context.getEnabledExtensions()) {
            final ExtensionItemInfoImpl info = context.getInfo(ext);
            bindExtension(info, info.getInstaller(), context.getExtensionsHolder());
        }
    }

    /**
     * Bind extension to guice context.
     *
     * @param item      extension item descriptor
     * @param installer detected extension installer
     * @param holder    extensions holder bean
     */
    @SuppressWarnings("unchecked")
    private void bindExtension(final ExtensionItemInfo item, final FeatureInstaller installer,
                               final ExtensionsHolder holder) {
        final Class<? extends FeatureInstaller> installerClass = installer.getClass();
        final Class<?> type = item.getType();
        holder.register(installerClass, type);
        if (installer instanceof BindingInstaller) {
            ((BindingInstaller) installer).install(binder(), type, item.isLazy());
        } else if (!item.isLazy()) {
            // if installer isn't install binding manually, lazy simply disable registration
            binder().bind(type);
        }
    }
}
