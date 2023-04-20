package ru.vyarus.dropwizard.guice.debug.report.stat;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ru.vyarus.dropwizard.guice.debug.report.ReportRenderer;
import ru.vyarus.dropwizard.guice.debug.util.TreeNode;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.Filters;
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo;
import ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller;

import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.*;
import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.NEWLINE;

/**
 * Renders startup statistics. Overall guicey time is composed from bundle time and hk time and so
 * Hk execution time is shown also below guicey.
 *
 * @author Vyacheslav Rusakov
 * @since 28.07.2016
 */
@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_INFERRED")
public class StatsRenderer implements ReportRenderer<Boolean> {

    private final GuiceyConfigurationInfo info;

    public StatsRenderer(final GuiceyConfigurationInfo info) {
        this.info = info;
    }

    /**
     * @param hideTiny hide sections with time less then 1ms
     * @return rendered statistics report
     */
    @Override
    public String renderReport(final Boolean hideTiny) {
        final TreeNode root = new TreeNode("GUICEY started in %s (%s config / %s run / %s jersey)",
                info.getStats().humanTime(GuiceyTime),
                info.getStats().humanTime(ConfigurationTime),
                info.getStats().humanTime(RunTime),
                info.getStats().humanTime(JerseyTime));
        renderTimes(root, hideTiny);

        final StringBuilder res = new StringBuilder().append(NEWLINE).append(NEWLINE);
        root.render(res);
        return res.toString();
    }

    private void renderTimes(final TreeNode root, final boolean hideTiny) {
        long remaining = info.getStats().time(GuiceyTime);
        final double percent = remaining / 100d;
        remaining -= renderClasspathScanInfo(root, hideTiny, percent);
        remaining -= renderBundlesProcessing(root, hideTiny, percent);
        remaining -= renderCommandsRegistration(root, hideTiny, percent);
        remaining -= renderModulesProcessing(root, percent);
        remaining -= renderInstallersProcessing(root, hideTiny, percent);
        remaining -= renderInjectorCreation(root, hideTiny, percent);
        remaining -= renderExtensionsInstallation(root, hideTiny, percent);
        remaining -= renderJerseyPart(root, hideTiny, percent);
        if (show(hideTiny, remaining)) {
            root.child("[%.2g%%] remaining %s ms", remaining / percent, remaining);
        }
    }

    private long renderClasspathScanInfo(final TreeNode root, final boolean hideTiny, final double percent) {
        final long scan = info.getStats().time(ScanTime);
        if (show(hideTiny, scan)) {
            final TreeNode node = root.child("[%.2g%%] CLASSPATH scanned in %s",
                    scan / percent, info.getStats().humanTime(ScanTime));
            final int classes = info.getStats().count(ScanClassesCount);
            node.child("scanned %s classes", classes);
            final int recognized = info.getData().getItems(Filters.fromScan()).size();
            node.child("recognized %s classes (%.2g%% of scanned)",
                    recognized, recognized / (classes / 100f));
        }
        return scan;
    }

    private long renderBundlesProcessing(final TreeNode root, final boolean hideTiny, final double percent) {
        final long bundle = info.getStats().time(BundleTime);
        if (show(hideTiny, bundle)) {
            final TreeNode node = root.child("[%.2g%%] BUNDLES processed in %s",
                    bundle / percent, info.getStats().humanTime(BundleTime));
            // if no bundles were actually resolved, resolution time would be tiny
            final long resolved = info.getStats().time(BundleResolutionTime);
            if (show(hideTiny, resolved)) {
                node.child("%s resolved in %s",
                        info.getBundlesFromLookup().size(),
                        info.getStats().humanTime(BundleResolutionTime));
            }
            final int guiceyBundles = info.getGuiceyBundles().size();
            if (guiceyBundles > 0) {
                node.child("%s initialized in %s", guiceyBundles,
                        info.getStats().humanTime(GuiceyBundleInitTime));
            }
            final int dwBundles = info.getDropwizardBundles().size();
            if (dwBundles > 0) {
                node.child("%s dropwizard bundles initialized in %s", dwBundles,
                        info.getStats().humanTime(DropwizardBundleInitTime));
            }
        }
        return bundle;
    }

    private long renderCommandsRegistration(final TreeNode root, final boolean hideTiny, final double percent) {
        final long command = info.getStats().time(CommandTime);
        // most likely if commands search wasn't register then processing time will be less then 1 ms and so
        // no commands processing will be shown
        if (show(hideTiny, command)) {
            final TreeNode node = root.child("[%.2g%%] COMMANDS processed in %s",
                    command / percent, info.getStats().humanTime(CommandTime));
            final int registered = info.getCommands().size();
            if (registered > 0) {
                node.child("registered %s commands", registered);
            }
        }
        return command;
    }

