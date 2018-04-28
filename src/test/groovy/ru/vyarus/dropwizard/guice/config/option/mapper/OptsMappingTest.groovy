package ru.vyarus.dropwizard.guice.config.option.mapper

import com.google.inject.Stage
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.contrib.java.lang.system.RestoreSystemProperties
import org.junit.contrib.java.lang.system.SystemOutRule
import org.junit.runners.model.Statement
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.GuiceyOptions
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.option.mapper.OptionsMapper
import ru.vyarus.dropwizard.guice.module.installer.InstallersOptions
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule

/**
 * @author Vyacheslav Rusakov
 * @since 28.04.2018
 */
class OptsMappingTest extends AbstractTest {

    @Rule
    EnvironmentVariables ENV = new EnvironmentVariables()
    @Rule
    SystemOutRule out = new SystemOutRule().enableLog();
    @Rule
    RestoreSystemProperties propsReset = new RestoreSystemProperties();

    def "Check options mapping"() {
        setup:
        ENV.set("STAGE", Stage.DEVELOPMENT.name())
        System.setProperty("strictListeners", "true")
        def rule = new GuiceyAppRule(App, null)

        when: "staring app"
        GuiceyConfigurationInfo info
        rule.apply({ info = rule.getBean(GuiceyConfigurationInfo) } as Statement, null).evaluate()
        then: "options applied"
        info.getOptions().getValue(InstallersOptions.DenySessionListenersWithoutSession) == true
        info.getOptions().getValue(GuiceyOptions.InjectorStage) == Stage.DEVELOPMENT
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .options(new OptionsMapper()
                    .prop("strictListeners", InstallersOptions.DenySessionListenersWithoutSession)
                    .env("STAGE", GuiceyOptions.InjectorStage)
                    .map())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
