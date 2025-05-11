package ru.vyarus.dropwizard.guice.debug.report.start;

import com.google.common.base.MoreObjects;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.context.stat.DetailStat;
import ru.vyarus.dropwizard.guice.module.context.stat.Stat;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;

import java.time.Duration;
import java.util.Map;

/**
 * Render startup times.
 *
 * @author Vyacheslav Rusakov
 * @since 07.03.2025
 */
@SuppressWarnings("MultipleStringLiterals")
public class StartupTimeRenderer {

    /**
     * Render startup time report.
     *
     * @param info startup info
     * @return rendered report
     */
    public String render(final StartupTimeInfo info) {
        final StringBuilder res = new StringBuilder(200);
        res.append("\n\n").append(line(1, "JVM time before", Duration.ofMillis(info.getJvmStart())))

                .append('\n')
                .append(line(1, "Application startup", info.getStats().duration(Stat.OverallTime)))

                .append(line(2, "Dropwizard initialization", info.getInitTime()));
        info.getBundlesInitPoints().forEach((s, point) -> {
            if (GuiceBundle.class.equals(s)) {
                printGuiceyInit(3, point, info, res);
            } else {
                res.append(line(3, RenderUtils.getClassName(s), "finished since start at ", point, null));
            }
        });

        res.append('\n').append(line(2, "Dropwizard run", info.getRunPoint().minus(info.getInitTime())))
                .append(line(3, "Configuration and Environment", info.getDwPreRunTime()));
        info.getBundlesRunTimes().forEach((s, duration) -> {
            if (GuiceBundle.class.equals(s)) {
                printGuiceyRun(3, duration, info, res);
            } else {
                res.append(line(3, RenderUtils.getClassName(s), duration));
            }
        });

        res.append('\n').append(line(2, "Web server startup", info.getWebTime()));
        printGuiceyWeb(3, info, res);

        return res.toString();
    }

    private void printGuiceyInit(final int shift,
                                 final Duration point,
                                 final StartupTimeInfo info,
                                 final StringBuilder res) {
        res.append(line(shift, GuiceBundle.class.getSimpleName(), null,
                        // exclude dropwizard bundles time (registered through guicey) - time tracked separately
                        info.getStats().duration(Stat.ConfigurationTime)
                                .minus(info.getStats().duration(Stat.DropwizardBundleInitTime)),
                        " (finished since start at " + PrintUtils.ms(point) + ")"))

                .append(line(shift + 1, "Bundle builder time",
                        info.getStats().duration(Stat.BundleBuilderTime)))

                .append(line(shift + 1, "Hooks processing",
                        info.getStats().duration(Stat.HooksTime)));

        info.getStats().getDetailedStats(DetailStat.Hook).forEach((type, duration) -> {
            final String className = RenderUtils.getClassName(type);
            res.append(line(shift + 2, className, duration));
        });

        res.append(line(shift + 1, "Classpath scan", info.getStats().duration(Stat.ScanTime)))

                .append(line(shift + 1, "Commands processing", info.getStats().duration(Stat.CommandTime)));
        info.getStats().getDetailedStats(DetailStat.Command).forEach((type, duration) ->
                res.append(line(shift + 2, RenderUtils.getClassName(type), duration)));

        res.append(line(shift + 1, "Bundles lookup", info.getStats().duration(Stat.BundleResolutionTime)))

                .append(line(shift + 1, "Guicey bundles init", info.getStats().duration(Stat.GuiceyBundleInitTime)));
        final Map<Class<?>, Duration> detailedStats = info.getStats().getDetailedStats(DetailStat.BundleInit);
        // bundle stats would contain incorrect init order
        info.getGuiceyBundlesInitOrder().forEach(type -> {
            Duration actual = detailedStats.get(type);
            // exclude transitive bundles time
            for (Class<?> transitive : info.getGuiceyBundleTransitives().get(type)) {
                actual = actual.minus(detailedStats.get(transitive));
            }
            res.append(line(shift + 2, RenderUtils.getClassName(type), actual));
        });

        res.append(line(shift + 1, "Installers time", info.getInitInstallersTime()))
                .append(line(shift + 2, "Installers resolution",
                        info.getStats().duration(Stat.InstallersResolutionTime)))
                .append(line(shift + 2, "Scanned extensions recognition", info.getInitExtensionsTime()))

                .append(line(shift + 1, "Listeners time", info.getInitListenersTime()));
        info.getStats().getDetailedStats(DetailStat.Listener).forEach((type, time) -> {
            if (info.getInitEvents().contains(type)) {
                res.append(line(shift + 2, type.getSimpleName(), time));
            }
        });
    }

