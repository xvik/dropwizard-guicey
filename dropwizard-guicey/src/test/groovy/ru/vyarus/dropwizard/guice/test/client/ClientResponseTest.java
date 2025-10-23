package ru.vyarus.dropwizard.guice.test.client;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.client.builder.TestClientResponse;
import ru.vyarus.dropwizard.guice.test.client.util.FileDownloadUtil;
import ru.vyarus.dropwizard.guice.test.client.builder.util.conf.MultipartSupport;
import ru.vyarus.dropwizard.guice.test.client.support.ClientApp;
import ru.vyarus.dropwizard.guice.test.client.support.FileResource;
import ru.vyarus.dropwizard.guice.test.client.support.Resource;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Vyacheslav Rusakov
 * @since 08.10.2025
 */
@TestDropwizardApp(value = ClientApp.class, apacheClient = true)
public class ClientResponseTest {

    @TempDir
    Path temp;

    @Test
    void testResponseAssertions(ClientSupport client) {

        final ResourceClient<Resource> rest = client.restClient(Resource.class);

        final TestClientResponse response = rest.method(Resource::get).invoke();
        response.assertResponse(res -> res.getStatus() == 200);
        assertThat(response.toString()).isEqualTo("Response: 200 OK");

        assertThatThrownBy(() -> response.assertResponse(res -> res.getStatus() == 500))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Response does not match condition ==> expected: <true> but was: <false>");

        assertThatThrownBy(() -> response.assertResponse(List.class, res -> res.size() == 1))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Response does not match condition ==> expected: <true> but was: <false>");

        // response body was already read
        final TestClientResponse response2 = rest.method(Resource::get).invoke();
        assertThatThrownBy(() -> response2.assertResponse(new GenericType<List<Integer>>() {}, res -> res.size() == 1))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Response does not match condition ==> expected: <true> but was: <false>");

        assertThatThrownBy(() -> response2.assertStatus(300))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Unexpected response status 200 when expected 300 ==> expected: <true> but was: <false>");

        assertThatThrownBy(() -> response2.assertStatus(Response.Status.Family.CLIENT_ERROR))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected 'CLIENT_ERROR' response status, but found 'SUCCESSFUL' ==> expected: <CLIENT_ERROR> but was: <SUCCESSFUL>");

        assertThatThrownBy(() -> response2.assertStatus(statusType -> statusType.getStatusCode() == 300))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Response status '200 OK' does not match condition ==> expected: <true> but was: <false>");

        response2.assertSuccess().assertStatus(200);

        assertThatThrownBy(response2::assertFail)
                .isInstanceOf(AssertionError.class)
                .hasMessage("Failed response expected, but found 'SUCCESSFUL' ==> expected: not equal but was: <SUCCESSFUL>");

        assertThatThrownBy(response2::assertRedirect)
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected 'REDIRECTION' response status, but found 'SUCCESSFUL' ==> expected: <REDIRECTION> but was: <SUCCESSFUL>");

        // response body was already read
        final TestClientResponse response3 = rest.method(Resource::get).invoke();

        assertThatThrownBy(response3::assertVoidResponse)
                .isInstanceOf(AssertionError.class)
                .hasMessage("Void response expected, but found: \n[1,2,3] ==> expected: <false> but was: <true>");

        // response body was already read
        final TestClientResponse response4 = rest.method(Resource::del).invoke();
        response4.assertVoidResponse();


        final TestClientResponse response5 = rest.method(Resource::get).invoke();
        response5.assertResponse(String.class, "[1,2,3]"::equals);

        final TestClientResponse response6 = rest.method(Resource::get).invoke();
        response6.assertResponse(new GenericType<String>() {}, "[1,2,3]"::equals);
    }

