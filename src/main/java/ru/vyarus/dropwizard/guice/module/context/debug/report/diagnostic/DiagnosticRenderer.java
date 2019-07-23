package ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import io.dropwizard.cli.Command;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.debug.report.ReportRenderer;
import ru.vyarus.dropwizard.guice.module.context.info.*;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
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
 * Not used and disabled installers could be rendered too.
 * If installer resolved by classpath scan then SCAN marker shown.
 * <p>
 * If extensions print enabled without installers enabled then all extensions are rendered in registration order.
 * Markers:
 * <ul>
 * <li>HOOK when extension installed by {@link GuiceyConfigurationHook}</li>
 * <li>SCAN when extension found by classpath scan</li>
 * <li>LAZY when {@link ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding}
 * annotation set</li>
 * <li>JERSEY when extension managed by jersey (annotated with
 * {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged})</li>
 * </ul>
 * <p>
 * Guice modules are rendered by type in registration order. OVERRIDE marker may appear if module was
 * registered as overriding.
 * <p>
 * Some extensions may come from different sources. For example, bundle could come from lookup and registered directly.
 * In such cases all markers are shown (indicating all sources), whereas actually only one source was used for
 * configuration and other sources were ignored.
 * <p>
 * When item registered multiple times marker REG(N) shown where N - registrations count.
 *
 * @author Vyacheslav Rusakov
 * @see GuiceyConfigurationInfo for diagnostic data source
 * @since 22.06.2016
 */
@Singleton
public class DiagnosticRenderer implements ReportRenderer<DiagnosticConfig> {

    private static final int SINGLE = 1;

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
            printDisabledExtensions(config, res, true);
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
            final CommandItemInfo info = service.getInfo(command);
            commonMarkers(markers, info);
            if (info.isEnvironmentCommand()) {
                markers.add("GUICE_ENABLED");
            }
            res.append(TAB).append(TAB).append(renderClassLine(command, markers)).append(NEWLINE);
        }
    }

    private void printBundles(final DiagnosticConfig config, final StringBuilder res) {
        // top level bundles
        final List<Class<GuiceyBundle>> bundles = service.getDirectBundles();
        if (!config.isPrintBundles() || bundles.isEmpty()) {
            return;
        }
        res.append(NEWLINE).append(NEWLINE).append(TAB).append("BUNDLES = ").append(NEWLINE);
        // bundles must be rendered just once, no matter that different instances of the same type could be registered
        // in different places - this report is per class
        final List<Class> rendered = new ArrayList<>();
        for (Class<GuiceyBundle> bundle : bundles) {
            renderBundleRecursive(res, bundle, 1, rendered);
        }
        if (config.isPrintDisabledItems()) {
            for (Class<GuiceyBundle> bundle : service.getBundlesDisabled()) {
                res.append(TAB).append(TAB).append(renderDisabledClassLine(bundle)).append(NEWLINE);
            }
        }
    }

    private void renderBundleRecursive(final StringBuilder res,
                                       final Class<GuiceyBundle> bundle,
                                       final int level,
                                       final List<Class> rendered) {
        final BundleItemInfo info = service.getInfo(bundle);
        final List<String> markers = Lists.newArrayList();
        if (info.isFromLookup()) {
            markers.add("LOOKUP");
        }
        commonMarkers(markers, info);
        for (int i = 0; i <= level; i++) {
            res.append(TAB);
        }
        res.append(renderClassLine(bundle, markers)).append(NEWLINE);
        final List<Class<GuiceyBundle>> bundles = service.getRelativelyInstalledBundles(bundle);

        rendered.add(bundle);
        for (Class<GuiceyBundle> relative : bundles) {
            if (!rendered.contains(relative)) {
                renderBundleRecursive(res, relative, level + 1, rendered);
            }
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

        if (config.isPrintDisabledItems()) {
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
            final InstallerItemInfo info = service.getInfo(installer);
            markers.clear();
            commonMarkers(markers, info);
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
        printDisabledExtensions(config, res, false);
    }

    private void printDisabledExtensions(final DiagnosticConfig config, final StringBuilder res,
                                         final boolean section) {
        final List<Class<Object>> extensions = service.getExtensionsDisabled();
        if (!config.isPrintExtensions() || !config.isPrintDisabledItems() || extensions.isEmpty()) {
            return;
        }

        if (section) {
            res.append(NEWLINE).append(NEWLINE).append(TAB).append("DISABLED EXTENSIONS = ").append(NEWLINE);
        }
        for (Class<Object> ext : extensions) {
            res.append(TAB).append(TAB).append(renderDisabledClassLine(ext)).append(NEWLINE);
        }
    }

    private String renderExtension(final Class<Object> extension) {
        final ExtensionItemInfo einfo = service.getInfo(extension);
        final List<String> markers = Lists.newArrayList();
        commonMarkers(markers, einfo);
        if (einfo.isLazy()) {
            markers.add("LAZY");
        }
        if (einfo.isJerseyManaged()) {
            markers.add("JERSEY");
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
        final List<String> markers = Lists.newArrayList();
        for (Class<Module> module : modules) {
            markers.clear();
            final ModuleItemInfo info = service.getInfo(module);
            commonMarkers(markers, info);
            if (info.isOverriding()) {
                markers.add("OVERRIDE");
            }
            res.append(TAB).append(TAB).append(renderClassLine(module, markers)).append(NEWLINE);
        }
        if (config.isPrintDisabledItems()) {
            for (Class<Module> module : service.getModulesDisabled()) {
                res.append(TAB).append(TAB).append(renderDisabledClassLine(module)).append(NEWLINE);
            }
        }
    }

    private void commonMarkers(final List<String> markers, final ItemInfo item) {
        if (item.getRegisteredBy().contains(ConfigScope.ClasspathScan.getKey())) {
            markers.add("SCAN");
        }
        if (item.getRegisteredBy().contains(ConfigScope.Hook.getKey())) {
            markers.add("HOOK");
        }
        if (item.getItemType().isInstanceConfig()) {
            // for instance types all registrations are aggregated into one record
            final List<ItemInfo> allItems = service.getData().getInfos(item.getType());
            // avoid printing number for single registration, but show duplicate cases even for 1 item
            if (allItems.size() > 1 || (allItems.size() == 1 && allItems.get(0).getRegistrationAttempts() > 1)) {
                int registrations = 0;
                for (ItemInfo info : allItems) {
                    registrations += info.getRegistrationAttempts();
                }
                markers.add(formatReg(allItems.size(), registrations));
            }
        } else if (item.getRegistrationAttempts() > SINGLE) {
            // for class based items only show registrations
            markers.add(formatReg(1, item.getRegistrationAttempts()));
        }
    }

    private String formatReg(int used, int registered) {
        return String.format("REG(%s/%s)", used, registered);
    }
}
