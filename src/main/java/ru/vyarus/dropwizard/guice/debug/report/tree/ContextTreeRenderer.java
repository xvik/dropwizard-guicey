package ru.vyarus.dropwizard.guice.debug.report.tree;

import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ru.vyarus.dropwizard.guice.debug.report.ReportRenderer;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.debug.util.TreeNode;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.Filters;
import ru.vyarus.dropwizard.guice.module.context.info.*;
import ru.vyarus.dropwizard.guice.module.context.info.sign.DisableSupport;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import java.util.List;
import java.util.Set;

import static ru.vyarus.dropwizard.guice.module.context.ConfigScope.*;

/**
 * Renders complete configuration tree.
 * <p>
 * Different instance items of the same type are marker by "#N" numbers. The only exception is duplicates - original
 * registered item name used (which this ignored instance is equal to) to indicate duplicate.
 * <p>
 * Report tries to preserve registration order but only within extension types. The only exception is ignored
 * instances, which are printed directly after original item appearance, even if they were registered further
 * in this scope.
 * For example, for registration like this:
 * <pre>{@code
 *      bundle
 *          .extensions(Ext1.class)
 *          .modules(new Mod(1), new OtherMod()
 *          .extensions(Ext2.class)
 *          .modules(new Mod(2), new Mod(1))
 * }</pre>
 * will be reported as
 * <pre>
 *    extension     Ext1
 *    extension     Ext2
 *    module        Mod
 *    module        -Mod        *DUPLICATE
 *    module        OtherMod
 *    module        Mod#2
 * </pre>
 * Which should be enough to associate with real configuration.
 * <p>
 * Used markers:
 * <ul>
 * <li>DW - marks dropwizard bundles</li>
 * <li>OPTIONAL - marks optional extensions</li>
 * <li>DISABLED - item disabled (by user)</li>
 * <li>DUPLICATE - item considered as duplicate by deduplication mechanism and ignored. If contains number
 * ("DUPLICATE(3)") then multiple instances were considered duplicate in this scope and ignored. </li>
 * </ul>
 * <p>
 * Recognized guice bindings are shown as separate subtree "GUICE BINDINGS" and not under modules in configuration
 * tree because it is impossible to associate binding with exact module instance (and multiple instances of the same
 * module could be registered). So bindings report is a "projection on class level".
 *
 * @author Vyacheslav Rusakov
 * @since 17.07.2016
 */
@SuppressWarnings("PMD.GodClass")
public class ContextTreeRenderer implements ReportRenderer<ContextTreeConfig> {

    private static final String DUPLICATE = "DUPLICATE";
    private static final String DISABLED = "DISABLED";

    private final GuiceyConfigurationInfo service;

    public ContextTreeRenderer(final GuiceyConfigurationInfo service) {
        this.service = service;
    }

    /**
     * Renders configuration tree report according to provided config.
     * By default report padded left with one tab. Subtrees are always padded with empty lines for better
     * visibility.
     *
     * @param config tree rendering config
     * @return rendered tree
     */
    @Override
    public String renderReport(final ContextTreeConfig config) {

        final Set<ItemId> scopes = service.getActiveScopes(!config.isHideDisables());

        final TreeNode root = new TreeNode("APPLICATION");
        if (!config.getHiddenScopes().contains(Application.getType())) {
            renderScopeContent(config, root, Application.getKey());
        }

        renderSpecialScope(config, scopes, root, "BUNDLES LOOKUP", BundleLookup);
        renderSpecialScope(config, scopes, root, "CLASSPATH SCAN", ClasspathScan);
        renderGuiceBindings(config, scopes, root);
        renderSpecialScope(config, scopes, root, "HOOKS", Hook);

        final StringBuilder res = new StringBuilder().append(Reporter.NEWLINE).append(Reporter.NEWLINE);
        root.render(res);
        return res.toString();
    }

    private void renderSpecialScope(final ContextTreeConfig config, final Set<ItemId> scopes,
                                    final TreeNode root, final String name, final ConfigScope scope) {
        if (isScopeVisible(config, scopes, scope.getKey())) {
            final TreeNode node = new TreeNode(name);
            renderScopeContent(config, node, scope.getKey());
            // scope may be empty due to hide configurations
            if (node.hasChildren()) {
                root.child(node);
            }
        }
    }

