package ru.vyarus.dropwizard.guice.cases.multicases

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.cases.multicases.support.CustomInstaller
import ru.vyarus.dropwizard.guice.cases.multicases.support.Feature
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.InstallerItemInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingletonInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsHolder
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject


/**
 * @author Vyacheslav Rusakov
 * @since 01.08.2016
 */
@UseGuiceyApp(App)
class RepeatedRegistrationTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    ExtensionsHolder holder

    def "Check duplicate manual registrations"() {

        expect: "extension registered once"
        ExtensionItemInfo item = info.getData().getInfo(Feature.class)
        item.registeredBy == [Application] as Set
        item.registrationAttempts == 2
        info.getExtensionsOrdered(EagerSingletonInstaller).size() == 1

        and: "installer registered once"
        InstallerItemInfo inst = info.getData().getInfo(CustomInstaller.class)
        inst.registeredBy == [Application] as Set
        inst.registrationAttempts == 2
        holder.getInstallers().findAll { it.class == CustomInstaller }.size() == 1

    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(Feature, Feature)
                    .installers(EagerSingletonInstaller, CustomInstaller, CustomInstaller)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}