package ru.vyarus.dropwizard.guice.module.autoconfig.feature;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.autoconfig.util.FeatureUtils;

import java.util.List;

/**
 * Installs all extensions found during classpath scanning.
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
        for (FeatureInstaller installer : holder.getInstallers()) {
            final List<Class> res = holder.getFeatures(installer);
            if (res != null) {
                for (Class inst : res) {
                    installer.install(environment, injector.getInstance(inst));
                    logger.debug("{} extension installed: {}",
                            FeatureUtils.getInstallerExtName(installer.getClass()), inst.getName());
                }
            }
        }
    }
}
