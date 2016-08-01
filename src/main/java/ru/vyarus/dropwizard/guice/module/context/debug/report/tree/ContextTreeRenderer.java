package ru.vyarus.dropwizard.guice.module.context.debug.report.tree;

import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.dropwizard.Application;
import io.dropwizard.Bundle;
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.Filters;
import ru.vyarus.dropwizard.guice.module.context.debug.report.ReportRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.context.debug.util.TreeNode;
import ru.vyarus.dropwizard.guice.module.context.info.BundleItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.sign.DisableSupport;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;

/**
 * Renders complete configuration tree.
 *
 * @author Vyacheslav Rusakov
 * @since 17.07.2016
 */
@Singleton
public class ContextTreeRenderer implements ReportRenderer<ContextTreeConfig> {

    private final GuiceyConfigurationInfo service;

    @Inject
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

        final Set<Class<?>> scopes = service.getActiveScopes();

        final TreeNode root = new TreeNode("APPLICATION");
        renderScopeContent(config, root, Application.class);

        renderSpecialScope(config, scopes, root, "BUNDLES LOOKUP", GuiceyBundleLookup.class);
        renderSpecialScope(config, scopes, root, "DROPWIZARD BUNDLES", Bundle.class);
        renderSpecialScope(config, scopes, root, "CLASSPATH SCAN", ClasspathScanner.class);

        final StringBuilder res = new StringBuilder().append(Reporter.NEWLINE).append(Reporter.NEWLINE);
        root.render(res);
        return res.toString();
    }

    private void renderSpecialScope(final ContextTreeConfig config, final Set<Class<?>> scopes,
                                    final TreeNode root, final String name, final Class<?> scope) {
        if (isScopeVisible(config, scopes, scope)) {
            final TreeNode node = new TreeNode(name);
            renderScopeContent(config, node, scope);
            // scope may be empty due to hide configurations
            if (node.hasChildren()) {
                root.child(node);
            }
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
    private void renderScopeContent(final ContextTreeConfig config, final TreeNode root, final Class<?> scope) {
        renderScopeItems(config, root, scope);

        final List<Class<Object>> bundles = service.getData()
                .getItems(and(Filters.registeredBy(scope), Filters.type(ConfigItem.Bundle)));

        for (Class<Object> bundle : bundles) {
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
    private void renderScopeItems(final ContextTreeConfig config, final TreeNode root, final Class<?> scope) {
        final List<Class<Object>> items = service.getData()
                .getItems(and(Filters.registeredBy(scope), not(Filters.type(ConfigItem.Bundle))));

        final List<String> markers = Lists.newArrayList();
        for (Class<?> item : items) {
            markers.clear();
            final ItemInfo info = service.getData().getInfo(item);
            if (isHidden(config, info, scope)) {
                continue;
            }
            fillCommonMarkers(info, markers, scope);
            renderLeaf(root, info.getItemType().name().toLowerCase(), item, markers);
        }

        if (!config.isHideDisables()) {
            final List<Class<Object>> disabled = service.getData().getItems(Filters.disabledBy(scope));
            for (Class<?> item : disabled) {
                renderLeaf(root, "-disable", item, null);
            }
        }
    }

    /**
     * Render simple (leaf) child node.
     *
     * @param root    root node
     * @param name    child node name
     * @param item    reference item class
     * @param markers markers (may be null)
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_INFERRED")
    private void renderLeaf(final TreeNode root, final String name,
                            final Class<?> item, final List<String> markers) {
        root.child(String.format("%-10s ", name) + RenderUtils.renderClassLine(item, markers));
    }

    /**
     * Renders bundle if allowed.
     *
     * @param config tree config
     * @param root   current node
     * @param scope  current scope
     * @param bundle bundle class
     */
    private void renderBundle(final ContextTreeConfig config, final TreeNode root,
                              final Class<?> scope, final Class<Object> bundle) {
        final BundleItemInfo info = service.getData().getInfo(bundle);
        if (isHidden(config, info, scope)) {
            return;
        }
        final List<String> markers = Lists.newArrayList();
        fillCommonMarkers(info, markers, scope);
        final TreeNode node = new TreeNode(RenderUtils.renderClassLine(bundle, markers));
        // avoid duplicate bundle content render
        if (!isDuplicateRegistration(info, scope)) {
            renderScopeContent(config, node, bundle);
        }
        // avoid showing empty bundle line if configured to hide
        if (node.hasChildren() || !config.isHideEmptyBundles()) {
            root.child(node);
        }
    }

    private void fillCommonMarkers(final ItemInfo info, final List<String> markers, final Class<?> scope) {
        if (isDuplicateRegistration(info, scope)) {
            markers.add("IGNORED");
        }
        if (isDisabled(info)) {
            markers.add("DISABLED");
        }
    }

    /**
     * @param config tree config
     * @param scopes active scopes: containing items (may be null)
     * @param scope  scope to check
     * @return true if scope visible, false otherwise
     */
    private boolean isScopeVisible(final ContextTreeConfig config, final Set<Class<?>> scopes, final Class<?> scope) {
        return !config.getHiddenScopes().contains(scope)
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
    private boolean isHidden(final ContextTreeConfig config, final ItemInfo info, final Class<?> scope) {
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
        return ConfigItem.Bundle.equals(info.getItemType()) && !isScopeVisible(config, null, info.getType());
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
    private boolean isDuplicateRegistration(final ItemInfo item, final Class<?> scope) {
        return !scope.equals(item.getRegistrationScope());
    }
}
