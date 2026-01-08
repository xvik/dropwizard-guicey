package ru.vyarus.dropwizard.guice.url.resource;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.client.builder.call.MultipartArgumentHelper;
import ru.vyarus.dropwizard.guice.url.model.ResourceMethodInfo;
import ru.vyarus.dropwizard.guice.url.resource.support.DirectResource;
import ru.vyarus.dropwizard.guice.url.resource.support.FormResource;
import ru.vyarus.dropwizard.guice.url.resource.support.InterfaceResource;
import ru.vyarus.dropwizard.guice.url.resource.support.MappedBean;
import ru.vyarus.dropwizard.guice.url.resource.support.NoPathResource;
import ru.vyarus.dropwizard.guice.url.resource.support.ResourceDeclaration;
import ru.vyarus.dropwizard.guice.url.resource.support.SuperclassDeclarationResource;
import ru.vyarus.dropwizard.guice.url.resource.support.sub.RootResource;
import ru.vyarus.dropwizard.guice.url.resource.support.sub.SubResource1;
import ru.vyarus.dropwizard.guice.url.resource.support.sub.SubResource2;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Vyacheslav Rusakov
 * @since 29.09.2025
 */
public class ResourceAnalyzerTest {

    @Test
    void testResourcePath() {
        assertThat(ResourceAnalyzer.getResourcePath(DirectResource.class))
                .isEqualTo("/direct");
        assertThat(ResourceAnalyzer.getResourcePath(SuperclassDeclarationResource.class))
                .isEqualTo("/direct");
        assertThat(ResourceAnalyzer.getResourcePath(InterfaceResource.class))
                .isEqualTo("/iface");
    }

    @Test
    void testMethodPath() throws Exception {
        assertThat(ResourceAnalyzer.getMethodPath(DirectResource.class, "form")).isEqualTo("/form");
        assertThat(ResourceAnalyzer.getMethodPath(SuperclassDeclarationResource.class, "form")).isEqualTo("/form");
        assertThat(ResourceAnalyzer.getMethodPath(InterfaceResource.class, "form")).isEqualTo("/form");

        assertThat(ResourceAnalyzer.getMethodPath(DirectResource.class.getMethod("get", MappedBean.class)))
                .isEqualTo("/{sm}/2");

        // NO @Path on method
        assertThat(ResourceAnalyzer.getMethodPath(DirectResource.class, "nopath")).isEqualTo("/");
        assertThat(ResourceAnalyzer.getMethodPath(InterfaceResource.class, "nopath")).isEqualTo("/");
    }

    @Test
    void testAnnotatedResourceSearch() {
        assertThat(ResourceAnalyzer.findAnnotatedResource(DirectResource.class))
                .isEqualTo(DirectResource.class);
        assertThat(ResourceAnalyzer.findAnnotatedResource(SuperclassDeclarationResource.class))
                .isEqualTo(DirectResource.class);
        assertThat(ResourceAnalyzer.findAnnotatedResource(InterfaceResource.class))
                .isEqualTo(ResourceDeclaration.class);

        assertThatThrownBy(() -> ResourceAnalyzer.findAnnotatedResource(NoPathResource.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("@Path annotation was not found on resource NoPathResource or any of it's super classes and interfaces");
    }

    @Test
    void testAnnotatedMethodSearch() throws Exception {
        assertThat(ResourceAnalyzer.findAnnotatedMethod(DirectResource.class.getMethod("get", MappedBean.class)).getDeclaringClass())
                .isEqualTo(DirectResource.class);
        assertThat(ResourceAnalyzer.findAnnotatedMethod(SuperclassDeclarationResource.class.getMethod("get", MappedBean.class)).getDeclaringClass())
                .isEqualTo(DirectResource.class);
        assertThat(ResourceAnalyzer.findAnnotatedMethod(InterfaceResource.class.getMethod("get", MappedBean.class)).getDeclaringClass())
                .isEqualTo(ResourceDeclaration.class);
    }

    @Test
    void testMethodCallAnalysis() throws Exception {
        // WHEN analyze get
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res.get("1", "2", "3", "4", "5"));

        assertThat(info.getResource()).isEqualTo(DirectResource.class);
        assertThat(info.getResourcePath()).isEqualTo("/direct");
        assertThat(info.getMethod())
                .isEqualTo(DirectResource.class.getMethod("get", String.class, String.class, String.class, String.class, String.class));
        assertThat(info.getSubResources()).isEmpty();
        assertThat(info.getSteps()).isEmpty();

        assertThat(info.getPath()).isEqualTo("/{sm}");
        assertThat(info.getFullPath()).isEqualTo("/direct/{sm}");
        assertThat(info.getHttpMethod()).isEqualTo("GET");
        assertThat(info.getProduces()).contains(MediaType.APPLICATION_JSON);
        assertThat(info.getConsumes()).isEmpty();

        assertThat(info.getPathParams()).isNotEmpty().containsEntry("sm", "1");
        assertThat(info.getQueryParams()).isNotEmpty().containsEntry("q", "2");
        assertThat(info.getHeaderParams()).isNotEmpty().containsEntry("HH", "3");
        assertThat(info.getCookieParams()).isNotEmpty().containsEntry("cc", "4");
        assertThat(info.getMatrixParams()).isNotEmpty().containsEntry("mm", "5");
        assertThat(info.toString()).isEqualTo("DirectResource.Response get(String, String, String, String, String) (/direct/{sm})");


        // WHEN null values provided
        info = ResourceAnalyzer.analyzeMethodCall(DirectResource.class, res -> res.get(null, null, null, null, null));
        assertThat(info.getPathParams()).isEmpty();
        assertThat(info.getQueryParams()).isEmpty();
        assertThat(info.getHeaderParams()).isEmpty();
        assertThat(info.getCookieParams()).isEmpty();
        assertThat(info.getMatrixParams()).isEmpty();

        // WHEN method without path annotation
        info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, DirectResource::nopath);
        assertThat(info.getHttpMethod()).isEqualTo("GET");
        assertThat(info.getPath()).isEqualTo("/");
    }

