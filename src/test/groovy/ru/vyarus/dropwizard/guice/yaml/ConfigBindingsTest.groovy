package ru.vyarus.dropwizard.guice.yaml

import com.google.inject.Binding
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.name.Names
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.server.ServerFactory
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.yaml.module.Config
import ru.vyarus.dropwizard.guice.module.yaml.module.ConfigImpl
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 06.06.2018
 */
@UseGuiceyApp(App)
class ConfigBindingsTest extends AbstractTest {

    @Inject
    Injector injector

    def "Check configuration bindings"() {

        expect: "root bindings"
        binding(Configuration) != null
        binding(AppConfig) != null
        binding(ConfIface) == null

        and: "root qualified bindings"
        annBinding(Configuration) != null
        annBinding(AppConfig) != null
        annBinding(ConfIface) != null

        and: "uniqe objects bound"
        annBinding(SubConf) != null
        annBinding(ServerFactory) != null

        and: "unique direct bindings not available"
        binding(SubConf) == null
        binding(ServerFactory) == null

        and: "path bindings set"
        annBindingVal(String, "some") == "val"
        annBindingVal(SubConf, "sub") != null
        annBindingVal(Integer, "sub.foo") == 12
        annBindingVal(Integer, "server.adminMaxThreads") == 64
    }

    def "Check qualifier impl"() {

        when:
        def one = new ConfigImpl("one")
        def two = new ConfigImpl("two")
        then:
        !one.equals(two)
        one.equals(new ConfigImpl("one"))
        !one.equals(Names.named("ona"))
        one.toString() != two.toString()
    }

    private Binding binding(Class type) {
        injector.getExistingBinding(Key.get(type))
    }

    private Binding annBinding(Class type) {
        injector.getExistingBinding(Key.get(type, Config))
    }

    private Object annBindingVal(Class type, String path) {
        injector.getInstance(Key.get(type, new ConfigImpl(path)))
    }

    static class App extends Application<AppConfig> {

        @Override
        void initialize(Bootstrap<AppConfig> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(AppConfig configuration, Environment environment) throws Exception {
        }
    }

    static class AppConfig extends Configuration implements ConfIface {
        String some = "val"
        SubConf sub = new SubConf()
    }

    static class SubConf {
        int foo = 12
    }

    interface ConfIface {

    }
}