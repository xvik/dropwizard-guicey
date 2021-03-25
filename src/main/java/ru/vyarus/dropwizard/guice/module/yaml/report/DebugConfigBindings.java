package ru.vyarus.dropwizard.guice.module.yaml.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.BeforeRunEvent;

/**
 * Configuration bindings debug listener. Must be registered with
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#listen(
 * ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener...)}.
 * Could be configured to filter out not required info.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#printConfigurationBindings()
 * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#printCustomConfigurationBindings()
 * @since 13.06.2018
 */
public class DebugConfigBindings extends GuiceyLifecycleAdapter {

    private final Logger logger = LoggerFactory.getLogger(DebugConfigBindings.class);

    private final BindingsConfig config;

    public DebugConfigBindings(final BindingsConfig config) {
        this.config = config;
    }

    @Override
    protected void beforeRun(final BeforeRunEvent event) {
        final boolean customOnly = config.isShowCustomConfigOnly();
        final String report;
        if (customOnly && event.getConfigurationTree().getRootTypes().size() == 1) {
            report = Reporter.NEWLINE + Reporter.NEWLINE + Reporter.TAB + "No custom bindings";
        } else {
            report = new ConfigBindingsRenderer(event.getConfigurationTree()).renderReport(config);
        }
        logger.info("Available {}configuration bindings = {}",
                customOnly ? "custom " : "", report);
    }
}