    @Test
    void testResponseClose(ClientSupport client) {
        final ResourceClient<Resource> rest = client.restClient(Resource.class);
        
        final TestClientResponse response7 = rest.method(Resource::get).invoke();
        response7.close();
        assertThatThrownBy(() -> response7.as(String.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Entity input stream has already been closed.");
    }

    @Test
    void testResponseHeadersAssertions(ClientSupport client) {
        final ResourceClient<Resource> rest = client.restClient(Resource.class);

        final TestClientResponse response = rest.method(Resource::filled).invoke();

        // WHEN success checks
        response.assertCacheControl(CacheControl::isMustRevalidate)
                .assertHeader("HH", "3")
                .assertHeader(HttpHeader.X_POWERED_BY, "4")
                .assertHeader("HH", "3"::equals)
                .assertHeader(HttpHeader.X_POWERED_BY, "4"::equals)
                .assertMedia(MediaType.TEXT_PLAIN_TYPE)
                .assertLocale(Locale.CANADA)
                .assertCookie("C", "12")
                .assertCookie("C", cookie -> cookie.getValue().equals("12"));

        // WHEN failed assertions
        assertThatThrownBy(() -> response.assertHeader("Unknw", "12"))
                .isInstanceOf(AssertionError.class)
                .hasMessageStartingWith("Missing header 'Unknw' in response. Available headers: ");

        assertThatThrownBy(() -> response.assertHeader("HH", "4"))
                .isInstanceOf(AssertionError.class)
                .hasMessageStartingWith("Expected header 'HH' value '4', but found '3'");

        assertThatThrownBy(() -> response.assertHeader(HttpHeader.ACCEPT_LANGUAGE, s -> s.equals("11")))
                .isInstanceOf(AssertionError.class)
                .hasMessageStartingWith("Missing header 'Accept-Language' in response. Available headers: ");

        assertThatThrownBy(() -> response.assertHeader(HttpHeader.X_POWERED_BY, s -> "5".equals(s)))
                .isInstanceOf(AssertionError.class)
                .hasMessageStartingWith("Header 'X-Powered-By: 4' does not match condition");

        assertThatThrownBy(() -> response.assertMedia(MediaType.APPLICATION_JSON_TYPE))
                .isInstanceOf(AssertionError.class)
                .hasMessageStartingWith("Expected 'application/json' media type, but found 'text/plain'");

        assertThatThrownBy(() -> response.assertLocale(Locale.CHINA))
                .isInstanceOf(AssertionError.class)
                .hasMessageStartingWith("Expected 'zh_CN' response locale, but found 'en_CA'");

        assertThatThrownBy(() -> response.assertCookie("Unkwn", "1"))
                .isInstanceOf(AssertionError.class)
                .hasMessageStartingWith("Missing cookie 'Unkwn' in response. Available cookies:");

        assertThatThrownBy(() -> response.assertCookie("C", "1"))
                .isInstanceOf(AssertionError.class)
                .hasMessageStartingWith("Expected cookie 'C: 1', but found '12'");

        assertThatThrownBy(() -> response.assertCookie("Unkwn", cookie -> cookie.getValue().equals("1")))
                .isInstanceOf(AssertionError.class)
                .hasMessageStartingWith("Missing cookie 'Unkwn' in response. Available cookies:");

        assertThatThrownBy(() -> response.assertCookie("C", cookie -> cookie.getValue().equals("1")))
                .isInstanceOf(AssertionError.class)
                .hasMessageStartingWith("Cookie '$Version=1;C=12' does not match condition");
    }

    @Test
    void testWithMethods(ClientSupport client) {
        final ResourceClient<Resource> rest = client.restClient(Resource.class);

        final TestClientResponse response = rest.method(Resource::filled).invoke();

        // WHEN with* checks
        response.withHeader("HH", s -> assertThat(s).isEqualTo("3"));
        response.withHeader(HttpHeader.X_POWERED_BY, s -> assertThat(s).isEqualTo("4"));
        response.withStatus(statusType -> assertThat(statusType.getStatusCode()).isEqualTo(200));
        response.withCacheControl(cc -> assertThat(cc.isMustRevalidate()).isTrue());
        response.withCookie("C", cookie -> assertThat(cookie.getValue()).isEqualTo("12"));
        response.withResponse(res -> assertThat(res.getStatus()).isEqualTo(200));
        response.withResponse(String.class, s -> assertThat(s).isEqualTo("OK"));

        // reset body
        final TestClientResponse response2 = rest.method(Resource::filled).invoke();
        response2.withResponse(new GenericType<String>() {}, s -> assertThat(s).isEqualTo("OK"));

        // reset body
        final TestClientResponse response3 = rest.method(Resource::filled).invoke();

        // WHEN with* fail
        assertThatThrownBy(() -> response3.withHeader("HH", s ->
                assertThat(s).as("Bad HH header").isEqualTo("4")))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Bad HH header");

        assertThatThrownBy(() -> response3.withHeader(HttpHeader.X_POWERED_BY, s ->
                assertThat(s).as("Bad header").isEqualTo("5")))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Bad header");

        assertThatThrownBy(() -> response3.withStatus(s ->
                assertThat(s.getFamily()).as("Bad status").isEqualTo(Response.Status.Family.CLIENT_ERROR)))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Bad status");

        assertThatThrownBy(() -> response3.withCacheControl(cc ->
                assertThat(assertThat(cc.isMustRevalidate()).as("Cache revalidation enabled").isFalse())))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Cache revalidation enabled");

        assertThatThrownBy(() -> response3.withCookie("C", c ->
                assertThat(assertThat(c.getValue()).as("Cookie C invalid").isEqualTo("1"))))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Cookie C invalid");

        assertThatThrownBy(() -> response3.withResponse(res ->
                assertThat(assertThat(res.getStatus()).as("Bad status").isEqualTo(201))))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Bad status");

        assertThatThrownBy(() -> response3.withResponse(new GenericType<String>() {}, res ->
                assertThat(assertThat(res).as("Bad response").isEqualTo("KO"))))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Bad response");
    }

    @Test
    void testResponseMappings(ClientSupport client) {
        final ResourceClient<Resource> rest = client.restClient(Resource.class);

        assertThat(rest.method(Resource::get).invoke()
                .as(List.class)).hasSize(3).contains(1, 2, 3);
        assertThat(rest.method(Resource::get).invoke()
                .as(new GenericType<List<Integer>>() {})).hasSize(3).contains(1, 2, 3);
        assertThat((Integer) rest.method(Resource::get).invoke()
                .as(response -> response.readEntity(new GenericType<List<Integer>>() {}).get(0))).isEqualTo(1);
        assertThat(rest.method(Resource::get).invoke().asResponse().getStatus()).isEqualTo(200);

        assertThat(rest.method(Resource::get).invoke().asList(Integer.class)).containsExactly(1, 2, 3);

    }

    @Test
    void testFileDownload(ClientSupport client) {
        final ResourceClient<FileResource> rest = client.restClient(FileResource.class);

        // WHEN jersey file download (temp)
        File res = rest.method(FileResource::download).as(File.class);
        System.out.println(res.getAbsolutePath());
        String tmp = System.getProperty("java.io.tmpdir");
        assertThat(res.getAbsolutePath()).startsWith(tmp);
        assertThat(res.getName()).isNotEqualTo("logback.xml");

        // WHEN file download api
        Path file = rest.method(FileResource::download).invoke().asFile(temp);
        assertThat(file.getParent()).isEqualTo(temp);
        assertThat(file.getFileName().toString()).isEqualTo("logback.xml");

        // WHEN same file download (name counter appears)
        file = rest.method(FileResource::download).invoke().asFile(temp);
        assertThat(file.getFileName().toString()).isEqualTo("logback(1).xml");

        // WHEN raw response
        Response response = rest.method(FileResource::download).invoke().asResponse();
        assertThat(MultipartSupport.readFilename(response)).isEqualTo("logback.xml");
        assertThat(FileDownloadUtil.parseFileName(response)).isEqualTo("logback.xml");

        // WHEN non file response
        response = client.restClient(Resource.class).method(instance -> instance.get()).invoke().asResponse();
        assertThat(MultipartSupport.readFilename(response)).isNull();
        assertThat(FileDownloadUtil.parseFileName(response)).isNull();

        // WHEN incorrect header
        assertThatThrownBy(() -> MultipartSupport.readFilename("incorrect header"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to parse Content-Disposition header");
    }
}
