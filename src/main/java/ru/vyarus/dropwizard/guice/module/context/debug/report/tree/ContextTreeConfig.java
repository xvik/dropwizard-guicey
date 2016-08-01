package ru.vyarus.dropwizard.guice.module.context.debug.report.tree;

import com.google.common.collect.Sets;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;

import java.util.Arrays;
import java.util.Set;

/**
 * Configuration tree reporting configuration. Tree configuration is based on restriction: by default everything is
 * showed and additional configuration could only reduce displayed items. Configuration could be performed in
 * chained fashion:
 * <pre><code>
 *     TreeConfig config = new TreeConfig()
 *          .hideScopes(CoreInstallersBundle.class)
 *          .hideModules()
 *          .hideDisables()
 * </code></pre>
 *
 * @author Vyacheslav Rusakov
 * @see ContextTreeRenderer for usage
 * @since 17.07.2016
 */
public final class ContextTreeConfig {

    private final Set<ConfigItem> items = Sets.newHashSet();
    private final Set<Class<?>> scopes = Sets.newHashSet();
    private boolean disables;
    private boolean notUsedInstallers;
    private boolean duplicateRegistrations;
    private boolean emptyBundles;

    /**
     * @return set with item type to avoid printing or empty set
     */
    public Set<ConfigItem> getHiddenItems() {
        return items;
    }

    /**
     * @return set of scopes to avoid printing or empty set
     */
    public Set<Class<?>> getHiddenScopes() {
        return scopes;
    }

    /**
     * @return true to hide disable mentions, false otherwise
     */
    public boolean isHideDisables() {
        return disables;
    }

    /**
     * @return true to hide installers which did not install any extensions, false otherwise
     */
    public boolean isHideNotUsedInstallers() {
        return notUsedInstallers;
    }

    /**
     * @return true to hide duplicate items registrations, false otherwise
     */
    public boolean isHideDuplicateRegistrations() {
        return duplicateRegistrations;
    }

    /**
     * @return true to avoid showing empty bundles, false to always show bundles
     */
    public boolean isHideEmptyBundles() {
        return emptyBundles;
    }

    /**
     * Hide guice modules registrations.
     *
     * @return config instance for chained calls
     */
    public ContextTreeConfig hideModules() {
        items.add(ConfigItem.Module);
        return this;
    }

    /**
     * Hide installers registrations.
     *
     * @return config instance for chained calls
     */
    public ContextTreeConfig hideInstallers() {
        items.add(ConfigItem.Installer);
        return this;
    }

    /**
     * Hide extensions registrations.
     *
     * @return config instance for chained calls
     */
    public ContextTreeConfig hideExtensions() {
        items.add(ConfigItem.Extension);
        return this;
    }

    /**
     * Hide commands registrations (commands registered only by classpath scan).
     *
     * @return config instance for chained calls
     */
    public ContextTreeConfig hideCommands() {
        items.add(ConfigItem.Command);
        return this;
    }

    /**
     * Hide some scopes from report.
     * <p>
     * Warning: this can remove entire subtrees (for example, by hiding bundle which installs other bundles)
     * <p>
     * Application (root scope) can't be hidden (because it would hide entire report).
     *
     * @param avoid scopes to avoid printing
     * @return config instance for chained calls
     * @see ItemInfo#getRegisteredBy() for more info about scopes
     */
    public ContextTreeConfig hideScopes(final Class<?>... avoid) {
        scopes.addAll(Arrays.asList(avoid));
        return this;
    }

    /**
     * Hide disable mentions.
     *
     * @return config instance for chained calls
     */
    public ContextTreeConfig hideDisables() {
        disables = true;
        return this;
    }

    /**
     * Hides installers, which was not used (no extensions installed by this installer).
     *
     * @return config instance for chained calls
     */
    public ContextTreeConfig hideNotUsedInstallers() {
        this.notUsedInstallers = true;
        return this;
    }

    /**
     * By default, duplicate registrations are shown with "IGNORED" marker. Hiding duplicates removes these lines
     * from report.
     *
     * @return config instance for chained calls
     */
    public ContextTreeConfig hideDuplicateRegistrations() {
        this.duplicateRegistrations = true;
        return this;
    }

    /**
     * By default, if bundle does not contain registrations (or all registrations were filtered by
     * config) bundle is still shown (as leaf, without subtree). This option hides such visually "empty" bundles.
     *
     * @return config instance for chained calls
     */
    public ContextTreeConfig hideEmptyBundles() {
        this.emptyBundles = true;
        return this;
    }
}
