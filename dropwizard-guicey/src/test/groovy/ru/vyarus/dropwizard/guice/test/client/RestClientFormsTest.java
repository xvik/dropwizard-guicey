package ru.vyarus.dropwizard.guice.test.client;

import jakarta.ws.rs.core.MultivaluedHashMap;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.client.support.ClientApp;
import ru.vyarus.dropwizard.guice.test.client.support.FormBeanResource;
import ru.vyarus.dropwizard.guice.test.client.support.FormResource;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

import java.io.File;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 10.10.2025
 */
@TestDropwizardApp(value = ClientApp.class)
public class RestClientFormsTest {

    @Test
    void testUrlencodedForms(ClientSupport client) {
        final ResourceClient<FormResource> rest = client.restClient(FormResource.class);

        // WHEN simple values
        assertThat(rest.buildForm("/post")
                .param("name", "1")
                .param("date", "2")
                .buildPost()
                .as(String.class)).isEqualTo("name=1, date=2");

        assertThat(rest.method(instance -> instance.post("1", "2"))
                .as(String.class)).isEqualTo("name=1, date=2");

        // WHEN multimap parameter
        assertThat(rest.buildForm("/post2")
                .param("name", "1")
                .param("date", "2")
                .buildPost()
                .as(String.class)).contains("name=[1]").contains("date=[2]");

        final MultivaluedHashMap<String, String> map = new MultivaluedHashMap<>();
        map.add("name", "1");
        map.add("date", "2");
        assertThat(rest.method(instance -> instance.post2(map))
                .as(String.class)).contains("name=[1]").contains("date=[2]");

        // WHEN multivalue
        assertThat(rest.buildForm("/postMulti")
                .param("name", 1, 2, 3)
                .param("date", "2")
                .buildPost()
                .as(String.class)).isEqualTo("name=[1, 2, 3], date=2");

        assertThat(rest.method(instance -> instance.postMulti(Arrays.asList("1", "2", "3"), "2"))
                .as(String.class)).isEqualTo("name=[1, 2, 3], date=2");

        // WHEN multimap parameter with multivalues
        assertThat(rest.buildForm("/post2Multi")
                .param("name", 1, 2, 3)
                .param("date", "2")
                .buildPost()
                .as(String.class)).contains("name=[1, 2, 3]").contains("date=[2]");

        final MultivaluedHashMap<String, String> map2 = new MultivaluedHashMap<>();
        map2.addAll("name", "1", "2", "3");
        map2.add("date", "2");
        assertThat(rest.method(instance -> instance.post2Multi(map2))
                .as(String.class)).contains("name=[1, 2, 3]").contains("date=[2]");
    }

    @Test
    void testUrlencodedWithBeans(ClientSupport client) {
        final ResourceClient<FormBeanResource> rest = client.restClient(FormBeanResource.class);

        // WHEN simple values
        assertThat(rest.buildForm("/post")
                .param("name", "1")
                .param("date", "2")
                .buildPost()
                .as(String.class)).isEqualTo("name=1, date=2");

        assertThat(rest.method(instance -> instance.post(new FormBeanResource.SimpleBean("1", "2")))
                .as(String.class)).isEqualTo("name=1, date=2");


        // WHEN multivalue
        assertThat(rest.buildForm("/postMulti")
                .param("name", 1, 2, 3)
                .param("date", "2")
                .buildPost()
                .as(String.class)).isEqualTo("name=[1, 2, 3], date=2");

        assertThat(rest.method(instance -> instance.postMulti(new FormBeanResource.SimpleMultiBean(Arrays.asList("1", "2", "3"), "2")))
                .as(String.class)).isEqualTo("name=[1, 2, 3], date=2");
    }


    @Test
    void testGet(ClientSupport client) {
        final ResourceClient<FormResource> rest = client.restClient(FormResource.class);

        // WHEN simple values
        assertThat(rest.buildForm("/get")
                .param("name", "1")
                .param("date", "2")
                .buildGet()
                .as(String.class)).isEqualTo("name=1, date=2");

        assertThat(rest.method(instance -> instance.get("1", "2"))
                .as(String.class)).isEqualTo("name=1, date=2");

        // WHEN multivalue
        assertThat(rest.buildForm("/getMulti")
                .param("name", 1, 2, 3)
                .param("date", "2")
                .buildGet()
                .as(String.class)).isEqualTo("name=[1, 2, 3], date=2");

        assertThat(rest.method(instance -> instance.getMulti(Arrays.asList("1", "2", "3"), "2"))
                .as(String.class)).isEqualTo("name=[1, 2, 3], date=2");
    }

