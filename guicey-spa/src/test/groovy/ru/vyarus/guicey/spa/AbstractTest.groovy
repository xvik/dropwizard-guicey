package ru.vyarus.guicey.spa

import org.apache.commons.text.StringEscapeUtils
import ru.vyarus.dropwizard.guice.test.ClientSupport
import spock.lang.Specification

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/**
 * @author Vyacheslav Rusakov
 * @since 29.11.2019
 */
abstract class AbstractTest extends Specification {

    // default builder for text/html type (user call simulation)
    ClientSupport client

    void setup(ClientSupport client) {
        this.client = client
    }

    // shortcut to return body
    String get(String url) {
        call(main(), url)
    }

    String adminGet(String url) {
        call(admin(), url)
    }

    protected WebTarget main() {
        return client.target('http://localhost:8080')
    }

    protected WebTarget admin() {
        return client.target('http://localhost:8081')
    }

    private String call(WebTarget http, String path) {
        Response res = http.path(path).request(MediaType.TEXT_HTML).get()
        if (res.status == 404) {
            throw new FileNotFoundException()
        } else if (res.status != 200) {
            throw new IOException("status: ${res.status}")
        }
        return StringEscapeUtils.unescapeHtml4(res.readEntity(String))
    }
}
