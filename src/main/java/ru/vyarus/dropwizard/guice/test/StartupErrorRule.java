package ru.vyarus.dropwizard.guice.test;

import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.contrib.java.lang.system.internal.CheckExitCalled;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Dropwizard exit with code 1 in case of exception during command run. In order to test error situations
 * system exit must be prevented.
 * Rule use <a href="http://stefanbirkner.github.io/system-rules/index.html">system rules</a> to intercept
 * exit and output streams (system.out, system.err).
 * <p>
 * Extra dependency 'com.github.stefanbirkner:system-rules:1.16.0' is required.
 * <p>
 * In spock tests, 'then' section must contain {@code rule.indicatorExceptionType} (or {@code thrown(CheckExitCalled)})
 * as rule can't intercept this exception.
 * 'then' section can be used for assertions after system exit and so no need to register custom callback in the rule.
 *
 * @author Vyacheslav Rusakov
 * @since 16.03.2017
 * @deprecated in favour of junit 5. But system-rules does not support junit 5 so there is no direct replacement.
 *  For system exit capturing you can use https://github.com/tginsberg/junit5-system-exit
 *  For system streams capturing there is no alternatives, but in any case it is impossible to do for parallel tests
 */
@Deprecated
public final class StartupErrorRule implements TestRule {
    private final ExpectedSystemExit exit = ExpectedSystemExit.none();
    private final SystemErrRule systemErr = new SystemErrRule();
    private final SystemOutRule systemOut = new SystemOutRule();

    private StartupErrorRule() {
        exit.expectSystemExitWithStatus(1);
        systemErr.enableLog();
        systemOut.enableLog();
    }

    /**
     * Use with spock tests or when no assertions required after system exit call.
     *
     * @return rule instance
     */
    public static StartupErrorRule create() {
        return new StartupErrorRule();
    }

    /**
     * This is useful for junit tests, because there is no other way to check anything after exit call.
     * In spock, 'then' section is always called and so may be used for assertions.
     *
     * @param check assertion callback to execute after exit call
     * @return activated rule
     */
    public static StartupErrorRule create(final AfterExitAssertion check) {
        return create().checkAfterExit(check);
    }

    /**
     * In junit it is impossible to use assertion lines after {@code System.exit()} call. This method will register
     * a custom check callback to validate state after system exit call.
     * May be called multiple times.
     * <p>
     * NOTE: in spock there is no need for it, because 'then' section will be called.
     *
     * @param check assertion command
     * @return rule instance for chained calls
     * @see ExpectedSystemExit#checkAssertionAfterwards(org.junit.contrib.java.lang.system.Assertion)
     */
    public StartupErrorRule checkAfterExit(final AfterExitAssertion check) {
        exit.checkAssertionAfterwards(() -> check.check(getOutput(), getError()));
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
     * NOTE: useful only for spock tests.
     *
     * @return content of system.out or empty string
     */
    public String getOutput() {
        return clearString(systemOut.getLog());
    }

    /**
     * Dropwizard exception will be presented here.
     * <p>
     * NOTE: useful only for spock tests.
     *
     * @return content of system.err or empty string
     */
    public String getError() {
        return clearString(systemErr.getLog());
    }

    /**
     * Useful only for spock tests because in junit rule can intercept this exception implicitly.
     *
     * @return type of exception thrown when exit called
     */
    public Class<? extends Exception> getIndicatorExceptionType() {
        return CheckExitCalled.class;
    }

    private String clearString(final String message) {
        return message.trim().replaceAll("\r", "");
    }

    /**
     * Interface implementation may be registered to check assertions after system exit.
     */
    @FunctionalInterface
    public interface AfterExitAssertion {

        /**
         * Called after system exit call to perform custom assertions.
         *
         * @param out output stream content or empty string
         * @param err error stream content (exception logged there)
         * @throws Exception in case of error
         */
        @SuppressWarnings("PMD.AvoidPrefixingMethodParameters")
        void check(String out, String err) throws Exception;
    }
}
