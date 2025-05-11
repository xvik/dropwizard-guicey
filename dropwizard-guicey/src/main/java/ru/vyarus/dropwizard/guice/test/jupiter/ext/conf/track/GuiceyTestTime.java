package ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.track;

/**
 * Guicey timers for test extensions performance measurement.
 * <p>
 * Note that actions may be performed under different test phases (e.g., application could start in before all
 * for all test methods, but could be in before each for per-method instance tests).
 *
 * @author Vyacheslav Rusakov
 * @since 04.02.2025
 */
public enum GuiceyTestTime {
    /**
     * Before all time.
     */
    BeforeAll("Before all"),
    /**
     * Before each time.
     */
    BeforeEach("Before each"),
    /**
     * After each time.
     */
    AfterEach("After each"),
    /**
     * After all time.
     */
    AfterAll("After all"),

    /**
     * Guice injectMemebers() executed for test instance.
     */
    GuiceInjection("Guice fields injection"),
    /**
     * Hook and setup fields searched to verify correctness (different from fields analysis below).
     */
    ReusableAppWarnings("Check reusable app warnings"),
    /**
     * Guicey hook and setup fields found and resolved (reflection).
     */
    GuiceyFieldsSearch("Guicey fields search"),
    /**
     * Registration of hooks resolved from fields and declared in extension (application hooks registration
     * not tracked).
     */
    HooksRegistration("Guicey hooks registration"),
    /**
     * Setup objects executed.
     */
    SetupObjectsExecution("Guicey setup objects execution"),
    /**
     * Creation of the dropwizard or guicey test support object.
     */
    DropwizardTestSupport("DropwizardTestSupport creation"),
    /**
     * Test support object before() executed (application start plus configuration overrides).
     */
    SupportStart("Application start"),
    /**
     * Test support after() executed (application stopped).
     */
    SupportStop("Application stop"),
    /**
     * Test listeners time (registered in setup objects).
     */
    TestListeners("Listeners execution");

    private final String name;

    GuiceyTestTime(final String name) {
        this.name = name;
    }

    /**
     * @return display name
     */
    public String getDisplayName() {
        return name;
    }
}
