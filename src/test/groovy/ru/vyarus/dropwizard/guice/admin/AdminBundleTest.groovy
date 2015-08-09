package ru.vyarus.dropwizard.guice.admin

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.admin.support.AdminRestApplication
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

/**
 * @author Vyacheslav Rusakov 
 * @since 08.08.2015
 */
@UseDropwizardApp(value = AdminRestApplication,
        config = 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml')
class AdminBundleTest extends AbstractTest {

    def "Check res access from user context"() {

        when: "opened rest"
        def res = new URL("http://localhost:8080/hybrid/hello").getText()
        then: "ok"
        res == "hello"

        when: "admin only rest"
        new URL("http://localhost:8080/hybrid/admin").getText()
        then: "not accessible"
        def ex = thrown(IOException)
        ex.getMessage().contains("403")

        when: "admin only rest (by class)"
        new URL("http://localhost:8080/admin/").getText()
        then: "not accessible"
        ex = thrown(IOException)
        ex.getMessage().contains("403")
    }

    def "Check access from admin context"() {

        // when rest is registered to root, admin rest is accessible from  /api/
        when: "public rest"
        def res = new URL("http://localhost:8081/api/hybrid/hello").getText()
        then: "ok"
        res == "hello"

        when: "admin rest"
        res = new URL("http://localhost:8081/api/hybrid/admin").getText()
        then: "ok"
        res == "admin"

        when: "admin rest (by class annotation)"
        res = new URL("http://localhost:8081/api/admin/").getText()
        then: "ok"
        res == "hello"
    }
}