    private long renderModulesProcessing(final TreeNode root, final double percent) {
        final long modules = info.getStats().time(ModulesProcessingTime);
        final TreeNode node = root.child("[%.2g%%] MODULES processed in %s",
                modules / percent, info.getStats().humanTime(ModulesProcessingTime));

        node.child("%s modules autowired", info.getModules().size());

        // identify if analysis were performed
        final int bindings = info.getStats().count(AnalyzedBindingsCount);
        if (bindings > 0) {
            node.child("%s elements found in %s user modules in %s",
                    // -1 because guicey bootstrap module is ignored
                    info.getStats().count(BindingsCount), info.getNormalModules().size() - 1,
                    info.getStats().humanTime(BindingsResolutionTime));
            final int found = info.getData().getItems(ConfigItem.Extension, Filters.fromBinding()).size();
            node.child("%s extensions detected from %s acceptable bindings", found, bindings);

            final int removed = info.getStats().count(RemovedBindingsCount);
            if (removed > 0) {
                node.child("%s elements removed", removed);
            }
            final int removedModules = info.getStats().count(RemovedInnerModules);
            if (removedModules > 0) {
                // multiple instance of the same module could be registered, but in stat it will be as 1 type
                node.child("%s inner modules removed (types)", removedModules);
            }
        }
        return modules;
    }

    private long renderInstallersProcessing(final TreeNode root, final boolean hideTiny, final double percent) {
        final long installers = info.getStats().time(InstallersTime);
        if (show(hideTiny, installers)) {
            final TreeNode node = root.child("[%.2g%%] INSTALLERS processed in %s",
                    installers / percent, info.getStats().humanTime(InstallersTime));
            final int registered = info.getInstallers().size();
            if (registered > 0) {
                node.child("registered %s installers", registered);

                final long extensions = info.getStats().time(ExtensionsRecognitionTime);
                if (show(hideTiny, extensions)) {
                    final int manual = info.getExtensionsRegisteredManauallyOnly().size();
                    node.child("%s extensions recognized from %s classes in %s",
                            info.getData().getItems(ConfigItem.Extension).size(),
                            info.getStats().count(ScanClassesCount)
                                    + info.getStats().count(AnalyzedBindingsCount) + manual,
                            info.getStats().humanTime(ExtensionsRecognitionTime));
                }
            }
        }
        return installers;
    }

    private long renderInjectorCreation(final TreeNode root, final boolean hideTiny, final double percent) {
        final long injector = info.getStats().time(InjectorCreationTime);
        final TreeNode node = root.child("[%.2g%%] INJECTOR created in %s",
                injector / percent, info.getStats().humanTime(InjectorCreationTime));
        info.getStats().getGuiceStats().forEach(it -> {
            if (!hideTiny || !it.endsWith(" 0 ms")) {
                node.child(it);
            }
        });
        return injector;
    }

    private long renderExtensionsInstallation(final TreeNode root, final boolean hideTiny, final double percent) {
        final long extensions = info.getStats().time(ExtensionsInstallationTime);
        if (show(hideTiny, extensions)) {
            final TreeNode node = root.child("[%.2g%%] EXTENSIONS installed in %s",
                    extensions / percent, info.getStats().humanTime(ExtensionsInstallationTime));

            node.child("%s extensions installed",
                    info.getExtensions().size());
            node.child("declared as: %s manual, %s scan, %s binding",
                    info.getExtensionsRegisteredManually().size(),
                    info.getExtensionsFromScan().size(),
                    info.getExtensionsFromBindings().size());
        }
        return extensions;
    }

    private long renderJerseyPart(final TreeNode root, final boolean hideTiny, final double percent) {
        final long hk = info.getStats().time(JerseyTime);
        if (show(hideTiny, hk)) {
            final TreeNode node = root.child("[%.2g%%] JERSEY bridged in %s",
                    hk / percent, info.getStats().humanTime(JerseyTime));
            final int installers = info.getData()
                    .getItems(ConfigItem.Installer, it -> JerseyInstaller.class.isAssignableFrom(it.getType())).size();
            if (installers > 0) {
                node.child("using %s jersey installers", installers);

                final int extensions = info.getData()
                        .getItems(ConfigItem.Extension, (ExtensionItemInfo it) -> it.isEnabled()
                                && JerseyInstaller.class.isAssignableFrom(it.getInstalledBy())).size();
                node.child("%s jersey extensions installed in %s",
                        extensions, info.getStats().humanTime(JerseyInstallerTime));
            }
        }
        return hk;
    }

    private boolean show(final boolean hideTiny, final long value) {
        return !hideTiny || value > 0;
    }
}
