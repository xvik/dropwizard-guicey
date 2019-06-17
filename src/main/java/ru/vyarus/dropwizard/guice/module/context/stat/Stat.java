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
     * Overall guicey startup time (including hk part). All other timers represents this timer detalization.
     */
    GuiceyTime(true),
    /**
     * Commands processing time. Includes environment commands members injection (always performed)
     * and commands registration from classpath scan (disabled by default).
     */
    CommandTime(true),
    /**
     * Classpath scan time (time to resolve all classes from configured packages).
     */
    ScanTime(true),
    /**
     * Count of classes loaded during classpath scan.
     */
    ScanClassesCount(false),
    /**
     * Bundles resolution, creation and starting time (combined from both configuration and run phases).
     */
    BundleTime(true),
    /**
     * Bundles resolution time only (lookup mechanism).
     * Part of {@link #BundleTime}.
     */
    BundleResolutionTime(true),
    /**
     * Installers resolution and instantiation time.
     */
    InstallersTime(true),
    /**
     * Time spent on extensions resolution (matching all extension classes with configured installers ).
     * Does not contain classpath scan time, because already use cached scan result (actual scan performed
     * before initializations).
     */
    ExtensionsRecognitionTime(true),
    /**
     * Guice injector creation time.
     */
    InjectorCreationTime(true),
    /**
     * Time spent installing extensions with registered installers.
     * Part of {@link #InjectorCreationTime}.
     */
    ExtensionsInstallationTime(true),
    /**
     * Guicey initialization time inside HK context. HK is started only when server command used
     * (after guice context startup and so out of scope of guice bundle execution).
     * Part of {@link #GuiceyTime}.
     */
    HKTime(true),
    /**
     * Time spent by {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} to install
     * jersey related features. Part of {@link #HKTime}.
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