    /**
     * As it is impossible to track binding to exact module instance, recognized guice bindings are shown separately.
     *
     * @param config tree config
     * @param scopes active scopes
     * @param root   root render tree node
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void renderGuiceBindings(final ContextTreeConfig config,
                                     final Set<ItemId> scopes,
                                     final TreeNode root) {
        if (config.getHiddenScopes().contains(ConfigScope.Module.getType())
                || config.getHiddenItems().contains(ConfigItem.Module)) {
            return;
        }
        final TreeNode bindings = new TreeNode("GUICE BINDINGS");
        for (final ItemId scope : scopes) {
            if (recognize(scope).equals(Module)) {
                final TreeNode module = new TreeNode(RenderUtils.renderClassLine(scope.getType()));
                // note that items may actually be bound by submodules, but they always show under the top module
                // (only top module is visible in configuration)
                renderScopeItems(config, module, scope);
                if (module.hasChildren()) {
                    bindings.child(module);
                }
            }
        }
        if (bindings.hasChildren()) {
            root.child(bindings);
        }
    }

    /**
     * Render entire scope subtree. Scope items are rendered first and bundles at the end (because most likely
     * they will be subtrees).
     *
     * @param config tree config
     * @param root   root node
     * @param scope  scope to render
     */
    private void renderScopeContent(final ContextTreeConfig config, final TreeNode root, final ItemId scope) {
        renderScopeItems(config, root, scope);

        final List<ItemId<Object>> bundles = service.getData()
                .getItems(Filters.bundles().and(Filters.registeredBy(scope)));

        for (ItemId<Object> bundle : bundles) {
            renderBundle(config, root, scope, bundle);
        }
    }

    /**
     * Render simple scope items (except bundles) including installer disables.
     *
     * @param config tree config
     * @param root   root node
     * @param scope  current scope
     */
    private void renderScopeItems(final ContextTreeConfig config, final TreeNode root, final ItemId scope) {
        final List<ItemId<Object>> items = service.getData()
                .getItems(Filters.bundles().negate().and(Filters.registeredBy(scope)));

        final List<String> markers = Lists.newArrayList();
        for (ItemId<?> item : items) {
            final ItemInfo info = service.getData().getInfo(item);
            if (!isHidden(config, info, scope)) {
                renderItem(root, scope, info, markers, false);
            }
            // check if item is ignored in the same scope as registration and render ignores
            if (!config.isHideDuplicateRegistrations()
                    && scope.equals(info.getRegistrationScope())
                    && info.getIgnoresByScope(scope) > 0) {
                renderItem(root, scope, info, markers, true);
            }
        }

        if (!config.isHideDisables()) {
            final List<ItemId<Object>> disabled = service.getData().getItems(Filters.disabledBy(scope));
            for (ItemId<?> item : disabled) {
                renderLeaf(root, "-disable", item, 0, null);
            }
        }
    }

    /**
     * Render ignored or not scope item.
     *
     * @param root     root node
     * @param scope    current scope
     * @param info     item descriptor
     * @param markers  markers
     * @param asIgnore true for ignored item render
     */
    private void renderItem(final TreeNode root,
                            final ItemId scope,
                            final ItemInfo info,
                            final List<String> markers,
                            final boolean asIgnore) {
        markers.clear();
        fillCommonMarkers(info, markers, scope, asIgnore);
        renderLeaf(root,
                info.getItemType().name().toLowerCase(),
                info.getId(),
                info.getItemType().isInstanceConfig() ? ((InstanceItemInfo) info).getInstanceCount() : 0,
                markers);
    }

