package ru.vyarus.guicey.gsp.classloader

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle
import ru.vyarus.guicey.gsp.app.asset.AssetSources
import ru.vyarus.guicey.gsp.app.ext.DelayedConfigurationCallback
import ru.vyarus.guicey.gsp.app.rest.mapping.ViewRestSources

import java.nio.file.Paths

/**
 * @author Vyacheslav Rusakov
 * @since 13.04.2020
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class CustomClasspathInExtensionCallbackTest extends AbstractTest {

    def "Check app resources access"() {

        when: "accessing external asset"
        String res = getHtml("/app/ext.ftl")
        then: "resource found"
        res.contains("external template")

        when: "accessing direct resource (through servlet)"
        res = getHtml("/app/other.css")
        then: "resource found"
        res.contains("other css")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(ServerPagesBundle.builder()
                            .enableFreemarkerCustomClassLoadersSupport()
                            .build(),
                            // app resources from classpath
                            ServerPagesBundle.app("app", "app", "/app")
                                    .indexPage("index.html")
                                    .build(),
                            // external assets
                            ServerPagesBundle.extendApp('app')
                                    .attachAssets("extra")
                                    .delayedConfiguration(new DelayedConfigurationCallback() {
                                        @Override
                                        void configure(GuiceyEnvironment environment, AssetSources assets, ViewRestSources views) {
                                            assets.attach("extra", new URLClassLoader([Paths.get("src/test/external").toUri().toURL()] as URL[]))
                                        }
                                    })
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
