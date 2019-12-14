package ru.vyarus.dropwizard.guice.module.installer;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
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
            bindExtension(ext, ext.getInstaller(), ext.getManualBinding());
        }
    }

    /**
     * Bind extension to guice context. If extension already resolved from guice binding then default binding
     * is not performed, but still {@link BindingInstaller} may require to perform additional operations even
     * with existing binding.
     *
     * @param item          extension item descriptor
     * @param installer     detected extension installer
     * @param manualBinding manual binding from guice module
     */
    @SuppressWarnings("unchecked")
    private void bindExtension(final ExtensionItemInfo item,
                               final FeatureInstaller installer,
                               final Binding manualBinding) {
        final Class<?> type = item.getType();
        if (installer instanceof BindingInstaller) {
            final BindingInstaller bindingInstaller = (BindingInstaller) installer;
            if (manualBinding != null) {
                bindingInstaller.manualBinding(binder(), type, manualBinding);
            } else {
                // perform default binding only if not already bound manually by user
                bindingInstaller.bindExtension(binder(), type, item.isLazy());
            }
            // reporting call common for both branches
            bindingInstaller.extensionBound(binder().currentStage(), type);

        } else if (manualBinding == null && !item.isLazy()) {
            // if installer isn't install binding manually, lazy simply disable registration
            binder().bind(type);
        }
    }
}
