package ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.inject.Module;
import io.dropwizard.cli.Command;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.Filters;
import ru.vyarus.dropwizard.guice.module.context.debug.report.ReportRenderer;
import ru.vyarus.dropwizard.guice.module.context.info.BundleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.CommandItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.InstallerItemInfo;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static ru.vyarus.dropwizard.guice.module.context.debug.util.RenderUtils.*;
import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.NEWLINE;
import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.TAB;

/**
 * Render configuration diagnostic info.
 * <p>
 * Commands rendered in registration order. All commands are rendered with SCAN marker to indicate
 * classpath scanning. Environment commands are also marked with GUICE_ENABLED to indicate that only
 * these commands may use guice injections.
 * <p>
 * Bundles rendered as tree to indicate transitive installations.
 * Possible markers:
 * <ul>
 * <li>LOOKUP when bundle come from lookup mechanism</li>
 * <li>DW when bundle recognized from dropwizard bundle (disabled by default)</li>
 * </ul>
 * <p>
 * Installers are rendered in execution order. Extensions (if enabled) are rendered relative to used installer.
 * If extension managed by multiple installers it wil appear multiple times. Not used and disabled
 * installers could be rendered too.
 * If installer resolved by classpath scan then SCAN marker shown.
 * <p>
 * If extensions print enabled without installers enabled then all extensions are rendered in registration order.
 * Markers:
 * <ul>
 * <li>SCAN when extension found by classpath scan</li>
 * <li>LAZY when {@link ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding}
 * annotation set</li>
 * <li>HK when extension managed by HK (annotated with
 * {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed})</li>
 * </ul>
 * <p>
 * Guice modules are rendered by type in registration order.
 * <p>
 * Some extensions may come from different sources. For example, bundle could come from lookup and registered directly.
 * In such cases all markers are shown (indicating all sources), whereas actually only one source was used for
 * configuration and other sources were ignored.
 *
 * @author Vyacheslav Rusakov
 * @see GuiceyConfigurationInfo for diagnostic data source
 * @since 22.06.2016
 */
@Singleton
public class DiagnosticRenderer implements ReportRenderer<DiagnosticConfig> {

    public static final String SCAN = "SCAN";
    private final GuiceyConfigurationInfo service;

    @Inject
    public DiagnosticRenderer(final GuiceyConfigurationInfo service) {
        this.service = service;
    }

    /**
     * Renders diagnostic report according to config.
     *
     * @param config print config
     * @return rendered diagnostic
     */
    @Override
    public String renderReport(final DiagnosticConfig config) {
        final StringBuilder res = new StringBuilder();
        printCommands(config, res);
        printBundles(config, res);
        if (config.isPrintInstallers()) {
            printInstallers(config, res);
        } else {
            printExtensionsOnly(config, res);
        }
        printModules(config, res);
        return res.toString();
    }

    private void printCommands(final DiagnosticConfig config, final StringBuilder res) {
        final List<Class<Command>> commands = service.getCommands();
        if (!config.isPrintCommands() || commands.isEmpty()) {
            return;
        }
        // modules will never be empty
        res.append(NEWLINE).append(NEWLINE).append(TAB).append("COMMANDS = ").append(NEWLINE);
        final List<String> markers = Lists.newArrayList();
        for (Class<Command> command : commands) {
            markers.clear();
            final CommandItemInfo info = service.getData().getInfo(command);
            markers.add(SCAN);
            if (info.isEnvironmentCommand()) {
                markers.add("GUICE_ENABLED");
            }
            res.append(TAB).append(TAB).append(renderClassLine(command, markers)).append(NEWLINE);
        }
    }

    private void printBundles(final DiagnosticConfig config, final StringBuilder res) {
        // top level bundles
        final List<Class<GuiceyBundle>> bundles = service.getData()
                .getItems(ConfigItem.Bundle, new Predicate<BundleItemInfo>() {
                    @Override
                    public boolean apply(final @Nonnull BundleItemInfo input) {
                        return input.isRegisteredDirectly() || input.isFromLookup() || input.isFromDwBundle();
                    }
                });
        if (!config.isPrintBundles() || bundles.isEmpty()) {
            return;
        }
        res.append(NEWLINE).append(NEWLINE).append(TAB).append("BUNDLES = ").append(NEWLINE);
        for (Class<GuiceyBundle> bundle : bundles) {
            renderBundleRecursive(res, bundle, 1);
        }
    }

