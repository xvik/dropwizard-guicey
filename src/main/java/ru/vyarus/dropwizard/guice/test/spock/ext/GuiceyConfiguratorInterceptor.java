package ru.vyarus.dropwizard.guice.test.spock.ext;

import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import ru.vyarus.dropwizard.guice.configurator.ConfiguratorsSupport;
import ru.vyarus.dropwizard.guice.configurator.GuiceyConfigurator;

import java.util.List;

/**
 * Apply configurators according to spock lifecycle.
 *
 * @author Vyacheslav Rusakov
 * @since 12.04.2018
 */
public class GuiceyConfiguratorInterceptor extends AbstractMethodInterceptor {

    private final List<GuiceyConfigurator> configurators;

    public GuiceyConfiguratorInterceptor(final List<GuiceyConfigurator> configurators) {
        this.configurators = configurators;
    }

    @Override
    public void interceptSharedInitializerMethod(final IMethodInvocation invocation) throws Throwable {
        configurators.forEach(ConfiguratorsSupport::listen);
        invocation.proceed();
    }

    @Override
    public void interceptCleanupSpecMethod(final IMethodInvocation invocation) throws Throwable {
        // for case when base test is used without actual application start and so listeners will never be used
        // cleanup state after tests to nt affect other tests
        ConfiguratorsSupport.reset();
        invocation.proceed();
    }
}
