package ru.vyarus.dropwizard.guice.debug;

import com.google.inject.Binding;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.BindingUtils;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.*;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.JerseyConfigurationEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.JerseyExtensionsInstalledEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Debug guicey lifecycle listener. Could be installed with bundle shortcut:
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#printLifecyclePhases()}.
 * <p>
 * Use system out instead of logger because logger in not initialized in time of first events and for
 * more clarity.
 * <p>
 * Split logs with current phase name and startup timer. This should clarify custom logic execution times.
 * <p>
 * If multiple listeners registered, only first registered will be actually used (allow safe multiple registrations).
 *
 * @author Vyacheslav Rusakov
 * @since 17.04.2018
 */
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class LifecycleDiagnostic extends GuiceyLifecycleAdapter {

    private static final String BUNDLES = "bundles";
    private static final String DISABLED = "disabled";
    private static final String NL = "\n";
    private static final String EXTENSIONS = "extensions";
    private static final String INSTALLED_EXTENSIONS = "installed extensions";

    private final boolean showDetails;

    // counting time from listener creation (~same as bundle registration and app initial configuration)
    private final StopWatch timer = StopWatch.createStarted();

    public LifecycleDiagnostic(final boolean showDetails) {
        this.showDetails = showDetails;
    }

    @Override
    protected void configurationHooksProcessed(final ConfigurationHooksProcessedEvent event) {
        log("%s hooks processed", event.getHooks().size());
        if (showDetails) {
            logDetails("hooks", event.getHooks());
        }
    }

    @Override
    protected void dropwizardBundlesInitialized(final DropwizardBundlesInitializedEvent event) {
        log("Initialized %s%s dropwizard bundles",
                event.getBundles().size(), fmtDisabled(event.getDisabled()));
        if (showDetails) {
            logDetails(BUNDLES, event.getBundles());
            logDetails(DISABLED, event.getDisabled());
        }
    }

    @Override
    protected void lookupBundlesResolved(final BundlesFromLookupResolvedEvent event) {
        log("%s lookup bundles recognized", event.getBundles().size());
        if (showDetails) {
            logDetails(BUNDLES, event.getBundles());
        }
    }

    @Override
    protected void bundlesInitialized(final BundlesInitializedEvent event) {
        log("Initialized %s%s GuiceyBundles",
                event.getBundles().size(), fmtDisabled(event.getDisabled()));
        if (showDetails) {
            logDetails(BUNDLES, event.getBundles());
            logDetails(DISABLED, event.getDisabled());
        }
    }

    @Override
    protected void commandsResolved(final CommandsResolvedEvent event) {
        if (!event.getCommands().isEmpty()) {
            log("%s commands installed", event.getCommands().size());
            if (showDetails) {
                logDetails("commands", event.getCommands());
            }
        }
    }

    @Override
    protected void installersResolved(final InstallersResolvedEvent event) {
        log("%s%s installers initialized", event.getInstallers().size(), fmtDisabled(event.getDisabled()));
        if (showDetails) {
            logDetails("installers", event.getInstallers());
            logDetails(DISABLED, event.getDisabled());
        }
    }

    @Override
    protected void manualExtensionsValidated(final ManualExtensionsValidatedEvent event) {
        log("%s manual extensions validated (of %s registered)",
                event.getValidated().size(), event.getExtensions().size());
        if (showDetails) {
            logDetails("validated", event.getValidated());
            final List<Class<?>> ignored = new ArrayList<>(event.getExtensions());
            ignored.removeAll(event.getValidated());
            logDetails("ignored", ignored);
        }
    }

    @Override
    protected void classpathExtensionsResolved(final ClasspathExtensionsResolvedEvent event) {
        log("%s classpath extensions detected", event.getExtensions().size());
        if (showDetails) {
            logDetails(EXTENSIONS, event.getExtensions());
        }
    }

    @Override
    protected void bundlesStarted(final BundlesStartedEvent event) {
        log("Started %s GuiceyBundles", event.getBundles().size());
        if (showDetails) {
            logDetails(BUNDLES, event.getBundles());
        }
    }

    @Override
    protected void modulesAnalyzed(ModulesAnalyzedEvent event) {
        log("%s binding extensions detected", event.getExtensions().size());
        if (showDetails) {
            logDetails(EXTENSIONS, event.getExtensions());
            logDetails("removed inner modules", event.getInnerModulesRemoved());

            if (!event.getBindingsRemoved().isEmpty()) {
                final List<String> bindings = new ArrayList<>();
                for (Binding binding : event.getBindingsRemoved()) {
                    final List<String> modules = BindingUtils.getModules(binding).stream()
                            .sorted(Collections.reverseOrder())
                            .map(it -> it.substring(it.lastIndexOf(".") + 1))
                            .collect(Collectors.toList());
                    bindings.add(String.join("/", modules) + " | " + RenderUtils
                            .renderClassLine(binding.getKey().getTypeLiteral().getRawType(), null));
                }
                logDetails("removed extension bindings", bindings);
            }
        }
    }

    @Override
    protected void extensionsResolved(final ExtensionsResolvedEvent event) {
        log("%s%s extensions found", event.getExtensions().size(), fmtDisabled(event.getDisabled()));
        if (showDetails) {
            logDetails(EXTENSIONS, event.getExtensions());
            logDetails(DISABLED, event.getDisabled());
        }
    }

    @Override
    protected void injectorCreation(final InjectorCreationEvent event) {
        log("Staring guice with %s/%s%s modules...",
                event.getModules().size(), event.getOverridingModules().size(), fmtDisabled(event.getDisabled()));
        if (showDetails) {
            logDetails("modules", event.getModules());
            logDetails("overriding", event.getOverridingModules());
            logDetails(DISABLED, event.getDisabled());
        }
    }

    @Override
    protected void extensionsInstalled(final ExtensionsInstalledEvent event) {
        log("%s extensions installed", event.getExtensions().size());
        if (showDetails) {
            logDetails(INSTALLED_EXTENSIONS, event.getExtensions());
        }
    }

    @Override
    protected void applicationRun(final ApplicationRunEvent event) {
        log("Guice started, app running...");
        event.registerJettyListener(new JettyLifecycleListener());
        event.registerJerseyListener(new JerseyEventListener());
    }

    @Override
    protected void jerseyConfiguration(final JerseyConfigurationEvent event) {
        log("Configuring Jersey...");
    }

    @Override
    protected void jerseyExtensionsInstalled(final JerseyExtensionsInstalledEvent event) {
        log("%s Jersey extensions installed", event.getExtensions().size());
        if (showDetails) {
            logDetails(INSTALLED_EXTENSIONS, event.getExtensions());
        }
    }


    @Override
    public boolean equals(final Object obj) {
        // allow only one listener instance
        return obj instanceof LifecycleDiagnostic;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private void log(final String message, final Object... args) {
        final int gap = 70;
        final String time = timer.toString();
        final String msg = String.format(message, args);
        final String topLine = String.format("%" + (gap + 3) + "s", "")
                + String.join("", Collections.nCopies(msg.length(), "\u2500"));
        final String prefix = "__[ " + time + " ]" + String.join("",
                Collections.nCopies((gap - 6) - time.length(), "_"));
        System.out.println("\n\n" + topLine + NL + prefix + "/  " + msg + "  \\____\n");
    }

    private String fmtDisabled(final List items) {
        return items.isEmpty() ? "" : " (-" + items.size() + ")";
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private void logDetails(final String message, final Collection<?> items) {
        if (items.isEmpty()) {
            return;
        }
        final StringBuilder builder = new StringBuilder()
                .append("\t").append(message).append(" = \n");
        for (Object item : items) {
            builder.append("\t\t").append(item instanceof String ? item
                    : RenderUtils.renderClassLine(item instanceof Class ? (Class) item : item.getClass(), null))
                    .append(NL);
        }
        System.out.println(builder.toString());
    }

    /**
     * Jetty listener.
     */
    private class JettyLifecycleListener extends AbstractLifeCycle.AbstractLifeCycleListener {
        @Override
        public void lifeCycleStarting(final LifeCycle event) {
            log("Jetty starting...");
        }

        @Override
        public void lifeCycleStarted(final LifeCycle event) {
            log("Jetty started");
        }

        @Override
        public void lifeCycleStopping(final LifeCycle event) {
            timer.reset();
            log("Stopping Jetty...");
        }

        @Override
        public void lifeCycleStopped(final LifeCycle event) {
            log("Jetty stopped");
        }
    }

    /**
     * Jersey listener.
     */
    private class JerseyEventListener implements ApplicationEventListener {
        @Override
        @SuppressWarnings({"checkstyle:MissingSwitchDefault", "PMD.SwitchStmtsShouldHaveDefault"})
        @SuppressFBWarnings("SF_SWITCH_NO_DEFAULT")
        public void onEvent(final ApplicationEvent event) {
            switch (event.getType()) {
                case INITIALIZATION_START:
                    log("Initializing jersey app...");
                    break;
                case INITIALIZATION_APP_FINISHED:
                    log("Jersey app initialized");
                    break;
                case INITIALIZATION_FINISHED:
                    log("Jersey initialized");
                    break;
                case DESTROY_FINISHED:
                    log("Jersey app destroyed");
                    break;
            }
        }

        @Override
        public RequestEventListener onRequest(final RequestEvent requestEvent) {
            return null;
        }
    }
}