    private void renderBundleRecursive(final StringBuilder res, final Class<GuiceyBundle> bundle, final int level) {
        final BundleItemInfo info = service.getData().getInfo(bundle);
        final List<String> markers = Lists.newArrayList();
        if (info.isFromLookup()) {
            markers.add("LOOKUP");
        }
        if (info.isFromDwBundle()) {
            markers.add("DW");
        }
        for (int i = 0; i <= level; i++) {
            res.append(TAB);
        }
        res.append(renderClassLine(bundle, markers)).append(NEWLINE);
        final List<Class<GuiceyBundle>> bundles = service.getData()
                .getItems(ConfigItem.Bundle, Filters.registrationScope(bundle));
        for (Class<GuiceyBundle> relative : bundles) {
            renderBundleRecursive(res, relative, level + 1);
        }
    }

    private void printInstallers(final DiagnosticConfig config, final StringBuilder res) {
        final List<Class<FeatureInstaller>> installers = service.getInstallersOrdered();
        if (installers.isEmpty()) {
            return;
        }

        res.append(NEWLINE).append(NEWLINE).append(TAB).append("INSTALLERS ");
        if (config.isPrintExtensions()) {
            res.append("and EXTENSIONS ");
        }
        res.append("in processing order = ").append(NEWLINE);

        renderInstallers(config, res, installers);

        if (config.isPrintDisabledInstallers()) {
            for (Class<FeatureInstaller> installer : service.getInstallersDisabled()) {
                res.append(TAB).append(TAB).append(renderDisabledInstaller(installer)).append(NEWLINE);
            }
        }
    }

    private void renderInstallers(final DiagnosticConfig config, final StringBuilder res,
                                  final List<Class<FeatureInstaller>> installers) {
        final List<String> markers = Lists.newArrayList();
        for (Class<FeatureInstaller> installer : installers) {
            final List<Class<Object>> extensions = service.getExtensionsOrdered(installer);
            if (extensions.isEmpty() && !config.isPrintNotUsedInstallers()) {
                continue;
            }
            final InstallerItemInfo info = service.getData().getInfo(installer);
            markers.clear();
            if (info.isFromScan()) {
                markers.add(SCAN);
            }
            res.append(TAB).append(TAB).append(renderInstaller(installer, markers)).append(NEWLINE);

            if (config.isPrintExtensions()) {
                for (Class<Object> ext : extensions) {
                    res.append(TAB).append(TAB).append(TAB).append(renderExtension(ext)).append(NEWLINE);
                }
            }
        }
    }

    private void printExtensionsOnly(final DiagnosticConfig config, final StringBuilder res) {
        final List<Class<Object>> extensions = service.getExtensions();
        if (!config.isPrintExtensions() || extensions.isEmpty()) {
            return;
        }
        res.append(NEWLINE).append(NEWLINE).append(TAB).append("EXTENSIONS = ").append(NEWLINE);
        for (Class<Object> ext : extensions) {
            res.append(TAB).append(TAB).append(renderExtension(ext)).append(NEWLINE);
        }
    }

    private String renderExtension(final Class<Object> extension) {
        final ExtensionItemInfo einfo = service.getData().getInfo(extension);
        final List<String> markers = Lists.newArrayList();
        if (einfo.isFromScan()) {
            markers.add(SCAN);
        }
        if (einfo.isLazy()) {
            markers.add("LAZY");
        }
        if (einfo.isHk2Managed()) {
            markers.add("HK");
        }
        return renderClassLine(extension, markers);
    }

    private void printModules(final DiagnosticConfig config, final StringBuilder res) {
        final List<Class<Module>> modules = service.getModules();
        if (!config.isPrintModules() || modules.isEmpty()) {
            return;
        }
        // modules will never be empty
        res.append(NEWLINE).append(NEWLINE).append(TAB).append("GUICE MODULES = ").append(NEWLINE);
        for (Class<Module> module : modules) {
            res.append(TAB).append(TAB).append(renderClassLine(module, null)).append(NEWLINE);
        }
    }
}
