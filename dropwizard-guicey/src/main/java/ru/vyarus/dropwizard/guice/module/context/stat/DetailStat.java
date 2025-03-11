package ru.vyarus.dropwizard.guice.module.context.stat;

/**
 * Startup time for exact item.
 *
 * @author Vyacheslav Rusakov
 * @since 10.03.2025
 */
public enum DetailStat {
    /**
     * Configuration hook run time.
     */
    Hook,
    /**
     * Command resolution (scan), instantiation and fields injection time.
     */
    Command,
    /**
     * Guicey bundle init time.
     */
    BundleInit,
    /**
     * Guicey bundle run time.
     */
    BundleRun
}
