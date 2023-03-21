package ru.vyarus.dropwizard.guice.config.option

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller
import ru.vyarus.dropwizard.guice.module.installer.option.InstallerOptionsSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.DenyServletRegistrationWithClash

/**
 * @author Vyacheslav Rusakov
 * @since 20.08.2016
 */
@TestGuiceyApp(InstOptApp)
class InstallerOptionsTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check installer options access"() {

        expect: "installer access options"
        InstallerWithOption.called
        info.options.isUsed(DenyServletRegistrationWithClash)
        info.options.isSet(DenyServletRegistrationWithClash)
    }

    static class InstOptApp extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .installers(InstallerWithOption)
            // does not matter what to install
                    .extensions(InstOptApp)
                    .option(DenyServletRegistrationWithClash, true)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    static class InstallerWithOption extends InstallerOptionsSupport implements FeatureInstaller {
        static boolean called

        @Override
        boolean matches(Class type) {
            assert option(DenyServletRegistrationWithClash) == true
            called = true
            return true
        }

        @Override
        void report() {

        }
    }
}