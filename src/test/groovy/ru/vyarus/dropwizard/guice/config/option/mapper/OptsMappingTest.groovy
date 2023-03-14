package ru.vyarus.dropwizard.guice.config.option.mapper

import com.google.inject.Stage
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.junit.jupiter.api.extension.ExtendWith
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.GuiceyOptions
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.option.mapper.OptionsMapper
import ru.vyarus.dropwizard.guice.module.installer.InstallersOptions
import ru.vyarus.dropwizard.guice.test.TestSupport
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import uk.org.webcompere.systemstubs.jupiter.SystemStub
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension
import uk.org.webcompere.systemstubs.properties.SystemProperties

/**
 * @author Vyacheslav Rusakov
 * @since 28.04.2018
 */
@ExtendWith(SystemStubsExtension)
class OptsMappingTest extends AbstractTest {

    @SystemStub
    EnvironmentVariables ENV
    @SystemStub
    SystemProperties propsReset

    def "Check options mapping"() {
        setup:
        ENV.set("STAGE", Stage.DEVELOPMENT.name())
        System.setProperty("strictListeners", "true")

        when: "starting app"
        GuiceyConfigurationInfo info = TestSupport.runCoreApp(App, null,
                { it.getInstance(GuiceyConfigurationInfo) })
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
