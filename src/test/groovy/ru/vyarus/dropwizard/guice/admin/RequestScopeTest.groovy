package ru.vyarus.dropwizard.guice.admin

import ru.vyarus.dropwizard.guice.admin.support.AdminRestApplication
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2015
 */
@UseDropwizardApp(value = AdminRestApplication,
        config = 'src/test/resources/ru/vyarus/dropwizard/guice/admin/simpleServerConfig.yml')
class RequestScopeTest extends Specification {

    def "Check request scope exist when access from admin context"() {

        when: "rest with request scope"
        def res = new URL("http://localhost:8080/admin/rest/request/").getText()
        then: "ok"
        res == "hello"
    }
}