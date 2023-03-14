package ru.vyarus.dropwizard.guice.cases.multicases

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.cases.multicases.support.CustomInstaller
import ru.vyarus.dropwizard.guice.cases.multicases.support.Feature
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.InstallerItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingletonInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsHolder
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 01.08.2016
 */
@TestGuiceyApp(App)
class RepeatedRegistrationTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    ExtensionsHolder holder

    def "Check duplicate manual registrations"() {

        expect: "extension registered once"
        ExtensionItemInfo item = info.getInfo(Feature.class)
        item.registeredBy == [ItemId.from(Application)] as Set
        item.registrationAttempts == 2
        info.getExtensionsOrdered(EagerSingletonInstaller).size() == 1

        and: "installer registered once"
        InstallerItemInfo inst = info.getInfo(CustomInstaller.class)
        inst.registeredBy == [ItemId.from(Application)] as Set
        inst.registrationAttempts == 2
        holder.getInstallers().findAll { it.class == CustomInstaller }.size() == 1

    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .noDefaultInstallers()
                    .extensions(Feature, Feature)
                    .installers(EagerSingletonInstaller, CustomInstaller, CustomInstaller)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}