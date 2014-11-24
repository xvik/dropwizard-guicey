package ru.vyarus.dropwizard.guice.module.installer.internal;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.TypeInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

import java.util.List;

/**
 * Installs all extensions found during classpath scanning.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
public class FeatureInstallerExecutor {
    private final Logger logger = LoggerFactory.getLogger(FeatureInstallerExecutor.class);

    private final FeaturesHolder holder;
    private final Environment environment;
    private final Injector injector;

    @Inject
    public FeatureInstallerExecutor(
            final FeaturesHolder holder,
            final Environment environment,
            final Injector injector) {

        this.holder = holder;
        this.environment = environment;
        this.injector = injector;

        installFeatures();
    }

    @SuppressWarnings("unchecked")
    private void installFeatures() {
        holder.order();
        for (FeatureInstaller installer : holder.getInstallers()) {
            final List<Class<?>> res = holder.getFeatures(installer.getClass());
            if (res != null) {
                for (Class inst : res) {
                    if (installer instanceof TypeInstaller) {
                        ((TypeInstaller) installer).install(environment, inst);
                    }
                    if (installer instanceof InstanceInstaller) {
                        ((InstanceInstaller) installer).install(environment, injector.getInstance(inst));
                    }
                    logger.trace("{} extension installed: {}",
                            FeatureUtils.getInstallerExtName(installer.getClass()), inst.getName());
                }
            }
            if (!(installer instanceof JerseyInstaller)) {
                // jersey installers reporting occurs after jersey context start
                installer.report();
            }
        }
    }
}
