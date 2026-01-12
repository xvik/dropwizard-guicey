package ru.vyarus.dropwizard.guice.debug.renderer.guice

import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.matcher.Matchers
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceAopConfig
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceAopMapRenderer
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 10.09.2019
 */
@TestDropwizardApp(App)
class GuiceAopRendererForSpecialMethodsTest extends Specification {

    static {
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    @Inject
    Injector injector
    GuiceAopMapRenderer renderer

    void setup() {
        renderer = new GuiceAopMapRenderer(injector)
    }

    def "Check aop render"() {

        expect:
        render(new GuiceAopConfig())
                .replace('IndyInterface.java:344', 'IndyInterface.java:321') == """

    1 AOP handlers declared
    └── GuiceAopRendererForSpecialMethodsTest\$App\$1/GuiceAopRendererForSpecialMethodsTest\$App\$1\$1    at org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(IndyInterface.java:321)


    1 bindings affected by AOP
    │
    └── Service    (r.v.d.g.d.r.g.GuiceAopRendererForSpecialMethodsTest)
        ├── [SYNTHETIC] \$getStaticMetaClass()                                 GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── compare(Integer, Integer)                                         GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── [SYNTHETIC] compare(Object, Object)                               GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── getMetaClass()                                                    GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── getProperty(String)                                               GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── invokeMethod(String, Object)                                      GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── [SYNTHETIC] methodMissing(String, Object)                         GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── [SYNTHETIC] propertyMissing(String)                               GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── [SYNTHETIC] propertyMissing(String, Object)                       GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── reversed()                                                        GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── setMetaClass(MetaClass)                                           GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── setProperty(String, Object)                                       GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── thenComparing(Comparator<? super Object>)                         GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── thenComparing(Function<? super Object, ? extends Object>)         GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── thenComparing(Function<? super Object, ? extends Object>, Comparator<? super Object>)      GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── thenComparingDouble(ToDoubleFunction<? super Object>)             GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        ├── thenComparingInt(ToIntFunction<? super Object>)                   GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
        └── thenComparingLong(ToLongFunction<? super Object>)                 GuiceAopRendererForSpecialMethodsTest\$App\$1\$1
""" as String;
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(Service)
                            bindInterceptor(Matchers.subclassesOf(Service), Matchers.any(), new MethodInterceptor() {
                                @Override
                                Object invoke(MethodInvocation invocation) throws Throwable {
                                    return null
                                }
                            })
                        }
                    })
                    .printGuiceAopMap()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    String render(GuiceAopConfig config) {
        renderer.renderReport(config).replace("\r", "").replaceAll(" +\n", "\n")
        // unify package name for jdk 9 and above
                .replace('java.base/jdk.internal', 'sun')
    }

    // synthetic methods would be provided by groovy methods
    static class Service implements Comparator<Integer> {

        // force bridge, ignored by guice
        @Override
        int compare(Integer o1, Integer o2) {
            return 0
        }
    }
}
