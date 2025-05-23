package ru.vyarus.dropwizard.guice.test.jupiter.ext.spy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

/**
 * Replace any guice service with mockito spy in test. The difference with mock: spy wraps around the real service(!)
 * and could be used to validate called service methods (verify incoming parameters and output value).
 * <p>
 * Important: requires mockito dependency!
 * <p>
 * In contrast to mocks and stubs, spies work with guice AOP: all calls to service are intercepted and
 * passed through the spy object (as "proxy"). That also means that all aop, applied to the original bean, would
 * work (in contrast to mocks).
 * <p>
 * As spy requires real bean instance - spy object is created just after injector creation (and AOP interceptor
 * redirects into it (then real bean called)). Spy object, injected to a field would not be the same instance as
 * injected bean ({@code @Inject SpiedService}) because injected bean would be a proxy, handling guice AOP.
 * <p>
 * Calling bean methods directly on spy is completely normal (guice bean just redirects calls to spy object)!
 * <p>
 * Example: {@code @SpyBean Service spy}. May be used for static and instance fields.
 * <p>
 * Spy field CAN'T be initialized manually. Use {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean}
 * or {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean}
 * instead for manual spy objects initialization. Such manual initialization could be required for spies created
 * from abstract classes (or multiple interfaces): in this case actual bean instance is not required, and so mocks
 * support could be used instead: {@code @MockBean AbstractService spy = Mockito.spy(AbstractService.class)}.
 * <p>
 * Spy stubs could be configured in test beforeEach method: {@code Mockito.roReturn("ok").when(spy).something()}.
 * <p>
 * Spies reset called before and after each test method. Could be disabled with {@link #autoReset()}
 * <p>
 * Mockito provide the detailed report of used mock methods and redundant stub definitions. Use {@link #printSummary()}
 * to enable this report (printed after each test method).
 * <p>
 * Guicey extension debug ({@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp#debug()}) enables
 * spy fields debug: all recognized annotated fields would be printed to console.
 * <p>
 * If spies assumed to be used only to validate bean in/out, then
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean} might be used instead: it is simply collects
 * called methods with argument and results, plug measure performance (spies and trackers could be used together).
 *
 * @author Vyacheslav Rusakov
 * @since 10.02.2025
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SpyBean {

    /**
     * Note: spy could be reset manually with {@link org.mockito.Mockito#reset(Object[])}.
     *
     * @return true to reset spy after each test method
     */
    boolean autoReset() default true;

    /**
     * Native mockito spy usage report: shows called methods and stubbed, but not used methods.
     *
     * @return true to print spy summary after each test
     */
    boolean printSummary() default false;

    /**
     * In most cases, spy object could be configured after application startup (using test setUp method). But
     * if target spy is used during application startup, this initializer could be used to configure spy object on
     * first access and so application startup logic (like managed objects) would use already configured spy during
     * startup.
     * <p>
     * Consumer accepts already created spy instance.
     *
     * @return optional spy object initializer
     */
    Class<? extends Consumer<?>>[] initializers() default {};
}
