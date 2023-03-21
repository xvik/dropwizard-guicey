package ru.vyarus.dropwizard.guice.support

import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.util.BindModule
import ru.vyarus.dropwizard.guice.test.InjectionTest

/**
 * Example of automatic configuration: installers, beans and commands searched automatically.
 * @author Vyacheslav Rusakov 
 * @since 01.09.2014
 */
class AutoScanApplication extends Application<TestConfiguration> {

    public static void main(String[] args) {
        new AutoScanApplication().run(args)
    }

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig("ru.vyarus.dropwizard.guice.support.feature")
                .searchCommands()
                .modules(new BindModule(InjectionTest.TestBean, InjectionTest.TestSingletonBean))
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
