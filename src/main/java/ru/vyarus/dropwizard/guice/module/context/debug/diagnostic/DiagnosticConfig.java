package ru.vyarus.dropwizard.guice.module.context.debug.diagnostic;

/**
 * Diagnostic reporting configuration. Config instance could be configured in chained fashion:
 * <pre><code>
 *     DiagnosticConfig config = new DiagnosticConfig()
 *          .printBundles()
 *          .printInstallers()
 *          .printExtensions()
 * </code></pre>
 * There are two presets for enabling default info logging (most commonly useful, {{@link #printDefaults()}})
 * and all info logging ({@link #printAll()}):
 * <pre><code>
 *     DiagnosticConfig config = new DiagnosticConfig().printDefaults()
 * </code></pre>
 *
 * @author Vyacheslav Rusakov
 * @see DiagnosticRenderer for usage
 * @since 13.07.2016
 */
public final class DiagnosticConfig {

    private boolean bundles;
    private boolean installers;
    private boolean notUsedInstallers;
    private boolean disabledInstallers;
    private boolean extensions;
    private boolean modules;

    /**
     * @return true if no prints configured, false otherwise
     */
    @SuppressWarnings("checkstyle:BooleanExpressionComplexity")
    public boolean isEmptyConfig() {
        return !(bundles || installers || notUsedInstallers || disabledInstallers || extensions || modules);
    }

    /**
     * @return true if bundles logging enabled
     */
    public boolean isPrintBundles() {
        return bundles;
    }

    /**
     * @return true if installers logging enabled
     */
    public boolean isPrintInstallers() {
        return installers;
    }

    /**
     * @return true if disabled installers logging enabled
     */
    public boolean isPrintDisabledInstallers() {
        return disabledInstallers;
    }

    /**
     * @return true if not used installers (without extensions) logging enabled
     */
    public boolean isPrintNotUsedInstallers() {
        return notUsedInstallers;
    }

    /**
     * @return true if extensions logging enabled
     */
    public boolean isPrintExtensions() {
        return extensions;
    }

    /**
     * @return true if guice modules logging enabled
     */
    public boolean isPrintModules() {
        return modules;
    }

    /**
     * Enables used bundles print.
     *
     * @return config instance for chained calls
     */
    public DiagnosticConfig printBundles() {
        bundles = true;
        return this;
    }

    /**
     * Enables installers print. By default prints only installers which install at least one extension.
     * <p>
     * If extensions printing enabled ({@link #printExtensions()}), then extensions will be printed
     * just after installer.
     * <p>
     * Both installers and extensions printed in execution order (sorted).
     * <p>
     * If disabled installers print enabled ({@link #printDisabledInstallers()}) then they will be printed
     * at the end of installers list.
     * <p>
     * All installers printing may be enabled with not used installers ({@link #printNotUsedInstallers()}).
     *
     * @return config instance for chained calls
     */
    public DiagnosticConfig printInstallers() {
        installers = true;
        return this;
    }

    /**
     * Enables disabled installers print. Requires {@link #printInstallers()} enabled.
     *
     * @return config instance for chained calls
     */
    public DiagnosticConfig printDisabledInstallers() {
        disabledInstallers = true;
        return this;
    }

    /**
     * Enables not used installers print. Requires {@link #printInstallers()} enabled.
     *
     * @return config instance for chained calls
     */
    public DiagnosticConfig printNotUsedInstallers() {
        notUsedInstallers = true;
        return this;
    }

    /**
     * Enables extensions print. If installers print enabled ({@link #printInstallers()}) then extensions
     * will be printed relative to used installer, otherwise extensions printed as simple list.
     *
     * @return config instance for chained calls
     */
    public DiagnosticConfig printExtensions() {
        extensions = true;
        return this;
    }

    /**
     * Enables registered guice modules print (only directly registered modules).
     *
     * @return config instance for chained calls
     */
    public DiagnosticConfig printModules() {
        modules = true;
        return this;
    }

    /**
     * Shortcut method to enables all possible prints.
     *
     * @return config instance for chained calls
     */
    public DiagnosticConfig printAll() {
        printDefaults();
        printDisabledInstallers();
        printNotUsedInstallers();
        return this;
    }

    /**
     * Enables default prints (most useful info): bundles, installers, extensions, modules.
     *
     * @return config instance for chained calls
     */
    public DiagnosticConfig printDefaults() {
        printBundles();
        printInstallers();
        printExtensions();
        printModules();
        return this;
    }
}
