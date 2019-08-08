package ru.vyarus.dropwizard.guice.module.installer.internal;

import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.TypeInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

import java.util.ArrayList;
import java.util.List;

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
}
