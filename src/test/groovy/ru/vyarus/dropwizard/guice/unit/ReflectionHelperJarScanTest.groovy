package ru.vyarus.dropwizard.guice.unit

import ru.vyarus.dropwizard.guice.module.installer.scanner.util.OReflectionHelper
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class ReflectionHelperJarScanTest extends Specification {

    def "Check jar scan"() {

        when: "scan jars"
        List<Class> classes = OReflectionHelper.getClassesFor("io.dropwizard.cli", Thread.currentThread().getContextClassLoader())
        then: "classes found"
        classes.size() == 6
    }
}