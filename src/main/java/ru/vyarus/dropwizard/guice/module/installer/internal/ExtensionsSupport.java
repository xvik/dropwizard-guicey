package ru.vyarus.dropwizard.guice.module.installer.internal;

import com.google.common.base.Preconditions;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.info.impl.ExtensionItemInfoImpl;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.TypeInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding;

import java.util.ArrayList;
import java.util.List;

import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.JerseyExtensionsManagedByGuice;

/**
 * Extensions installation utility.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
public final class ExtensionsSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionsSupport.class);

    private ExtensionsSupport() {
    }

    /**
     * Register extension (manual or from classpath scan).
     *
     * @param context  configuration context
     * @param type     extension class
     * @param fromScan true for classpath scan
     * @return true if extension recognized by installers, false otherwise
     */
    public static boolean registerExtension(final ConfigurationContext context,
                                            final Class<?> type,
                                            final boolean fromScan) {
        final FeatureInstaller installer = findInstaller(type, context.getExtensionsHolder());
        final boolean recognized = installer != null;
        if (recognized) {
            // important to force config creation for extension from scan to allow disabling by matcher
            final ExtensionItemInfoImpl info = context.getOrRegisterExtension(type, fromScan);
            info.setLazy(type.isAnnotationPresent(LazyBinding.class));
            info.setJerseyManaged(JerseyBinding.isJerseyManaged(type, context.option(JerseyExtensionsManagedByGuice)));
            info.setInstaller(installer);
        }
        return recognized;
    }

    /**
     * Register extension from guice binding.
     *
     * @param context              configuration context
     * @param type                 extension type
     * @param manualBinding        guice binding from module
     * @param topDeclarationModule top declaration module (which was manually added by user)
     * @return true if extension recognized by installers, false otherwise
     */
    public static boolean registerExtensionBinding(final ConfigurationContext context,
                                                   final Class<?> type,
                                                   final Binding<?> manualBinding,
                                                   final Class<? extends Module> topDeclarationModule) {
        final FeatureInstaller installer = findInstaller(type, context.getExtensionsHolder());
        final boolean recognized = installer != null;
        if (recognized) {
            // important to force config creation for extension from scan to allow disabling by matcher
            final ExtensionItemInfoImpl info = context.getOrRegisterBindingExtension(type, topDeclarationModule);

            // set values to unify cases of binding only extension and already known extensions (manually registered)
            info.setLazy(type.isAnnotationPresent(LazyBinding.class));
            info.setJerseyManaged(JerseyBinding.isJerseyManaged(type, context.option(JerseyExtensionsManagedByGuice)));

            Preconditions.checkState(!info.isLazy(),
                    "@%s annotation must not be used on manually bound extension: %s",
                    LazyBinding.class.getSimpleName(), type.getName());
            Preconditions.checkState(!info.isJerseyManaged(),
                    "Extension manually bound in guice module can't be marked as jersey managed (@%s): %s",
                    JerseyManaged.class.getSimpleName(), type.getName());

            info.setManualBinding(manualBinding);
            info.setInstaller(installer);
        }
        return recognized;
    }


    /**
     * Installs extensions by instance and type. Note that jersey extensions will be processed later after jersey
     * startup.
     *
     * @param context  configuration context
     * @param injector guice injector
     */
    @SuppressWarnings("unchecked")
    public static void installExtensions(final ConfigurationContext context, final Injector injector) {
        final ExtensionsHolder holder = context.getExtensionsHolder();
        holder.order();
        final List<Class<?>> allInstalled = new ArrayList<>();
        context.lifecycle().injectorPhase(injector);
        for (FeatureInstaller installer : holder.getInstallers()) {
            final List<Class<?>> res = holder.getExtensions(installer.getClass());
            if (res != null) {
                for (Class inst : res) {
                    if (installer instanceof TypeInstaller) {
                        ((TypeInstaller) installer).install(context.getEnvironment(), inst);
                    }
                    if (installer instanceof InstanceInstaller) {
                        ((InstanceInstaller) installer).install(context.getEnvironment(), injector.getInstance(inst));
                    }
                    LOGGER.trace("{} extension installed: {}",
                            FeatureUtils.getInstallerExtName(installer.getClass()), inst.getName());
                }
            }
            if (!(installer instanceof JerseyInstaller)) {
                // jersey installers reporting occurs after jersey context start
                installer.report();
                // extensions for jersey installers will be notified after HK2 context startup
                context.lifecycle().extensionsInstalled(installer.getClass(), res);
                if (res != null) {
                    allInstalled.addAll(res);
                }
            }
        }
        context.lifecycle().extensionsInstalled(allInstalled);
    }


    /**
     * Search for matching installer. Extension may match multiple installer, but only one will be actually
     * used (note that installers are ordered).
     *
     * @param type   extension type
     * @param holder extensions holder bean
     * @return matching installer or null if no matching installer found
     */
    @SuppressWarnings("unchecked")
    private static FeatureInstaller findInstaller(final Class<?> type, final ExtensionsHolder holder) {
        for (FeatureInstaller installer : holder.getInstallers()) {
            if (installer.matches(type)) {
                return installer;
            }
        }
        return null;
    }
}
