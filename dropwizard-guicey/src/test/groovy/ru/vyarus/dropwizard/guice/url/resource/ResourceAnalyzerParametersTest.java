package ru.vyarus.dropwizard.guice.url.resource;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.model.Parameter;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.client.builder.call.MultipartArgumentHelper;
import ru.vyarus.dropwizard.guice.url.model.ResourceMethodInfo;
import ru.vyarus.dropwizard.guice.url.model.param.BeanParameterSource;
import ru.vyarus.dropwizard.guice.url.model.param.DeclarationSource;
import ru.vyarus.dropwizard.guice.url.model.param.ParameterSource;
import ru.vyarus.dropwizard.guice.url.resource.support.DirectResource;
import ru.vyarus.dropwizard.guice.url.resource.support.FormResource;
import ru.vyarus.dropwizard.guice.url.resource.support.InterfaceResource;
import ru.vyarus.dropwizard.guice.url.resource.support.MappedBean;
import ru.vyarus.dropwizard.guice.url.resource.support.sub.RootResource;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 08.01.2026
 */
public class ResourceAnalyzerParametersTest {

    @Test
    void testMethodCallAnalysis() throws Exception {
        // WHEN analyze get
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res.get("1", "2", "3", "4", "5"));

        assertThat(info.selectParameterSources(source -> true)).hasSize(5);

        final ParameterSource param = info.getPathParamSource("sm");
        assertThat(param).isNotNull().isInstanceOf(ParameterSource.class).extracting(ParameterSource::getName)
                .isEqualTo("sm");
        assertThat(param.toString()).isEqualTo("PATH @PathParam(\"sm\") String");
        assertThat(param.getParameter()).isNotNull();
        assertThat(param.getValue()).isEqualTo("1");
        assertThat(param.getResource()).isEqualTo(DirectResource.class);
        assertThat(param.getMethod()).isEqualTo(DirectResource.class.getMethod("get", String.class, String.class, String.class, String.class, String.class ));
        assertThat(param.getArgumentPosition()).isEqualTo(0);
        assertThat(param.getUsedConverter()).isNull();
        assertThat(param.getType()).isEqualTo(Parameter.Source.PATH);
        assertThat(param.isBeanParam()).isFalse();
        assertThat(param.isSourceOnly()).isFalse();

