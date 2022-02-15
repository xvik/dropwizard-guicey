package ru.vyarus.dropwizard.guice.cases.norequestscope

import com.google.inject.ProvisionException
import com.google.inject.servlet.RequestScoped
import com.google.inject.servlet.RequestScoper
import com.google.inject.servlet.ServletScopes
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.util.BindModule
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import javax.inject.Inject
import javax.inject.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2020
 */
@TestDropwizardApp(App)
class NoRequestScopeTest extends AbstractTest {

    @Inject
    Provider<RScopedBean> bean;

    def "Check request scope workaround"() {

        when: "accessing request scope without request"
        bean.get()
        then: "fail"
        thrown(ProvisionException)

        when: "simulating scope"
        final RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
        RequestScoper.CloseableScope ignored = scope.open()
        def res = bean.get()
        ignored.close()
        then: "it works"
        res != null
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new BindModule(RScopedBean))
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @RequestScoped
    static class RScopedBean {

    }
}
