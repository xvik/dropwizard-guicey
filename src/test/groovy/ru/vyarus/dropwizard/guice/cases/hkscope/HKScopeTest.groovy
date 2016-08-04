package ru.vyarus.dropwizard.guice.cases.hkscope

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.glassfish.hk2.api.ServiceLocator
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.jersey.debug.service.ContextDebugService
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

import javax.inject.Inject
import javax.inject.Provider
import javax.ws.rs.ext.Providers

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@UseDropwizardApp(ScopeApplication)
class HKScopeTest extends AbstractTest {

    @Inject
    ContextDebugService debugService
    @Inject
    Provider<ServiceLocator> locator

    def "Check jersey extensions registration"() {

        setup: "need to request hk resource to force instantiation"
        new URL("http://localhost:8080/hk/foo").getText()

        and: "force jersey to load custom HKContextResolver"
        Providers providers = locator.get().getService(Providers.class)
        providers.getContextResolver(null, null)

        expect: "app launched successfully"
        debugService.guiceManaged.size() == debugService.hkManaged.size()
    }

    static class ScopeApplication extends Application<TestConfiguration> {

        @Override
        void initialize(Bootstrap<TestConfiguration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .enableAutoConfig("ru.vyarus.dropwizard.guice.cases.hkscope.support")
                    .build())
        }

        @Override
        void run(TestConfiguration configuration, Environment environment) throws Exception {
        }
    }
}