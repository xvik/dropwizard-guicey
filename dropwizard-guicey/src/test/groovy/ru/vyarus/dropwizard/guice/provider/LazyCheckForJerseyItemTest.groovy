package ru.vyarus.dropwizard.guice.provider

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.glassfish.jersey.internal.inject.InjectionManager
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged
import ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import jakarta.inject.Inject
import jakarta.ws.rs.ext.Provider
import java.util.function.Supplier

/**
 * @author Vyacheslav Rusakov
 * @since 12.09.2019
 */
@TestDropwizardApp(App)
class LazyCheckForJerseyItemTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    InjectionManager injectionManager

    def "Check item registration"() {

        when: "lookup extension info"
        ExtensionItemInfo item = info.getInfo(Fact)
        then: "jersey managed"
        item.isJerseyManaged()
        item.isLazy() // lazy remain in model because ignorance was installer decision

        and: "lookup instance ok"
        injectionManager.getInstance(Fact) != null

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(Fact)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @Provider
    @LazyBinding
    @JerseyManaged
    static class Fact implements Supplier<String> {
        @Override
        String get() {
            return "sample"
        }
    }
}
