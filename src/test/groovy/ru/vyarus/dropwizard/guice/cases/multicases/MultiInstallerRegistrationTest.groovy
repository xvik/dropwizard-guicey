package ru.vyarus.dropwizard.guice.cases.multicases

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.cases.multicases.support.CustomInstaller
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.InstallerItemInfo
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
class MultiInstallerRegistrationTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    ExtensionsHolder holder;

    def "Check duplicate installer registration"() {

        expect:
        InstallerItemInfo item = info.getData().getInfo(CustomInstaller.class)
        item.registeredBy == [Application, ClasspathScanner] as Set
        item.registrationAttempts == 2
        holder.getInstallers().findAll { it.class == CustomInstaller }.size() == 1

    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
            // installer will be registered by both autoscan and manually
                    .enableAutoConfig(CustomInstaller.package.name)
                    .installers(CustomInstaller)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}