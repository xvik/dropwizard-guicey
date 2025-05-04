package ru.vyarus.dropwizard.guice.test.jupiter.ext.track;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * Tracks method calls on any guice bean and records arguments and return values, together with measuring time.
 * <p>
 * Useful for validating called method arguments and return value (when service called indirectly - by another top-level
 * service). In this sense it is very close to mockito spy
 * ({@link ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean}), but api is simpler.
 * Tracker collects both raw in/out objects and string (snapshot) version (because mutable objects could change).
 * Raw objects holding could be disabled with {@link #keepRawObjects()}.
 * <p>
 * Another use-case is slow methods detection: tracker counts each method execution time, and after test could
 * print a report indicating the slowest methods. Or it could be used to simply print all called methods to console
 * with {@link #trace()} (could be useful during behavior investigations). Another option is to configure
 * a slow method threshold: then only methods above a threshold would be logged with WARN.
 * <p>
 * Example usage: {@code @TrackBean Tracker<Service> tracker}. All calls to {@code Service} bean would be tracked.
 * The field might be static.
 * <p>
 * Manual field initialization is not allowed.
 * <p>
 * Tracking is implemented with a custom AOP handler which intercepts all bean calls and record them.
 * <p>
 * Can be used together with mocks, spies or stubs ({@link ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean},
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean},
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean}).
 * <p>
 * Guicey extension debug ({@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp#debug()}) enables
 * tracker fields debug: all recognized annotated fields would be printed to console. Also, after each test method
 * it would print performance stats for called methods in all registered trackers.
 * <p>
 * Individual tracker report could be enabled with {@link #printSummary()} - will print called methods stats
 * for exact tracker (independent of guicey extension debug option).
 * <p>
 * By default, tracker re-set after each test method. Use {@link #autoReset()} to collect tracking data for the entire
 * test.
 * <p>
 * Limitation: could track only beans, created by guice (due to used AOP). Does not work for HK2 beans.
 *
 * @author Vyacheslav Rusakov
 * @since 11.02.2025
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TrackBean {

    /**
     * When enabled, prints called method just after it's execution (with called arguments and returned result).
     * Not enabled by default to avoid output mess in case when many methods would be called during test.
     *
     * @return true to print each method execution
     */
    boolean trace() default false;

    /**
     * Print warnings about methods executing longer than the specified threshold. Set to 0 to disable warnings.
     *
     * @return slow method threshold (in seconds, by default - see {@link #slowMethodsUnit()})
     */
    long slowMethods() default 5;

    /**
     * Unit for {@link #slowMethods()} threshold value (seconds by default).
     *
     * @return unit for threshold value
     */
    ChronoUnit slowMethodsUnit() default ChronoUnit.SECONDS;

    /**
     * It is more likely that trackers would be used mostly for "call and verify" scenarios where keeping raw
     * arguments makes perfect sense. That's why it's enabled by default.
     * <p>
     * Important: method arguments and the result objects state could be mutable and change after or during method
     * execution (and so be irrelevant for tracks analysis). For such cases, the tracker always holds string
     * representations of method arguments and the result (rendered in method execution time).
     * <p>
     * It makes sense to disable option if too many method executions appear during the test (e.g., tracker used
     * for performance metrics).
     *
     * @return true to keep raw arguments and result objects
     */
    boolean keepRawObjects() default true;

    /**
     * Required to keep called method toString rendering readable in case of large strings used.
     * Note that for non-string objects, an object type with identity hash would be shown (not rely on toString
     * because it would be too much unpredictable).
     *
     * @return maximum length of string in method parameter or returned result
     */
    int maxStringLength() default 30;

    /**
     * Note: tracker could be cleared manually with {@link ru.vyarus.dropwizard.guice.test.track.Tracker#clear()}.
     *
     * @return true to reset tracker (remove collected stats) after each test method
     */
    boolean autoReset() default true;

    /**
     * Note that the summary for all registered trackers is printed when the guicey extension debug option enabled.
     * This option exists to be able to print summary for a single tracker, independent of the debug option.
     * <p>
     * The report would be shown after each test method.
     * <p>
     * Note that such reports could be built at any time manually with {@code tracker.getStats().render()}
     * (or any custom report using {@code tracker.getStats().getMethods()}).
     *
     * @return true to print summary after test
     */
    boolean printSummary() default false;
}
