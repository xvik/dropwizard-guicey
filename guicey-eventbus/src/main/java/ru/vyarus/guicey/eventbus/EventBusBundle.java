package ru.vyarus.guicey.eventbus;

import com.google.common.eventbus.EventBus;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueGuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.guicey.eventbus.module.EventBusModule;
import ru.vyarus.guicey.eventbus.module.TypeLiteralAdapterMatcher;
import ru.vyarus.guicey.eventbus.report.EventSubscribersReporter;
import ru.vyarus.guicey.eventbus.service.EventSubscribersInfo;

/**
 * Binds support for single (!) event bus. {@link EventBus} available for injection (to publish events).
 * All guice beans with methods annotated with {@link com.google.common.eventbus.Subscribe} are
 * automatically registered. All listeners subscribed before startup are reported to logs (may be disabled).
 * <p>
 * If you want to customize default event bus, configure instance manually and provide instance in constructor:
 * <pre><code>
 *     new EventBusBundle(myCustomBus)
 * </code></pre>
 * <p>
 * You can reduce amount of classes checked for listener methods by providing custom types matcher. For example,
 * <pre><code>
 *     new EventBusBundle()
 *          .withMatcher(Matchers.inSubpackage("some.package"))
 * </code></pre>
 * <p>
 * Reflection is used for registered listeners printing (no way otherwise to get registered subscribers).
 * If there will be any problems with it, simply disable reporting.
 * <p>
 * Only one bundle instance will be actually used (in case of multiple registrations).
 *
 * @author Vyacheslav Rusakov
 * @see <a href="https://github.com/google/guava/wiki/EventBusExplained">eventbus documentation</a>
 * @see EventSubscribersInfo for subscribers info access
 * @since 12.10.2016
 */
public class EventBusBundle extends UniqueGuiceyBundle {

    private final EventBus eventbus;
    private Matcher<? super TypeLiteral<?>> typeMatcher = Matchers.any();
    private boolean report = true;

    /**
     * Register default event bus. Events processing is synchronous.
     */
    public EventBusBundle() {
        this(new EventBus("bus"));
    }

    /**
     * Registers custom event bus. Use this constructor to customize event bus or to switch to
     * {@link com.google.common.eventbus.AsyncEventBus}.
     *
     * @param eventbus event bus instance
     */
    public EventBusBundle(final EventBus eventbus) {
        this.eventbus = eventbus;
    }

    /**
     * By default, all registered bean types are checked for listener methods.
     * Listener check involves all methods in class and subclasses lookup.
     * If you have too much beans which are not using eventbus, then it makes sense to reduce checked beans scope
     * For example, check only beans in some package: {@code Matchers.inSubpackage("some.pacjage")}.
     * <p>
     * The most restrictive (and faster) approach would be to introduce your annotation (e.g. {@code @EventListener})
     * and search for listeners only inside annotated classes ({@code Matchers.annotatedWith(EventListener.class)}.
     *
     * @param classMatcher class matcher to reduce classes checked for listener methods
     * @return bundle instance for chained calls
     */
    public EventBusBundle withMatcher(final Matcher<? super Class<?>> classMatcher) {
        this.typeMatcher = new TypeLiteralAdapterMatcher(classMatcher);
        return this;
    }

    /**
     * If you have a lot of listeners or events or simply don't want console reporting use this method.
     * <p>
     * Disabling reporting will also disable reflective access to eventbus internals, so disable it if you have
     * problems (for example, new guava version renamed field).
     *
     * @return bundle instance for chained calls
     */
    public EventBusBundle noReport() {
        report = false;
        return this;
    }

    @Override
    public void run(final GuiceyEnvironment environment) {
        environment.modules(new EventBusModule(eventbus, typeMatcher));

        if (report) {
            // report after application startup to count events, resolved from JIT-created services (not declared)
            environment.onApplicationStartup(injector -> {
                new EventSubscribersReporter(
                        injector.getInstance(EventSubscribersInfo.class))
                        .report();
            });
        }
    }
}