    @Test
    void testMethodCallChainAnalysis() throws Exception {
        // WHEN analyze get
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(RootResource.class, res -> res.sub1().sub2("x").get("1"));

        assertThat(info.getResource()).isEqualTo(RootResource.class);
        assertThat(info.getResourcePath()).isEqualTo("/root");
        assertThat(info.getMethod())
                .isEqualTo(SubResource2.class.getMethod("get", String.class));
        assertThat(info.getSubResources())
                .hasSize(2)
                .contains(SubResource1.class, SubResource2.class);
        assertThat(info.getSteps()).hasSize(3);


        assertThat(info.getPath()).isEqualTo("/sub1/sub2/{name}/{sm}");
        assertThat(info.getFullPath()).isEqualTo("/root/sub1/sub2/{name}/{sm}");
        assertThat(info.getHttpMethod()).isEqualTo("GET");
        assertThat(info.getProduces()).contains(MediaType.APPLICATION_JSON);
        assertThat(info.getConsumes()).contains(MediaType.TEXT_PLAIN);

        assertThat(info.getPathParams()).hasSize(2)
                .containsEntry("name", "x")
                .containsEntry("sm", "1");
        assertThat(info.getQueryParams()).isEmpty();
        assertThat(info.getHeaderParams()).isEmpty();
        assertThat(info.getCookieParams()).isEmpty();
    }

    @Test
    void testRawEntityRecognition() {
        // WHEN analyze simple post with entity
        final DirectResource.ModelType entity = new DirectResource.ModelType("test");
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res.post2(entity));

        assertThat(info.getEntity()).isEqualTo(entity);

