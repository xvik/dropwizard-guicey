package ru.vyarus.dropwizard.guice.module.context.info.sign;

/**
 * Auto scan sign indicates that configuration item could be resolved with classpath scan.
 *
 * @author Vyacheslav Rusakov
 * @since 06.07.2016
 */
public interface ScanSupport {

    /**
     * @return true if item found by classpath scan, false otherwise
     */
    boolean isFromScan();
}
