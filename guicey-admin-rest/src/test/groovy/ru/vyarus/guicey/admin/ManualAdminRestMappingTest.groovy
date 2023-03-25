package ru.vyarus.guicey.admin

import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.admin.support.ManualAdminRestPathApp

/**
 * @author Vyacheslav Rusakov 
 * @since 08.08.2015
 */
@TestDropwizardApp(ManualAdminRestPathApp)
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