    @Test
    void testMultipart(ClientSupport client) {
        final ResourceClient<FormResource> rest = client.restClient(FormResource.class);

        // WHEN 2 args
        assertThat(rest.buildForm("/multipart")
                .param("file", new File("src/test/resources/logback.xml"))
                .buildPost()
                .as(String.class)).isEqualTo("logback.xml");

        assertThat(rest.multipartMethod((instance, multipart) ->
                        instance.multipart(multipart.fromClasspath("/logback.xml"),
                                multipart.disposition("file", "logback.xml")))
                .as(String.class)).isEqualTo("logback.xml");


        // WHEN body param arg
        assertThat(rest.buildForm("/multipart2")
                .param("file", new File("src/test/resources/logback.xml"))
                .buildPost()
                .as(String.class)).isEqualTo("logback.xml");

        assertThat(rest.multipartMethod((instance, multipart) ->
                        instance.multipart2(multipart.streamPart("file", "/logback.xml")))
                .as(String.class)).isEqualTo("logback.xml");

        assertThat(rest.multipartMethod((instance, multipart) ->
                        instance.multipart2(multipart.filePart("file", "src/test/resources/logback.xml")))
                .as(String.class)).isEqualTo("logback.xml");


        // WHEN multiple params with the same name
        assertThat(rest.buildForm("/multipartMulti")
                .param("file", new File("src/test/resources/logback.xml"), new File("src/test/resources/logback.xml"))
                .buildPost()
                .as(String.class)).isEqualTo("logback.xml");

        assertThat(rest.multipartMethod((instance, multipart) ->
                        instance.multipartMulti(Arrays.asList(
                                multipart.filePart("file", "src/test/resources/logback.xml"),
                                multipart.filePart("file", "src/test/resources/logback.xml"))))
                .as(String.class)).isEqualTo("logback.xml");

        // WHEN multiple dispositions with the same name
        assertThat(rest.buildForm("/multipartMulti2")
                .param("file", new File("src/test/resources/logback.xml"), new File("src/test/resources/logback.xml"))
                .buildPost()
                .as(String.class)).isEqualTo("logback.xml");

        assertThat(rest.multipartMethod((instance, multipart) ->
                        instance.multipartMulti2(Arrays.asList(
                                multipart.disposition("file", "logback.xml"),
                                multipart.disposition("file", "logback.xml"))))
                .as(String.class)).isEqualTo("logback.xml");

        // WHEN generic multipart used
        assertThat(rest.buildForm("/multipartGeneric")
                .param("file", new File("src/test/resources/logback.xml"))
                .buildPost()
                .as(String.class)).isEqualTo("logback.xml");

        assertThat(rest.multipartMethod((instance, multipart) ->
                        instance.multipartGeneric(multipart.multipart()
                                .field("foo", "bar")
                                .stream("file", "/logback.xml")
                                .build()))
                .as(String.class)).isEqualTo("logback.xml");
    }

    @Test
    void testMultipartWithBeans(ClientSupport client) {
        final ResourceClient<FormBeanResource> rest = client.restClient(FormBeanResource.class);

        // WHEN 2 args
        assertThat(rest.buildForm("/multipart")
                .param("file", new File("src/test/resources/logback.xml"))
                .buildPost()
                .as(String.class)).isEqualTo("logback.xml");

        assertThat(rest.multipartMethod((instance, multipart) ->
                        instance.multipart(new FormBeanResource.MultipartBean(
                                multipart.fromClasspath("/logback.xml"),
                                multipart.disposition("file", "logback.xml"))))
                .as(String.class)).isEqualTo("logback.xml");


        // WHEN body param arg
        assertThat(rest.buildForm("/multipart2")
                .param("file", new File("src/test/resources/logback.xml"))
                .buildPost()
                .as(String.class)).isEqualTo("logback.xml");

        assertThat(rest.multipartMethod((instance, multipart) ->
                        instance.multipart2(new FormBeanResource.MultipartBean2(multipart.streamPart("file", "/logback.xml"))))
                .as(String.class)).isEqualTo("logback.xml");

        assertThat(rest.multipartMethod((instance, multipart) ->
                        instance.multipart2(new FormBeanResource.MultipartBean2(multipart.filePart("file", "src/test/resources/logback.xml"))))
                .as(String.class)).isEqualTo("logback.xml");


        // WHEN multiple params with the same name
        assertThat(rest.buildForm("/multipartMulti")
                .param("file", new File("src/test/resources/logback.xml"), new File("src/test/resources/logback.xml"))
                .buildPost()
                .as(String.class)).isEqualTo("logback.xml");

        assertThat(rest.multipartMethod((instance, multipart) ->
                        instance.multipartMulti(new FormBeanResource.MultipartMultiBean(Arrays.asList(
                                multipart.filePart("file", "src/test/resources/logback.xml"),
                                multipart.filePart("file", "src/test/resources/logback.xml")))))
                .as(String.class)).isEqualTo("logback.xml");

        // WHEN multiple dispositions with the same name
        assertThat(rest.buildForm("/multipartMulti2")
                .param("file", new File("src/test/resources/logback.xml"), new File("src/test/resources/logback.xml"))
                .buildPost()
                .as(String.class)).isEqualTo("logback.xml");

        assertThat(rest.multipartMethod((instance, multipart) ->
                        instance.multipartMulti2(new FormBeanResource.MultipartMultiBean2(Arrays.asList(
                                multipart.disposition("file", "logback.xml"),
                                multipart.disposition("file", "logback.xml")))))
                .as(String.class)).isEqualTo("logback.xml");

    }
}
