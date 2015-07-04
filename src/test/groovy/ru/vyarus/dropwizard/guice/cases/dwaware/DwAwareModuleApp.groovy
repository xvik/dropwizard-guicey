package ru.vyarus.dropwizard.guice.cases.dwaware

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.TestConfiguration

/**
 * @author Vyacheslav Rusakov 
 * @since 04.07.2015
 */
class DwAwareModuleApp extends Application<TestConfiguration> {

    @Override
    void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
                .modules(new DwAwareModule())
                .build()
        );
    }

    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
    }
}
