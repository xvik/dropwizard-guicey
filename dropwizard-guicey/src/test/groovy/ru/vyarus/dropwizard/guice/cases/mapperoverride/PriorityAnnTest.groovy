package ru.vyarus.dropwizard.guice.cases.mapperoverride

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.glassfish.jersey.internal.inject.InjectionManager
import org.glassfish.jersey.internal.inject.Providers
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.InstallersOptions
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import javax.annotation.Priority
import javax.inject.Inject
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 04.10.2020
 */
@TestDropwizardApp(App)
class PriorityAnnTest extends AbstractTest {

    @Inject
    InjectionManager manager

    def "Check priority sorting"() {

        when: "Lookup mappers"
        def provs = Providers.getAllServiceHolders(manager, ExceptionMapper.class).findAll {
            it.contractTypes.find { it instanceof Class && (it as Class).package.name == 'ru.vyarus.dropwizard.guice.cases.mapperoverride' }
        }
        then: "custom provider last"
        provs.size() == 3
        // NOTE jersey sort non custom providers descending and custom ascending!!!
        // this is not right! But checking rank to make sure its set correctly
        provs[0].contractTypes.contains(Mapper3)
        provs[0].rank == 3
        provs.last().contractTypes.contains(Mapper2)
        provs.last().rank == 1
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(Mapper1, Mapper3, Mapper2)
            // prioritization disabled because @Custom providers are sorted directly by Provider annotation,
            // whereas default once require setting rank (with true it would test nothing)
                    .option(InstallersOptions.PrioritizeJerseyExtensions, false)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    @Provider
    @Priority(2)
    static class Mapper1 implements ExceptionMapper<IOException> {
        @Override
        Response toResponse(IOException exception) {
            return null
        }
    }

    @Provider
    @Priority(1)
    static class Mapper2 implements ExceptionMapper<IOException> {
        @Override
        Response toResponse(IOException exception) {
            return null
        }
    }

    @Provider
    @Priority(3)
    static class Mapper3 implements ExceptionMapper<IOException> {
        @Override
        Response toResponse(IOException exception) {
            return null
        }
    }
}