    /**
     * Render simple (leaf) child node.
     *
     * @param root    root node
     * @param name    child node name
     * @param item    reference item class
     * @param pos     item registration position in scope
     * @param markers markers (may be null)
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_INFERRED")
    private void renderLeaf(final TreeNode root, final String name,
                            final ItemId<?> item, final int pos, final List<String> markers) {
        final boolean ignored = markers != null
                && markers.stream().anyMatch(it -> it.equals(DISABLED) || it.startsWith(DUPLICATE));
        root.child(String.format("%-10s ", name) + (ignored
                ? RenderUtils.renderDisabledClassLine(item.getType(), pos, markers)
                : RenderUtils.renderClassLine(item.getType(), pos, markers)));
    }

    /**
     * Renders bundle if allowed.
     *
     * @param config tree config
     * @param root   current node
     * @param scope  current scope
     * @param bundle bundle class
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_INFERRED")
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    private void renderBundle(final ContextTreeConfig config, final TreeNode root,
                              final ItemId<?> scope, final ItemId<Object> bundle) {
        final BundleItemInfo info = service.getData().getInfo(bundle);
        final List<String> markers = Lists.newArrayList();
        if (!isHidden(config, info, scope)) {
            fillCommonMarkers(info, markers, scope, false);
            // duplicates could contain counter (e.g. DUPLICATE(2))
            final boolean duplicate = markers.stream().anyMatch(it -> it.startsWith(DUPLICATE));
            final TreeNode node = new TreeNode(duplicate || markers.contains(DISABLED)
                    ? RenderUtils.renderDisabledClassLine(bundle.getType(), info.getInstanceCount(), markers)
                    : RenderUtils.renderClassLine(bundle.getType(), info.getInstanceCount(), markers));
            // avoid duplicate bundle content render
            if (!isDuplicateRegistration(info, scope)) {
                renderScopeContent(config, node, bundle);
            }
            // avoid showing empty bundle line if configured to hide (but show if bundle is ignored as duplicate)
            if (node.hasChildren() || !config.isHideEmptyBundles() || (!config.isHideDisables() && duplicate)) {
                root.child(node);
            }
        }

        // check if bundle is also ignored in this scope and show it
        if (!config.isHideDuplicateRegistrations()
                && scope.equals(info.getRegistrationScope())
                && info.getIgnoresByScope(scope) > 0) {
            markers.clear();
            fillCommonMarkers(info, markers, scope, true);
            root.child(RenderUtils.renderDisabledClassLine(bundle.getType(), info.getInstanceCount(), markers));
        }
    }

    private void fillCommonMarkers(final ItemInfo info,
                                   final List<String> markers,
                                   final ItemId scope,
                                   final boolean asIgnore) {
        if (info.getItemType() == ConfigItem.DropwizardBundle) {
            // dropwizard bundle marker
            markers.add("DW");
        }
        if (info.getItemType() == ConfigItem.Extension && ((ExtensionItemInfo) info).isOptional()) {
            // optional extension marker
            markers.add("OPTIONAL");
        }
        if (asIgnore || isDuplicateRegistration(info, scope)) {
            final int cnt = info.getIgnoresByScope(scope);
            markers.add(DUPLICATE + (cnt > 1 ? "(" + cnt + ")" : ""));
        }
        if (isDisabled(info)) {
            markers.add(DISABLED);
        }
    }

    /**
     * @param config tree config
     * @param scopes active scopes: containing items (may be null)
     * @param scope  scope to check
     * @return true if scope visible, false otherwise
     */
    private boolean isScopeVisible(final ContextTreeConfig config,
                                   final Set<ItemId> scopes,
                                   final ItemId scope) {
        return !config.getHiddenScopes().contains(scope.getType())
                && (scopes == null || scopes.contains(scope));
    }

    /**
     * Checks element visibility according to config. Universal place to check visibility for either simple config
     * items or bundles and special scopes.
     *
     * @param config tree configuration
     * @param info   current item info
     * @param scope  current item rendering scope
     * @return true if item is hidden, false otherwise
     */
    @SuppressWarnings("checkstyle:BooleanExpressionComplexity")
    private boolean isHidden(final ContextTreeConfig config, final ItemInfo info, final ItemId scope) {
        // item explicitly hidden
        final boolean hidden = config.getHiddenItems().contains(info.getItemType());
        // installer disabled
        final boolean disabled = config.isHideDisables() && isDisabled(info);
        // duplicate registration
        final boolean ignored = config.isHideDuplicateRegistrations() && isDuplicateRegistration(info, scope);
        // item in scope hidden by config (special case for bundle: when its hidden by config)
        final boolean hiddenScope = !isScopeVisible(config, null, scope) || isHiddenBundle(config, info);
        // installer without any extension
        final boolean notUsedInstaller = config.isHideNotUsedInstallers() && isNotUsedInstaller(info);
        return hidden
                || disabled
                || ignored
                || hiddenScope
                || notUsedInstaller;
    }

    /**
     * @param config tree config
     * @param info   item info
     * @return true if item is bundle and its hidden by config, false otherwise
     */
    private boolean isHiddenBundle(final ContextTreeConfig config, final ItemInfo info) {
        return ConfigItem.Bundle.equals(info.getItemType()) && !isScopeVisible(config, null, info.getId());
    }

    /**
     * @param info item info
     * @return true if item is installer and no extensions were installed with it, false otherwise
     */
    @SuppressWarnings("unchecked")
    private boolean isNotUsedInstaller(final ItemInfo info) {
        return info.getItemType() == ConfigItem.Installer
                && service.getExtensions((Class<FeatureInstaller>) info.getType()).isEmpty();
    }

    /**
     * @param item item info
     * @return true if item disabled, false if enabled or doesn't support disabling
     */
    private boolean isDisabled(final ItemInfo item) {
        return item instanceof DisableSupport && !((DisableSupport) item).isEnabled();
    }

    /**
     * @param item  item info
     * @param scope current scope
     * @return true if item was not registered in provided scope (scope performed duplicate registration),
     * false otherwise
     */
    private boolean isDuplicateRegistration(final ItemInfo item, final ItemId scope) {
        return !scope.equals(item.getRegistrationScope());
    }
}
