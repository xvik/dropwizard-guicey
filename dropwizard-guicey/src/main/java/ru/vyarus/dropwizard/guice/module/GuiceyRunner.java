package ru.vyarus.dropwizard.guice.module;

import com.google.inject.Injector;
import com.google.inject.Module;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.injector.InjectorFactory;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.stat.StatTimer;
import ru.vyarus.dropwizard.guice.module.installer.internal.CommandSupport;
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsSupport;
import ru.vyarus.dropwizard.guice.module.installer.internal.ModulesSupport;
import ru.vyarus.dropwizard.guice.module.installer.util.BundleSupport;

import java.util.ArrayList;

import static ru.vyarus.dropwizard.guice.GuiceyOptions.InjectorStage;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.BundleTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.CommandTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.ExtensionsInstallationTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.GuiceyTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.InjectorCreationTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.ModulesProcessingTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.RunTime;

/**
 * Guicey run logic performed under dropwizard run phase.
 *
 * @author Vyacheslav Rusakov
 * @since 05.08.2019
 */
public class GuiceyRunner {

    private final StatTimer guiceyTime;
    private final StatTimer runTime;

    private final ConfigurationContext context;

    private Injector injector;

    public GuiceyRunner(final ConfigurationContext context,
                        final Configuration configuration,
                        final Environment environment) {
        guiceyTime = context.stat().timer(GuiceyTime);
        runTime = context.stat().timer(RunTime);

        context.runPhaseStarted(configuration, environment);
        this.context = context;
    }

    /**
     * Process run phase for guicey bundles.
     * Note: dropwizard bundles registered after {@link ru.vyarus.dropwizard.guice.GuiceBundle} (or within it) will
     * be processed after that point (because they will be registered after guice bundle and so dropwizard will call
     * their run methods after guice bundle's run).
     *
     * @throws Exception if something goes wrong
     */
    public void runBundles() throws Exception {
        final StatTimer timer = context.stat().timer(BundleTime);
        BundleSupport.runBundles(context);
        timer.stop();
    }

    /**
     * Prepare guice modules for injector creation.
     */
    public void prepareModules() {
        final StatTimer timer = context.stat().timer(ModulesProcessingTime);
        // dropwizard specific bindings and jersey integration
        context.registerModules(new GuiceBootstrapModule(context));
        ModulesSupport.configureModules(context);
        timer.stop();
    }

    /**
     * If configuration from guice bindings is enabled, configured guice modules will be repackaged in order to
     * resolve all configured bindings (and filter disabled bindings to simulate common extensions disable behaviour).
     * <p>
     * Note that analysis step use guice elements SPI, which guice will use in any case. And to avoid duplicate work
     * on injector creation, analyzed elements are packaged into synthetic guice module and passed to injector
     * instead of original modules.
     * <p>
     * After bindings analysis all extensions are finally registered and entire configuration info is finalized.
     * <p>
     * When bindings configuration is disabled (with
     * {@link ru.vyarus.dropwizard.guice.GuiceyOptions#AnalyzeGuiceModules}), no modules repackaging is applied
     * (exact legacy guicey behavior). It may also be useful to disable feature to check for side effects.
     *
     * @return modules to use
     */
    public Iterable<Module> analyzeAndRepackageBindings() {
        final Iterable<Module> res = ModulesSupport.prepareModules(context);
        context.finalizeConfiguration();
        context.lifecycle().extensionsResolved(context.getEnabledExtensions(), context.getDisabledExtensions());
        return res;
    }


    /**
     * @param injectorFactory configured injector factory
     * @param modules         guice modules to use for injector
     */
    public void createInjector(final InjectorFactory injectorFactory, final Iterable<Module> modules) {
        final StatTimer timer = context.stat().timer(InjectorCreationTime);
        context.lifecycle().injectorCreation(
                new ArrayList<>(context.getNormalModules()),
                new ArrayList<>(context.getOverridingModules()),
                context.getDisabledModules(),
                context.getIgnoredItems(ConfigItem.Module));
        // intercept detailed guice initialization stats from guice logs
        context.stat().getGuiceStats().injectLogsInterceptor();
        injector = injectorFactory.createInjector(
                context.option(InjectorStage), modules);
        context.stat().getGuiceStats().resetStatsLogger();
        InjectorLookup.registerInjector(context.getBootstrap().getApplication(), injector);
        timer.stop();
    }

    /**
     * Execute extensions installation (by type and instance).
     */
    public void installExtensions() {
        final StatTimer timer = context.stat().timer(ExtensionsInstallationTime);
        ExtensionsSupport.installExtensions(context, injector);
        timer.stop();
    }

    /**
     * Inject fields in registered commands. This step is actually required only if currently executed dropwizard
     * command require such injections.
     */
    @SuppressWarnings("unchecked")
    public void injectCommands() {
        final StatTimer timer = context.stat().timer(CommandTime);
        CommandSupport.initCommands(context.getBootstrap().getCommands(), injector);
        timer.stop();
    }

    /**
     * Run lifecycle end.
     */
    public void runFinished() {
        context.bundleStarted();

        runTime.stop();
        guiceyTime.stop();
    }
}
