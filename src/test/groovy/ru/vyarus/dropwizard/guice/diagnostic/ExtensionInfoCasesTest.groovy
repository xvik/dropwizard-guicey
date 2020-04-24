package ru.vyarus.dropwizard.guice.diagnostic

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject
import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 11.07.2016
 */
@UseGuiceyApp(App)
class ExtensionInfoCasesTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check extension info"() {

        expect: "extension info for hk2 managed"
        ExtensionItemInfo hk = info.getInfo(HkResource)
        !hk.lazy
        hk.jerseyManaged

        and: "extension info for lazy"
        ExtensionItemInfo lazy = info.getInfo(LazyResource)
        lazy.lazy
        !lazy.jerseyManaged

    }

    @Path("/")
    @JerseyManaged
    static class HkResource {}

    @Path("/foo")
    @LazyBinding
    static class LazyResource {}

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .noDefaultInstallers()
                    .installers(ResourceInstaller)
                    .extensions(HkResource, LazyResource)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}