        assertThat(info.getQueryParamSource("q")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("q");
        assertThat(info.getHeaderParamSource("HH")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("HH");
        assertThat(info.getCookieParamSource("cc")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("cc");
        assertThat(info.getMatrixParamSource("mm")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("mm");

        // WHEN null values provided
        info = ResourceAnalyzer.analyzeMethodCall(DirectResource.class, res -> res.get(null, null, null, null, null));
        assertThat(info.selectParameterSources(source -> true)).isEmpty();
    }

    @Test
    void testMethodCallChainAnalysis() throws Exception {
        // WHEN analyze get
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(RootResource.class, res -> res.sub1().sub2("x").get("1"));

        assertThat(info.selectParameterSources(source -> true)).hasSize(2);
        assertThat(info.getPathParamSource("name")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("name");
        assertThat(info.getPathParamSource("sm")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("sm");
    }

    @Test
    void testRawEntityRecognition() {
        // WHEN analyze simple post with entity
        final DirectResource.ModelType entity = new DirectResource.ModelType("test");
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res.post2(entity));

        assertThat(info.selectParameterSources(source -> true)).hasSize(1);
        assertThat(info.getEntityParamSource()).isNotNull().extracting(ParameterSource::getType)
                .isEqualTo(Parameter.Source.ENTITY);

        // WHEN analyze simple post with annotated
        info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res.post3(entity));

        assertThat(info.selectParameterSources(source -> true)).hasSize(1);
        assertThat(info.getEntityParamSource()).isNotNull().extracting(ParameterSource::getType)
                .isEqualTo(Parameter.Source.ENTITY);
    }

    @Test
    void testBeanParamMapping() throws Exception {
        MappedBean bean = new MappedBean();
        bean.setSm("1");
        bean.setQ("2");
        bean.setHh("3");
        bean.setCc("4");
        bean.setMm("5");

        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res.get(bean));

        assertThat(info.selectParameterSources(source -> true)).hasSize(5);

        assertThat(info.getPathParamSource("sm")).isNotNull().isInstanceOf(BeanParameterSource.class)
                .extracting(ParameterSource::getName).isEqualTo("sm");
        final BeanParameterSource param = (BeanParameterSource) info.getPathParamSource("sm");
        assertThat(param.toString()).isEqualTo("PATH @PathParam(\"sm\") String");
        assertThat(param.getParameter()).isNotNull();
        assertThat(param.getValue()).isEqualTo("1");
        assertThat(param.getResource()).isEqualTo(DirectResource.class);
        assertThat(param.getMethod()).isEqualTo(DirectResource.class.getMethod("get", MappedBean.class));
        assertThat(param.getArgumentPosition()).isEqualTo(0);
        assertThat(param.getUsedConverter()).isNull();
        assertThat(param.getType()).isEqualTo(Parameter.Source.PATH);
        assertThat(param.isBeanParam()).isTrue();
        assertThat(param.isSourceOnly()).isFalse();
        assertThat(param.getBeanClass()).isEqualTo(MappedBean.class);
        assertThat(param.getField()).isEqualTo(MappedBean.class.getDeclaredField("sm"));


        assertThat(info.getQueryParamSource("q")).isNotNull().isInstanceOf(BeanParameterSource.class)
                .extracting(ParameterSource::getName).isEqualTo("q");
        assertThat(info.getHeaderParamSource("HH")).isNotNull().isInstanceOf(BeanParameterSource.class)
                .extracting(ParameterSource::getName).isEqualTo("HH");
        assertThat(info.getCookieParamSource("cc")).isNotNull().isInstanceOf(BeanParameterSource.class)
                .extracting(ParameterSource::getName).isEqualTo("cc");
        assertThat(info.getMatrixParamSource("mm")).isNotNull().isInstanceOf(BeanParameterSource.class)
                .extracting(ParameterSource::getName).isEqualTo("mm");
    }

    @Test
    void testSimpleForm() {
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res.form("1", 2));

        assertThat(info.selectParameterSources(source -> true)).hasSize(2);
        assertThat(info.getFormParamSource("p1")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("p1");
        assertThat(info.getFormParamSource("p2")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("p2");
    }

    @Test
    void testMultipartForm() {
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res
                        .multipart("1", getClass().getResourceAsStream("/logback.xml"), new FormDataBodyPart("p2", "2")));

        assertThat(info.selectParameterSources(source -> true)).hasSize(3);
        assertThat(info.getFormParamSource("p1")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("p1");
        assertThat(info.getFormParamSource("file1")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("file1");
        assertThat(info.getFormParamSource("p2")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("p2");
    }

    @Test
    void testMultipartForm2() {
        InputStream file = getClass().getResourceAsStream("/logback.xml");
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res
                        .multipart2("1", file, new FormDataContentDisposition("form-data; name=\"file\"; filename=\"filename.jpg\"")));

        assertThat(info.selectParameterSources(source -> true)).hasSize(3);
        assertThat(info.getFormParamSource("p1")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("p1");
        assertThat(info.getFormParamSources("file")).hasSize(2);


        info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res
                        .multipart2("1", file, null)); //no file name

        assertThat(info.selectParameterSources(source -> true)).hasSize(2);
        assertThat(info.getFormParamSource("p1")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("p1");
        assertThat(info.getFormParamSources("file")).hasSize(1);
    }

    @Test
    void testDateInForm() {
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(FormResource.class, res -> res
                        .post("1", new Date()));

        assertThat(info.selectParameterSources(source -> true)).hasSize(2);
        assertThat(info.getFormParamSource("name")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("name");
        assertThat(info.getFormParamSource("date")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("date");
    }

    @Test
    void testMapInForm() {
        final MultivaluedHashMap<String, Object> map = new MultivaluedHashMap<>();
        map.add("name", "1");
        map.add("date", new Date());
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(FormResource.class, res -> res.post2(map));

        assertThat(info.selectParameterSources(source -> true)).hasSize(2);
        assertThat(info.getFormParamSource("name")).isNotNull().isInstanceOf(DeclarationSource.class)
                .extracting(ParameterSource::getName).isEqualTo("name");

        DeclarationSource source = (DeclarationSource) info.getFormParamSource("name");
        assertThat(source.toString()).isEqualTo("FORM \"name\" from MultivaluedMap");
        assertThat(source.isSourceOnly()).isTrue();
        assertThat(source.getArgumentPosition()).isEqualTo(0);

        assertThat(info.getFormParamSource("date")).isNotNull().isInstanceOf(DeclarationSource.class)
                .extracting(ParameterSource::getName).isEqualTo("date");
    }

    @Test
    void testListInForm() {
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(FormResource.class, res -> res
                        .postMulti(Arrays.asList("1", "2"), new Date()));

        assertThat(info.selectParameterSources(source -> true)).hasSize(2);
        assertThat(info.getFormParamSource("name")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("name");
        assertThat(info.getFormParamSource("date")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("date");
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

        // very special case, 2 arguments would be resolved as a single form parameter (with multiple values)
        // The type is DeclarationSource because jersey does not recognize it (UNKNOWN type) and so it can't be used
        // for parameters conversion
        assertThat(info.selectParameterSources(source -> true)).hasSize(2);
        assertThat(info.getFormParamSources("file")).hasSize(2)
                .allMatch(source -> source instanceof DeclarationSource);
    }

    @Test
    void testGenericMultipart() {
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(FormResource.class, res -> res
                        .multipartGeneric(new MultipartArgumentHelper().multipart()
                                .field("foo", "bar")
                                .stream("file", "/logback.xml")
                                .build()));

        assertThat(info.selectParameterSources(source -> true)).hasSize(2);
        assertThat(info.getFormParamSource("foo")).isNotNull().isInstanceOf(DeclarationSource.class)
                .extracting(ParameterSource::getName).isEqualTo("foo");
        assertThat(info.getFormParamSource("file")).isNotNull().isInstanceOf(DeclarationSource.class)
                .extracting(ParameterSource::getName).isEqualTo("file");
    }

    @Test
    void testMappedCookie() {
        final Cookie cookie = new Cookie.Builder("cc").value("tt").build();
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(DirectResource.class, res -> res
                        .cookie(cookie));

        assertThat(info.selectParameterSources(source -> true)).hasSize(1);
        assertThat(info.getCookieParamSource("cc")).isNotNull().extracting(ParameterSource::getName)
                .isEqualTo("cc");
    }

    @Test
    void testAnnotated() {
        ResourceMethodInfo info = ResourceAnalyzer
                .analyzeMethodCall(InterfaceResource.class, res -> res.get(null, null, null, null));

        assertThat(info.selectParameterSources(source -> true)).hasSize(0);
    }
}
