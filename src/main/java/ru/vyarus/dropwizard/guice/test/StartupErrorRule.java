package ru.vyarus.dropwizard.guice.test;

import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Dropwizard exit with code 1 in case of exception during command run. In order to test error situations
 * system exit must be prevented.
 * Rule use <a href="http://stefanbirkner.github.io/system-rules/index.html">system rules</a> to intercept
 * exit and output stream (system.err, system.out).
 *
 * @author Vyacheslav Rusakov
 * @since 16.03.2017
 */
public final class StartupErrorRule implements TestRule {
    private final ExpectedSystemExit exit = ExpectedSystemExit.none();
    private final SystemErrRule systemErr = new SystemErrRule();
    private final SystemOutRule systemOut = new SystemOutRule();

    private StartupErrorRule() {
    }

    /**
     * To activate rule use {@link #arm()} method.
     *
     * @return not activated rule
     */
    public static StartupErrorRule pending() {
        return new StartupErrorRule();
    }

    /**
     * @return activated rule
     */
    public static StartupErrorRule armed() {
        return pending().arm();
    }

    /**
     * Use when rule was initially created with {@link #pending()}.
     * Do nothing if called on already armed rule.
     *
     * @return activates rule
     */
    public StartupErrorRule arm() {
        exit.expectSystemExitWithStatus(1);
        systemErr.enableLog();
        systemOut.enableLog();
        return this;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {

        // sys.out -> sys.err -> system exit
        return systemOut.apply(
                systemErr.apply(
                        exit.apply(base, description),
                        description),
                description);
    }

    /**
     * @return content of system.out
     */
    public String getOutput() {
        return clearString(systemOut.getLog());
    }

    /**
     * Dropwizard exception will be presented here.
     *
     * @return content if system.err
     */
    public String getError() {
        return clearString(systemErr.getLog());
    }

    private String clearString(final String message) {
        return message.trim().replaceAll("\r", "");
    }
}
