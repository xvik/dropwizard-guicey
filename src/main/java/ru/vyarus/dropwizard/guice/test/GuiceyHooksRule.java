package ru.vyarus.dropwizard.guice.test;

import org.junit.rules.ExternalResource;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport;
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyHooks;

import java.util.Arrays;
import java.util.List;

/**
 * Junit rule for changing application configuration (remove some components or register test specific (e.g. mocks)).
 * Supposed to be used in conjunction with {@link io.dropwizard.testing.junit.DropwizardAppRule} or
 * {@link GuiceyAppRule}. Must be used ONLY with {@link org.junit.rules.RuleChain} because normally rules order
 * is not predictable:
 * <pre>{@code static GuiceyAppRule RULE = new GuiceyAppRule(App.class, null);
 *    {@literal @}ClassRule
 *    public static RuleChain chain = RuleChain
 *            .outerRule(new GuiceyHooksRule((builder) -> builder.modules(...)))
 *            .around(RULE);
 * }</pre>
 * To declare common extensions for all tests, declare common rule in test class (without {@code @ClassRule}
 * annotation!) and use it in chain:
 * <pre>{@code public class BaseTest {
 *         static GuiceyHooksRule BASE = new GuiceyHooksRule((builder) -> builder.modules(...))
 *     }
 *
 *     public class SomeTest extends BaseTest {
 *         static GuiceyAppRule RULE = new GuiceyAppRule(App.class, null);
 *         {@literal @}ClassRule
 *         public static RuleChain chain = RuleChain
 *            .outerRule(BASE)
 *            .around(new GuiceyHooksRule((builder) -> builder.modules(...)) // optional test-specific staff
 *            .around(RULE);
 *     }
 * }</pre>
 * <p>
 * IMPORTANT: rule will not work with spock extensions (because of lifecycle specifics)! Use
 * {@link UseGuiceyHooks} or new {@code hooks} attribute in
 * {@link ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp} or
 * {@link ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp} instead.
 * <p>
 * Rule is thread safe: it is assumed that rule will be applied at the same thread as test application initialization.
 *
 * @author Vyacheslav Rusakov
 * @since 11.04.2018
 */
public class GuiceyHooksRule extends ExternalResource {

    private final List<GuiceyConfigurationHook> hooks;

    public GuiceyHooksRule(final GuiceyConfigurationHook... hooks) {
        this.hooks = Arrays.asList(hooks);
    }

    @Override
    protected void before() throws Throwable {
        hooks.forEach(GuiceyConfigurationHook::register);
    }

    @Override
    protected void after() {
        // normally reset is not required, but called to avoid possible state for some failed cases
        ConfigurationHooksSupport.reset();
    }
}