    private void printGuiceyRun(final int shift,
                                final Duration duration,
                                final StartupTimeInfo info,
                                final StringBuilder res) {
        // same as info.getStats().duration(Stat.RunTime) but slightly more accurate
        res.append(line(shift, GuiceBundle.class.getSimpleName(), duration))
                .append(line(shift + 1, "Configuration analysis", info.getStats().duration(Stat.ConfigurationAnalysis)))
                .append(line(shift + 1, "Guicey bundles run", info.getStats().duration(Stat.GuiceyBundleRunTime)));

        // here order would be correct because there is no transitive bundles installation
        info.getStats().getDetailedStats(DetailStat.BundleRun).forEach((type, time) ->
                res.append(line(shift + 2, RenderUtils.getClassName(type), time)));

        final Duration bindingsAnalysisTime = info.getStats().duration(Stat.BindingsAnalysisTime);
        res.append(line(shift + 1, "Guice modules processing", info.getStats().duration(Stat.ModulesProcessingTime)))
                .append(line(shift + 2, "Bindings resolution", info.getStats().duration(Stat.BindingsResolutionTime)))

                .append(line(shift + 1, "Installers time", info.getStats().duration(Stat.InstallersTime)
                        .minus(info.getInitInstallersTime())
                        .plus(info.getStats().duration(Stat.ExtensionsInstallationTime))))
                .append(line(shift + 2, "Extensions registration", info.getStats()
                        .duration(Stat.ExtensionsRecognitionTime)
                        .minus(info.getInitExtensionsTime())
                        .minus(bindingsAnalysisTime)))
                .append(line(shift + 2, "Guice bindings analysis", bindingsAnalysisTime))
                .append(line(shift + 2, "Extensions installation", info.getStats()
                        .duration(Stat.ExtensionsInstallationTime)))

                .append(line(shift + 1, "Injector creation", info.getStats().duration(Stat.InjectorCreationTime)));
        info.getStats().getGuiceStats().forEach(s -> {
            if (!s.endsWith(" 0 ms")) {
                final String[] val = s.split(": ");
                res.append(tab(shift + 2)).append(String.format("%-35s: %s%n", val[0], val[1]));
            }
        });

        res.append(line(shift + 1, "Listeners time", info.getRunListenersTime()));
        info.getStats().getDetailedStats(DetailStat.Listener).forEach((type, time) -> {
            if (info.getRunEvents().contains(type)) {
                res.append(line(shift + 2, type.getSimpleName(), time));
            }
        });
    }

    private void printGuiceyWeb(final int shift,
                                final StartupTimeInfo info,
                                final StringBuilder res) {

        final boolean lifecycleSimulation = info.getJerseyTime() == null;
        res.append(line(shift, lifecycleSimulation ? "Lifecycle simulation time"
                : "Jetty lifecycle time", info.getLifecycleTime()));

        info.getManagedTimes().forEach((type, duration) ->
                res.append(tab(shift + 1)).append(String.format("%-10s", info.getManagedTypes().get(type)))
                        .append(line(0, RenderUtils.getClassName(type), duration)));

        final int prefix = lifecycleSimulation ? shift : shift + 1;
        if (!lifecycleSimulation) {
            res.append(line(prefix, "Jersey time", info.getJerseyTime()));
        }

        final Duration listenersTime = info.getStats().duration(Stat.ListenersTime)
                .minus(info.getRunListenersTime()).minus(info.getInitListenersTime());
        res.append(line(prefix + 1, "Guicey time", info.getStats().duration(Stat.JerseyTime).plus(listenersTime)))
                .append(line(prefix + 2, "Installers time", info.getStats().duration(Stat.JerseyInstallerTime)))

                .append(line(prefix + 2, "Listeners time", listenersTime));
        info.getStats().getDetailedStats(DetailStat.Listener).forEach((type, time) -> {
            if (info.getWebEvents().contains(type)) {
                res.append(line(prefix + 3, type.getSimpleName(), time));
            }
        });
    }

    private String line(final int shift,
                        final String name,
                        final Duration duration) {
        return line(shift, name, null, duration, null);
    }

    private String line(final int shift,
                        final String name,
                        final String prefix,
                        final Duration duration,
                        final String postfix) {
        return tab(shift) + format(name, prefix, duration, postfix);
    }

    private String format(final String name, final String prefix, final Duration duration, final String postfix) {
        return String.format("%-35s: %s%s%s%n", name, MoreObjects.firstNonNull(prefix, ""),
                PrintUtils.ms(duration), MoreObjects.firstNonNull(postfix, ""));
    }

    private String tab(final int shift) {
        final StringBuilder res = new StringBuilder();
        for (int i = 0; i < shift; i++) {
            res.append('\t');
        }
        return res.toString();
    }
}
