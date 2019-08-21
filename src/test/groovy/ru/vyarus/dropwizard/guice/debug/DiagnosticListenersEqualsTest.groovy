package ru.vyarus.dropwizard.guice.debug

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.debug.report.yaml.BindingsConfig

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
        LifecycleDiagnostic l1 = new LifecycleDiagnostic(false)
        LifecycleDiagnostic l2 = new LifecycleDiagnostic(true)

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
