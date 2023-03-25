package ru.vyarus.dropwizard.guice.test.spock.ext;

import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;

import java.util.List;

/**
 * Apply hooks according to spock lifecycle.
 *
 * @author Vyacheslav Rusakov
 * @since 12.04.2018
 * @deprecated additional hooks may be declared in static test fields
 */
@Deprecated
public class GuiceyConfigurationHookInterceptor extends AbstractMethodInterceptor {

    private final List<GuiceyConfigurationHook> hooks;

    public GuiceyConfigurationHookInterceptor(final List<GuiceyConfigurationHook> hooks) {
        this.hooks = hooks;
    }

    @Override
    public void interceptSharedInitializerMethod(final IMethodInvocation invocation) throws Throwable {
        hooks.forEach(GuiceyConfigurationHook::register);
        invocation.proceed();
    }

    @Override
    public void interceptCleanupSpecMethod(final IMethodInvocation invocation) throws Throwable {
        // for case when base test is used without actual application start and so listeners will never be used
        // cleanup state after tests to not affect other tests
        ConfigurationHooksSupport.reset();
        invocation.proceed();
    }
}
