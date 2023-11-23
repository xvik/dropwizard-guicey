package ru.vyarus.dropwizard.guice.yaml

import com.google.inject.name.Named
import com.google.inject.name.Names
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule
import ru.vyarus.dropwizard.guice.module.yaml.ConfigTreeBuilder
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.dropwizard.guice.yaml.support.AnnotatedConfig
import ru.vyarus.dropwizard.guice.yaml.support.ComplexGenericCase
import ru.vyarus.dropwizard.guice.yaml.support.CustQualifier
import ru.vyarus.dropwizard.guice.yaml.support.NotUniqueSubConfig
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 11.06.2018
 */
@TestGuiceyApp(App)
class ShortcutsTest extends Specification {

    @Inject
    Bootstrap bootstrap

    def "Check module shortcuts"() {

        when: "config with not unique custom type"
        def config = create(NotUniqueSubConfig)
        config.sub1 = new NotUniqueSubConfig.SubConfig(sub: "val")
        config.sub2 = new NotUniqueSubConfig.SubConfig()
        def res = ConfigTreeBuilder.build(bootstrap, config)
        def mod = new DropwizardAwareModule() {}
        mod.setConfigurationTree(res)
        then:
        mod.configuration("not.exists") == null
        mod.configuration("sub1") != null
        mod.configuration("sub1.sub") != null
        mod.configuration(NotUniqueSubConfig.SubConfig) == null
        mod.configurations(NotUniqueSubConfig.SubConfig).size() == 2

        when: "config with unique custom type"
        config = create(ComplexGenericCase)
        config.sub = new ComplexGenericCase.SubImpl()
        res = ConfigTreeBuilder.build(bootstrap, config)
        mod = new DropwizardAwareModule() {}
        mod.setConfigurationTree(res)
        then:
        mod.configuration("not.exists") == null
        mod.configuration("sub") != null
        mod.configuration("sub.smth") == "sample"
        mod.configuration(ComplexGenericCase.Sub) != null
        mod.configurations(ComplexGenericCase.Sub).size() == 1


        when: "config with qualified properties"
        config = create(AnnotatedConfig)
        config.prop = "1"
        config.prop2 = "2"
        config.prop3 = 3
        config.custom = "cust"
        res = ConfigTreeBuilder.build(bootstrap, config)
        mod = new DropwizardAwareModule() {}
        mod.setConfigurationTree(res)
        then:
        mod.annotatedConfiguration(CustQualifier) == "cust"
        mod.configurationTree().annotatedValues(CustQualifier) == ["cust"] as Set
        mod.annotatedConfiguration(Names.named("test")) == "1"
        mod.configurationTree().annotatedValues(Names.named("test2")) == ["2", 3] as Set
        
        when: "non-unique call"
        mod.annotatedConfiguration(Named)
        then:
        def ex = thrown(IllegalStateException)
        ex.message.startsWith("Multiple configuration paths qualified with annotation type @Named")

        when: "non-unique call2"
        mod.annotatedConfiguration(Names.named("test2"))
        then:
        def ex2 = thrown(IllegalStateException)
        ex2.message.startsWith("Multiple configuration paths qualified with annotation @Named(\"test2\")")
    }

    def "Check bundle shortcuts"() {

        when: "config with not unique custom type"
        def config = create(NotUniqueSubConfig)
        config.sub1 = new NotUniqueSubConfig.SubConfig(sub: "val")
        config.sub2 = new NotUniqueSubConfig.SubConfig()
        def res = ConfigTreeBuilder.build(bootstrap, config)
        def context = new ConfigurationContext()
        context.configurationTree = res
        def bundle = new GuiceyEnvironment(context)
        then:
        bundle.configuration("not.exists") == null
        bundle.configuration("sub1") != null
        bundle.configuration("sub1.sub") != null
        bundle.configuration(NotUniqueSubConfig.SubConfig) == null
        bundle.configurations(NotUniqueSubConfig.SubConfig).size() == 2

        when: "config with unique custom type"
        config = create(ComplexGenericCase)
        config.sub = new ComplexGenericCase.SubImpl()
        res = ConfigTreeBuilder.build(bootstrap, config)
        context = new ConfigurationContext()
        context.configurationTree = res
        bundle = new GuiceyEnvironment(context)
        then:
        bundle.configuration("not.exists") == null
        bundle.configuration("sub") != null
        bundle.configuration("sub.smth") == "sample"
        bundle.configuration(ComplexGenericCase.Sub) != null
        bundle.configurations(ComplexGenericCase.Sub).size() == 1


        when: "config with qualified properties"
        config = create(AnnotatedConfig)
        config.prop = "1"
        config.prop2 = "2"
        config.prop3 = 3
        config.custom = "cust"
        res = ConfigTreeBuilder.build(bootstrap, config)
        context = new ConfigurationContext()
        context.configurationTree = res
        bundle = new GuiceyEnvironment(context)
        then:
        bundle.annotatedConfiguration(CustQualifier) == "cust"
        bundle.configurationTree().annotatedValues(CustQualifier) == ["cust"] as Set
        bundle.annotatedConfiguration(Names.named("test")) == "1"
        bundle.configurationTree().annotatedValues(Names.named("test2")) == ["2", 3] as Set

        when: "non-unique call"
        bundle.annotatedConfiguration(Named)
        then:
        def ex = thrown(IllegalStateException)
        ex.message.startsWith("Multiple configuration paths qualified with annotation type @Named")

        when: "non-unique call2"
        bundle.annotatedConfiguration(Names.named("test2"))
        then:
        def ex2 = thrown(IllegalStateException)
        ex2.message.startsWith("Multiple configuration paths qualified with annotation @Named(\"test2\")")
    }

    private <T extends Configuration> T create(Class<T> type) {
        bootstrap.configurationFactoryFactory
                .create(type, bootstrap.validatorFactory.validator, bootstrap.objectMapper, "dw").build()
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}