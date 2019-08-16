package ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.cli.Command;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.debug.report.ReportRenderer;
import ru.vyarus.dropwizard.guice.module.context.info.*;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.TypeInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.option.WithOptions;
import ru.vyarus.dropwizard.guice.module.installer.order.Ordered;

import java.util.ArrayList;
import java.util.Collections;
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
 * Bundles rendered as tree to indicate transitive installations. Dropwizard and guicey bundles are rendered together:
 * first dropwizard bundles (because they actually register first) and then guicey.
 * Possible markers:
 * <ul>
 * <li>LOOKUP when bundle come from lookup mechanism</li>
 * <li>DW when bundle is dropwizard bundles</li>
 * <li>IGNORED if bundle was ignored because of deduplication logic</li>
 * <li>DISABLED if bundle was manually disabled</li>
 * </ul>
 * <p>
 * Installers are rendered in execution order. Extensions (if enabled) are rendered relative to used installer.
 * Not used and disabled installers could be rendered too.
 * If installer resolved by classpath scan then SCAN marker shown.
 * <p>
 * Installer report could contain installer interface markers in order to indicate installer abilities:
 * <ul>
 * <li>TYPE - install extension by type</li>
 * <li>OBJECT - install extension by instance</li>
 * <li>BIND - creates custom guicey bindings</li>
 * <li>JERSEY - applies custom jersey configuration</li>
 * <li>OPTIONS - supports options</li>
 * </ul>
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
 * <li>ORDER when installer supports ordering</li>
 * </ul>
 * <p>
 * Guice modules are rendered by type in registration order. OVERRIDE marker may appear if module was
 * registered as overriding.
 * <p>
 * When item registered multiple times marker REG(N/M) shown where N - accepted registrations count and M -
 * overall registrations count. For class based registarions it would always be REG(1/M). For instance registrations
 * any items count could be accepted (according to deduplication logic).
 *
 * @author Vyacheslav Rusakov
 * @see GuiceyConfigurationInfo for diagnostic data source
 * @since 22.06.2016
 */
public class DiagnosticRenderer implements ReportRenderer<DiagnosticConfig> {

    private static final int SINGLE = 1;
    private static final String DW = "DW";
    private static final String JERSEY = "JERSEY";

    private final GuiceyConfigurationInfo service;

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
        // top level bundles (dropwizard bundles first, then guicey - correct order)
        final List<Class<Object>> bundles = service.getDirectBundles();
        if (!config.isPrintBundles() || bundles.isEmpty()) {
            return;
        }
        res.append(NEWLINE).append(NEWLINE).append(TAB).append("BUNDLES = ").append(NEWLINE);
        // bundles must be rendered just once, no matter that different instances of the same type could be registered
        // in different places - this report is per class
        // all root bundles must be rendered at root scope, no matter if they appear transitively
        // NOTE: dropwizard and guicey bundle lists are not ordered in registration order - instead all guicey bundles
        // go first and next all dropwizard bundles
        final List<Class> rendered = new ArrayList<>(bundles);
        for (Class<Object> bundle : bundles) {
            renderBundleRecursive(res, bundle, 1, rendered);
        }
        if (config.isPrintDisabledItems()) {
            final List<String> dwMarker = Collections.singletonList(DW);
            for (Class<Object> bundle : service.getBundlesDisabled()) {
                res.append(TAB).append(TAB).append(renderDisabledClassLine(bundle, 0,
                        ConfiguredBundle.class.isAssignableFrom(bundle) ? dwMarker : null))
                        .append(NEWLINE);
            }
        }
    }

    private void renderBundleRecursive(final StringBuilder res,
                                       final Class<Object> bundle,
                                       final int level,
                                       final List<Class> rendered) {
        final BundleItemInfo info = service.getInfo(bundle);
        final List<String> markers = Lists.newArrayList();
        if (info.isDropwizard()) {
            markers.add(DW);
        } else if (((GuiceyBundleItemInfo) info).isFromLookup()) {
            markers.add("LOOKUP");
        }
        commonMarkers(markers, info);
        for (int i = 0; i <= level; i++) {
            res.append(TAB);
        }
        res.append(renderClassLine(bundle, markers)).append(NEWLINE);
        // dropwizrd bundles first (as they register immediately, then guicey
        final List<Class<Object>> bundles = service.getRelativelyInstalledBundles(bundle);

        rendered.add(bundle);
        for (Class<Object> relative : bundles) {
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
            final String prefix = config.isPrintInstallerInterfaceMarkers()
                    ? renderInstallerMarkers(installer) : "";
            res.append(TAB).append(TAB).append(prefix).append(renderInstaller(installer, markers)).append(NEWLINE);

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
            markers.add(JERSEY);
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

    private String renderInstallerMarkers(final Class type) {
        final List<String> markers = new ArrayList<>();
        if (TypeInstaller.class.isAssignableFrom(type)) {
            markers.add("TYPE");
        }
        if (InstanceInstaller.class.isAssignableFrom(type)) {
            markers.add("OBJECT");
        }
        if (JerseyInstaller.class.isAssignableFrom(type)) {
            markers.add(JERSEY);
        }
        if (BindingInstaller.class.isAssignableFrom(type)) {
            markers.add("BIND");
        }
        if (WithOptions.class.isAssignableFrom(type)) {
            markers.add("OPTIONS");
        }
        if (Ordered.class.isAssignableFrom(type)) {
            markers.add("ORDER");
        }
        return String.format("%-30s ", String.join(", ", markers));
    }
}
