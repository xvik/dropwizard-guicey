package ru.vyarus.dropwizard.guice.module;

import com.google.common.base.Stopwatch;
import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.injector.InjectorFactory;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.installer.internal.CommandSupport;
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsSupport;
import ru.vyarus.dropwizard.guice.module.installer.internal.ModulesSupport;
import ru.vyarus.dropwizard.guice.module.installer.util.BundleSupport;

import static ru.vyarus.dropwizard.guice.GuiceyOptions.InjectorStage;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.*;

/**
 * Guicey run logic performed under dropwizard run phase.
 *
 * @author Vyacheslav Rusakov
 * @since 05.08.2019
 */
public class GuiceyRunner {

    private final Stopwatch guiceyTime;
    private final Stopwatch runTime;

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
     */
    public void runBundles() {
        final Stopwatch timer = context.stat().timer(BundleTime);
        BundleSupport.runBundles(context);
        timer.stop();
    }

    /**
     * Prepare guice modules for injector creation.
     */
    public void prepareModules() {
        // dropwizard specific bindings and jersey integration
        context.registerModules(new GuiceBootstrapModule(context));
        context.finalizeConfiguration();
        ModulesSupport.configureModules(context);
    }


    /**
     * @param injectorFactory configured injector factory
     * @return created injector
     */
    public Injector createInjector(final InjectorFactory injectorFactory) {
        final Stopwatch timer = context.stat().timer(InjectorCreationTime);
        injector = injectorFactory.createInjector(
                context.option(InjectorStage), ModulesSupport.prepareModules(context));
        // registering as managed to cleanup injector on application stop
        context.getEnvironment().lifecycle().manage(
                InjectorLookup.registerInjector(context.getBootstrap().getApplication(), injector));
        timer.stop();
        return injector;
    }

    /**
     * Execute extensions installation (by type and instance).
     */
    public void installExtensions() {
        final Stopwatch timer = context.stat().timer(ExtensionsInstallationTime);
        ExtensionsSupport.installExtensions(context, injector);
        timer.stop();
    }

    /**
     * Inject fields in registered commands. This step is actually required only if currently executed dropwizard
     * command require such injections.
     */
    @SuppressWarnings("unchecked")
    public void injectCommands() {
        final Stopwatch timer = context.stat().timer(CommandTime);
        CommandSupport.initCommands(context.getBootstrap().getCommands(), injector);
        timer.stop();
    }

    /**
     * Run lifecycle end.
     */
    public void runFinished() {
        context.lifecycle().applicationRun();

        runTime.stop();
        guiceyTime.stop();
    }
}
