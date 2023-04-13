package ru.vyarus.guicey.gsp

import org.apache.commons.text.StringEscapeUtils
import ru.vyarus.dropwizard.guice.test.ClientSupport
import spock.lang.Specification

import jakarta.ws.rs.client.WebTarget
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

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
        call(main(), url, false)
    }

    String getHtml(String url) {
        call(main(), url, true)
    }

    String adminGet(String url) {
        call(admin(), url, false)
    }

    String adminGetHtml(String url) {
        call(admin(), url, true)
    }

    private WebTarget main() {
        return client.target('http://localhost:8080')
    }

    private WebTarget admin() {
        return client.target('http://localhost:8081')
    }

    private String call(WebTarget http, String path, boolean html) {
        Response res = http.path(path).request(html ? MediaType.TEXT_HTML : MediaType.TEXT_PLAIN).get()
        if (res.status == 404) {
            throw new FileNotFoundException()
        } else if (res.status != 200) {
            throw new IOException("status: ${res.status}")
        }
        String body = res.readEntity(String)
        if (html) {
            body = StringEscapeUtils.unescapeHtml4(body)
        }
        return body
    }
}
