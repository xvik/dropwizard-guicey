package ru.vyarus.dropwizard.guice.module.context.stat;

import com.google.common.base.Preconditions;

/**
 * Guicey metrics collected at startup.
 *
 * @author Vyacheslav Rusakov
 * @since 27.07.2016
 */
public enum Stat {

    /**
     * Timer started in time of guice bundle creation for measuring entire application startup time
     * (used for measuring time since startup).
     */
    OverallTime(true),
    /**
     * Overall guicey startup time (including configuration, run and jersey parts). All other timers represents this
     * timer detalization.
     */
    GuiceyTime(true),
    /**
     * Guicey time in dropwizard configuration phase. Part of {@link #GuiceyTime}.
     */
    ConfigurationTime(true),
    /**
     * Time of bundle configuration. Part of {@link #ConfigurationTime}
     */
    BundleBuilderTime(true),
    /**
     * Hooks resolution and processing time. Part of {@link #ConfigurationTime}.
     */
    HooksTime(true),
    /**
     * Commands processing time. Includes environment commands members injection (always performed)
     * and commands registration from classpath scan (disabled by default). Part of {@link #ConfigurationTime} and
     * a bit of {@link #RunTime} (fields initialization).
     */
    CommandTime(true),
    /**
     * Classpath scan time (time to resolve all classes from configured packages). Part of {@link #ConfigurationTime}.
     */
    ScanTime(true),
    /**
     * Count of classes loaded during classpath scan.
     */
    ScanClassesCount(false),
    /**
     * Bundles resolution, creation, initialization and run time (combined from both configuration and run phases).
     * Also includes dropwizard bundles initialization time (for bundles registered through guicey api).
     */
    BundleTime(true),
    /**
     * Bundles resolution time only (lookup mechanism).
     * Part of {@link #BundleTime}.
     */
    BundleResolutionTime(true),
    /**
     * Initialization time of registered dropwizard bundles. Part of {@link #BundleTime}.
     */
    DropwizardBundleInitTime(true),
    /**
     * Initialization time of registered guicey bundles. Part of {@link #BundleTime}.
     */
    GuiceyBundleInitTime(true),
    /**
     * Installers resolution, instantiation and execution time. Contains time from both initialization and run
     * phases.
     */
    InstallersTime(true),
    /**
     * Installers search and instantiation time. Part of {@link #InstallersTime}.
     */
    InstallersResolutionTime(true),
    /**
     * Time spent on extensions resolution (matching all extension classes with configured installers ).
     * Does not contain classpath scan time, because already use cached scan result (actual scan performed
     * before initializations). Part of {@link #InstallersTime}.
     */
    ExtensionsRecognitionTime(true),
    /**
     * Guicey listeners execution time.
     */
    ListenersTime(true),

    /**
     * Guicey time in dropwizard run phase (without jersey time). Part of {@link #GuiceyTime}.
     */
    RunTime(true),
    /**
     * Time of configuration object parsing (to bind later by value). Part of {@link #RunTime}.
     */
    ConfigurationAnalysis(true),
    /**
     * Time of guicey bundles run execution. Part of {@link #BundleTime} and {@link #RunTime}.
     */
    GuiceyBundleRunTime(true),
    /**
     * Modules pre processing time (include Aware* interfaces processing and bindings analysis).
     * Also includes part of {@link #ExtensionsRecognitionTime}.
     * Part of {@link #RunTime}.
     */
    ModulesProcessingTime(true),
    /**
     * Count of elements found in user modules (note that element is wider then binding and include listeners and
     * other configurations). Not all these elements were analyzed because only pure class
     * bindings are checked.
     */
    BindingsCount(false),
    /**
     * Count of bindings from registered modules which were analyzed for extensions. As analysis may be switched off by
     * {@link ru.vyarus.dropwizard.guice.GuiceyOptions#AnalyzeGuiceModules}, this counter indicates if
     * analysis was performed or not.
     */
    AnalyzedBindingsCount(false),
    /**
     * Removed configuration elements count. It may be disabled extension or entire inner module (all binding,
     * related to this module are removed).
     */
    RemovedBindingsCount(false),
    /**
     * Count of removed inner guice modules. Usual disable module may declare any inner guice module and all bindings
     * related to this module will be removed. Counter shows only how many disabled module types were affected.
     */
    RemovedInnerModules(false),
    /**
     * Guice SPI time of modules elements resolution (part of {@link #ModulesProcessingTime}). When bindings
     * inspection is disabled with {@link ru.vyarus.dropwizard.guice.GuiceyOptions#AnalyzeGuiceModules}, this stat
     * will be 0, but injector creation time will increase accordingly because guice will have to do the same
     * ({@link #InjectorCreationTime}).
     */
    BindingsResolutionTime(true),

    /**
     * Time of parsed bindings analysis. Part of {@link #ExtensionsRecognitionTime}.
     */
    BindingsAnalysisTime(true),
    /**
     * Guice injector creation time. Part of {@link #RunTime}.
     */
    InjectorCreationTime(true),
    /**
     * Time spent installing extensions with registered installers.
     * Part of {@link #RunTime}.
     */
    ExtensionsInstallationTime(true),

    /**
     * Guicey initialization time inside jersey context. Jersey is started only when server command used
     * (after guice context startup and so out of scope of guice bundle execution).
     * Part of {@link #GuiceyTime}.
     */
    JerseyTime(true),
    /**
     * Time spent by {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} to install
     * jersey related features. Part of {@link #JerseyTime}.
     */
    JerseyInstallerTime(true);

    private boolean timer;

    Stat(final boolean timer) {
        this.timer = timer;
    }

    /**
     * @return true is timer stat, false otherwise
     */
    public boolean isTimer() {
        return timer;
    }

    /**
     * @throws IllegalStateException is current stat is not timer
     */
    public void requiresTimer() {
        Preconditions.checkState(isTimer(), "%s is not timer stat", name());
    }

    /**
     * @throws IllegalStateException is current stat is not counter stat
     */
    public void requiresCounter() {
        Preconditions.checkState(!isTimer(), "%s is not counter stat", name());
    }
}
