package ru.vyarus.dropwizard.guice.cases.multicases

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.cases.multicases.support.Feature
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingletonInstaller
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject


/**
 * @author Vyacheslav Rusakov
 * @since 01.08.2016
 */
@UseGuiceyApp(App)
class MultiExtensionRegistrationTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check duplicate extension registration"() {

        expect:
        ExtensionItemInfo item = info.getData().getInfo(Feature.class)
        item.registeredBy == [Application, ClasspathScanner] as Set
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