        // WHEN analyze simple post with annotated
        info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res.post3(entity));

        assertThat(info.getEntity()).isEqualTo(entity);
    }

    @Test
    void testBeanParamMapping() {
        MappedBean bean = new MappedBean();
        bean.setSm("1");
        bean.setQ("2");
        bean.setHh("3");
        bean.setCc("4");
        bean.setMm("5");

        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res.get(bean));

        assertThat(info.getPathParams()).isNotEmpty().containsEntry("sm", "1");
        assertThat(info.getQueryParams()).isNotEmpty().containsEntry("q", "2");
        assertThat(info.getHeaderParams()).isNotEmpty().containsEntry("HH", "3");
        assertThat(info.getCookieParams()).isNotEmpty().containsEntry("cc", "4");
        assertThat(info.getMatrixParams()).isNotEmpty().containsEntry("mm", "5");
    }

    @Test
    void testSimpleForm() {
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res.form("1", 2));

        assertThat(info.getFormParams()).isNotEmpty()
                .containsEntry("p1", "1")
                .containsEntry("p2", 2);
        assertThat(info.getConsumes()).isNotEmpty()
                .contains(MediaType.APPLICATION_FORM_URLENCODED);
    }

    @Test
    void testMultipartForm() {
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res
                        .multipart("1", getClass().getResourceAsStream("/logback.xml"), new FormDataBodyPart("p2", "2")));

        assertThat(info.getFormParams()).isNotEmpty()
                .containsEntry("p1", "1")
                .containsEntry("p2", "2");
        assertThat(info.getConsumes()).isNotEmpty()
                .contains(MediaType.MULTIPART_FORM_DATA);
    }

    @Test
    void testMultipartForm2() {
        InputStream file = getClass().getResourceAsStream("/logback.xml");
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res
                        .multipart2("1", file, new FormDataContentDisposition("form-data; name=\"file\"; filename=\"filename.jpg\"")));

        assertThat(info.getFormParams()).isNotEmpty()
                .containsEntry("p1", "1")
                .extracting(map -> map.get("file"))
                .isInstanceOf(StreamDataBodyPart.class)
                .asInstanceOf(InstanceOfAssertFactories.type(StreamDataBodyPart.class))
                .extracting(StreamDataBodyPart::getStreamEntity, StreamDataBodyPart::getFilename)
                .containsExactly(file, "filename.jpg");
        assertThat(info.getConsumes()).isNotEmpty()
                .contains(MediaType.MULTIPART_FORM_DATA);


        info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res
                        .multipart2("1", file, null)); //no file name

        assertThat(info.getFormParams()).isNotEmpty()
                .containsEntry("p1", "1")
                .containsEntry("file", file);
        assertThat(info.getConsumes()).isNotEmpty()
                .contains(MediaType.MULTIPART_FORM_DATA);
    }

    @Test
    void testDateInForm() {
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(FormResource.class, res -> res
                        .post("1", new Date()));

        assertThat(info.getFormParams()).isNotEmpty()
                .containsEntry("name", "1");
        assertThat(info.getFormParams().get("date")).isInstanceOf(Date.class);
        assertThat(info.getConsumes()).isNotEmpty()
                .contains(MediaType.APPLICATION_FORM_URLENCODED);
    }

    @Test
    void testMapInForm() {
        final MultivaluedHashMap<String, Object> map = new MultivaluedHashMap<>();
        map.add("name", "1");
        map.add("date", new Date());
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(FormResource.class, res -> res.post2(map));

        assertThat(info.getFormParams()).isNotEmpty()
                .containsEntry("name", "1");
        assertThat(info.getFormParams().get("date")).isInstanceOf(Date.class);
        assertThat(info.getConsumes()).isNotEmpty()
                .contains(MediaType.APPLICATION_FORM_URLENCODED);
    }

    @Test
    void testListInForm() {
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(FormResource.class, res -> res
                        .postMulti(Arrays.asList("1", "2"), new Date()));

        assertThat(info.getFormParams()).isNotEmpty()
                .containsEntry("name", Arrays.asList("1", "2"));
        assertThat(info.getFormParams().get("date")).isInstanceOf(Date.class);
        assertThat(info.getConsumes()).isNotEmpty()
                .contains(MediaType.APPLICATION_FORM_URLENCODED);
    }

    @Test
    void testMultiDisposition() {
        final MultipartArgumentHelper helper = new MultipartArgumentHelper();
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(FormResource.class, res -> res
                        .multipartMulti2(Arrays.asList(
                                        helper.disposition("file", "logback.xml"),
                                        helper.disposition("file", "file.txt")),
                                Arrays.asList(
                                        helper.fromClasspath("/logback.xml"),
                                        helper.fromClasspath("/file.txt")
                                )));

        assertThat(info.getFormParams()).isNotEmpty().hasSize(1);
        assertThat(info.getFormParams().get("file")).isInstanceOf(List.class);
        assertThat(info.getConsumes()).isNotEmpty()
                .contains(MediaType.MULTIPART_FORM_DATA);
    }

    @Test
    void testGenericMultipart() {
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(FormResource.class, res -> res
                        .multipartGeneric(new MultipartArgumentHelper().multipart()
                                .field("foo", "bar")
                                .stream("file", "/logback.xml")
                                .build()));

        assertThat(info.getFormParams()).hasSize(2);
        assertThat(info.getFormParams()).isNotEmpty()
                .containsEntry("foo", "bar");
        assertThat(info.getFormParams().get("file")).isInstanceOf(StreamDataBodyPart.class);
        assertThat(info.getConsumes()).isNotEmpty()
                .contains(MediaType.MULTIPART_FORM_DATA);
    }

    @Test
    void testMappedCookie() {
        final Cookie cookie = new Cookie.Builder("cc").value("tt").build();
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res
                        .cookie(cookie));

        assertThat(info.getCookieParams()).isNotEmpty()
                .containsEntry("cc", cookie);
    }

    @Test
    void testAnnotated() {
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(InterfaceResource.class, res -> res.get(null, null, null, null));

        assertThat(info.getResource()).isEqualTo(ResourceDeclaration.class);
        assertThat(info.getMethod().getDeclaringClass()).isEqualTo(ResourceDeclaration.class);
    }

    @Test
    void testErrors() {
        assertThatThrownBy(() -> ResourceAnalyzer.analyzeMethodCall(null, res -> {}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Proxied class is required");

        assertThatThrownBy(() -> ResourceAnalyzer.analyzeMethodCall(DirectResource.class, res -> {}))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No method calls recorded");

        assertThatThrownBy(() -> ResourceAnalyzer.analyzeMethodCall(DirectResource.class, res -> {
            throw new IllegalStateException("fail");
        }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to record method call on resource 'DirectResource'");

        assertThatThrownBy(() -> ResourceAnalyzer.findMethod(DirectResource.class, "unknown"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Method 'unknown' not found in class 'DirectResource'");

        assertThatThrownBy(() -> ResourceAnalyzer.findMethod(DirectResource.class, "unknown", String.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Method 'unknown(String)' not found in class 'DirectResource'");

        assertThatThrownBy(() -> ResourceAnalyzer.findMethod(DirectResource.class, "get"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Method with name 'get' is not unique in class 'DirectResource'")
                .hasMessageContaining("Response get(MappedBean)")
                .hasMessageContaining("Response get(String, String, String, String, String)");

        assertThatThrownBy(() -> ResourceAnalyzer.findHttpMethod(RootResource.class.getMethod("sub1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Http method type annotation was not found on resource method: SubResource1 sub1()");
    }
}
