package ru.vyarus.dropwizard.guice.config.unique

import com.google.inject.Inject
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.config.unique.support.ParentLastURLClassLoader
import ru.vyarus.dropwizard.guice.config.unique.support.SampleExt
import ru.vyarus.dropwizard.guice.config.unique.support.SampleModule
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 09.09.2019
 */
@TestGuiceyApp(App)
class DifferentClassLoaderRecognitionTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    static {
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    def "Check classes from different loaders recognition"() {

        expect: "extensions deduplicated"
        info.getExtensions().size() == 1

        and: "only one module registered"
        info.getModules().size() == 2 // 1 + guice bootstrap module
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            def classpath = [DifferentClassLoaderRecognitionTest.class.getResource("/")]
            ClassLoader cl = new ParentLastURLClassLoader(classpath);

            def ext1 = SampleExt
            def ext2 = cl.loadClass(SampleExt.name)
            assert ext1.classLoader != ext2.classLoader

            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(ext1, ext2)
                    .modules(new SampleModule(), cl.loadClass(SampleModule.name).newInstance())
                    .printDiagnosticInfo()
                    .build()
            );
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
