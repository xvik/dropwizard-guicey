package ru.vyarus.dropwizard.guice.module.installer;

import com.google.inject.AbstractModule;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.impl.ExtensionItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsHolder;

/**
 * Module binds all discovered extensions to guice context: either perform default binding or
 * call installer if it supports custom bindings.
 * <p>
 * Feature installers can be disabled from bundle config.
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
    protected void configure() {
        // bound for internal usage
        bind(ExtensionsHolder.class).toInstance(context.getExtensionsHolder());

        for (ExtensionItemInfoImpl ext : context.getExtensionsHolder().getExtensionsData()) {
            bindExtension(ext, ext.getInstaller());
        }
    }

    /**
     * Bind extension to guice context.
     *
     * @param item      extension item descriptor
     * @param installer detected extension installer
     */
    private void bindExtension(final ExtensionItemInfo item, final FeatureInstaller installer) {
        final Class<?> type = item.getType();
        if (installer instanceof BindingInstaller) {
            ((BindingInstaller) installer).install(binder(), type, item.isLazy());
        } else if (!item.isLazy()) {
            // if installer isn't install binding manually, lazy simply disable registration
            binder().bind(type);
        }
    }
}
