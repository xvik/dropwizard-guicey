package ru.vyarus.dropwizard.guice.config.debug

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.module.context.debug.ConfigurationDiagnostic
import ru.vyarus.dropwizard.guice.module.context.debug.GuiceBindingsDiagnostic
import ru.vyarus.dropwizard.guice.module.context.debug.YamlBindingsDiagnostic
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.module.context.debug.report.yaml.BindingsConfig
import ru.vyarus.dropwizard.guice.module.lifecycle.debug.DebugGuiceyLifecycle

/**
 * @author Vyacheslav Rusakov
 * @since 20.08.2019
 */
class DiagnosticListenersEqualsTest extends AbstractTest {

    def "Check diagnostic listener"() {

        when: "two listener instances"
        ConfigurationDiagnostic l1 = new ConfigurationDiagnostic()
        ConfigurationDiagnostic l2 = new ConfigurationDiagnostic()

        then: "equal"
        l1.equals(l2)
        l1.hashCode() == l1.hashCode()

        when: "different title"
        l1 = ConfigurationDiagnostic.builder("foo").build()
        l2 = ConfigurationDiagnostic.builder("bar").build()
        then: "not equal"
        !l1.equals(l2)
        l1.hashCode() != l2.hashCode()
    }

    def "Check yaml listener"() {

        when: "two listener instances"
        YamlBindingsDiagnostic l1 = new YamlBindingsDiagnostic()
        YamlBindingsDiagnostic l2 = new YamlBindingsDiagnostic(new BindingsConfig().showCustomConfigOnly())

        then: "equal"
        l1.equals(l2)
        l1.hashCode() == l1.hashCode()

    }

    def "Check lifecycle listener"() {

        when: "two listener instances"
        DebugGuiceyLifecycle l1 = new DebugGuiceyLifecycle(false)
        DebugGuiceyLifecycle l2 = new DebugGuiceyLifecycle(true)

        then: "equal"
        l1.equals(l2)
        l1.hashCode() == l1.hashCode()

    }

    def "Check guice bindings listener"() {

        when: "two listener instances"
        GuiceBindingsDiagnostic l1 = new GuiceBindingsDiagnostic(new GuiceConfig())
        GuiceBindingsDiagnostic l2 = new GuiceBindingsDiagnostic(new GuiceConfig().hideGuiceBindings())

        then: "equal"
        l1.equals(l2)
        l1.hashCode() == l1.hashCode()

    }
}
