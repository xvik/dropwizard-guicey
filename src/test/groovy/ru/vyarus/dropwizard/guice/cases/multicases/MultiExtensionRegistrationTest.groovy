package ru.vyarus.dropwizard.guice.cases.multicases

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.cases.multicases.support.Feature
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingletonInstaller
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 01.08.2016
 */
@TestGuiceyApp(App)
class MultiExtensionRegistrationTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check duplicate extension registration"() {

        expect:
        ExtensionItemInfo item = info.getInfo(Feature.class)
        item.registeredBy == [ItemId.from(Application), ItemId.from(ClasspathScanner)] as Set
        item.registrationAttempts == 2
        info.getExtensionsOrdered(EagerSingletonInstaller).size() == 1

    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
            // feature will be registered by both autoscan and manually
                    .enableAutoConfig(Feature.package.name)
                    .extensions(Feature)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}