package ru.vyarus.guicey.admin

import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.admin.support.AdminRestApplication

/**
 * @author Vyacheslav Rusakov 
 * @since 08.08.2015
 */
@TestDropwizardApp(value = AdminRestApplication,
        config = 'src/test/resources/ru/vyarus/guicey/admin/simpleConfig.yml')
class SimpleServerTest extends AbstractTest {

    def "Check res access from user context"() {

        when: "opened rest"
        def res = new URL("http://localhost:8080/application/rest/hybrid/hello").getText()
        then: "ok"
        res == "hello"

        when: "admin only rest"
        new URL("http://localhost:8080/application/rest/hybrid/admin").getText()
        then: "not accessible"
        thrown(FileNotFoundException)

        when: "admin only rest (by class)"
        new URL("http://localhost:8080/application/rest/admin/").getText()
        then: "not accessible"
        thrown(FileNotFoundException)
    }

    def "Check access from admin context"() {

        when: "public rest"
        def res = new URL("http://localhost:8080/admin/rest/hybrid/hello").getText()
        then: "ok"
        res == "hello"

        when: "admin rest"
        res = new URL("http://localhost:8080/admin/rest/hybrid/admin").getText()
        then: "ok"
        res == "admin"

        when: "admin rest (by class annotation)"
        res = new URL("http://localhost:8080/admin/rest/admin/").getText()
        then: "ok"
        res == "hello"
    }
}