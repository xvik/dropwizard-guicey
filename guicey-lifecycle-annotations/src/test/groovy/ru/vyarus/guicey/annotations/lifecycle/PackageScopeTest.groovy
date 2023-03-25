package ru.vyarus.guicey.annotations.lifecycle

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.guicey.annotations.lifecycle.support.SampleBean
import ru.vyarus.guicey.annotations.lifecycle.support.sub.AnotherBean
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 27.11.2018
 */
@TestGuiceyApp(App)
class PackageScopeTest extends Specification {

    @Inject
    SampleBean bean
    @Inject
    AnotherBean anotherBean

    def "Check package scope"() {

        expect: "only bean in target package called"
        !bean.called
        anotherBean.called
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap
                    .addBundle(GuiceBundle.builder()
                            .bundles(new LifecycleAnnotationsBundle(AnotherBean.class.package.name))
                            .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

}
