package ru.vyarus.dropwizard.guice.admin

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.admin.support.ManualAdminRestPathApp
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

/**
 * @author Vyacheslav Rusakov 
 * @since 08.08.2015
 */
@UseDropwizardApp(value = ManualAdminRestPathApp,
        config = 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml')
class ManualAdminRestMappingTest extends AbstractTest {

    def "Check access from admin context"() {
        
        when: "public rest"
        def res = new URL("http://localhost:8081/rest/hybrid/hello").getText()
        then: "ok"
        res == "hello"

        when: "admin rest"
        res = new URL("http://localhost:8081/rest/hybrid/admin").getText()
        then: "ok"
        res == "admin"

        when: "admin rest (by class annotation)"
        res = new URL("http://localhost:8081/rest/admin/").getText()
        then: "ok"
        res == "hello"
    }
}