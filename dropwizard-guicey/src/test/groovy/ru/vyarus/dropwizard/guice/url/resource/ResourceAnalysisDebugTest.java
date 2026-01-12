package ru.vyarus.dropwizard.guice.url.resource;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.assertj.core.api.Assertions;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.client.builder.call.MultipartArgumentHelper;
import ru.vyarus.dropwizard.guice.url.model.ResourceMethodInfo;
import ru.vyarus.dropwizard.guice.url.resource.support.DirectResource;
import ru.vyarus.dropwizard.guice.url.resource.support.FormResource;
import ru.vyarus.dropwizard.guice.url.resource.support.MappedBean;
import ru.vyarus.dropwizard.guice.url.resource.support.sub.RootResource;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

import static ru.vyarus.dropwizard.guice.url.resource.ResourceAnalyzer.analyzeMethodCall;

/**
 * @author Vyacheslav Rusakov
 * @since 20.12.2025
 */
public class ResourceAnalysisDebugTest {

    @BeforeAll
    static void beforeAll() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    void testDebugReport() {
        expect(analyzeMethodCall(DirectResource.class, res -> res.get("1", "2", "3", "4", "5")),
                """
                        	GET /direct/{sm} (application/json)
                        	DirectResource.get(String, String, String, String, String):
                        		 1      PATH 	 sm = 1
                        		 2     QUERY 	 q = 2
                        		 3    HEADER 	 HH = 3
                        		 4    COOKIE 	 cc = 4
                        		 5    MATRIX 	 mm = 5
                        """);
    }

    @Test
    void testNulls() {
        expect(analyzeMethodCall(DirectResource.class, res -> res.get(null, null, null, null, null)),
                """
                        	GET /direct/{sm} (application/json)
                        	DirectResource.get(String, String, String, String, String):
                        """);
    }

    @Test
    void testChain() {
        expect(analyzeMethodCall(RootResource.class, res -> res.sub1().sub2("x").get("1")),
                """
                        	/root/sub1
                        	RootResource.sub1():
                        
                        		/sub2/{name} [consumes: text/plain]
                        		SubResource1.sub2(String):
                        			 1      PATH 	 name = x
                        
                        			GET /{sm} (application/json)
                        			SubResource2.get(String):
                        				 1      PATH 	 sm = 1
                        """);
    }

    @Test
    void testEntityRecognition() {
        final DirectResource.ModelType entity = new DirectResource.ModelType("test");
        expect(analyzeMethodCall(DirectResource.class, res -> res.post2(entity)),
                """
                        	POST /direct/entity
                        	DirectResource.post2(ModelType):
                        		 1    ENTITY 	  = ru.vyarus.dropwizard.guice.url.resource.support.DirectResource$ModelType@111111
                        """);
    }

    @Test
    void testEntityRecognition2() {
        final DirectResource.ModelType entity = new DirectResource.ModelType("test");
        expect(analyzeMethodCall(DirectResource.class, res -> res.post3(entity)),
                """
                        	POST /direct/entity2
                        	DirectResource.post3(ModelType):
                        		 1    ENTITY 	  = ru.vyarus.dropwizard.guice.url.resource.support.DirectResource$ModelType@111111
                        """);
    }

    @Test
    void testBeanParam() {
        MappedBean bean = new MappedBean();
        bean.setSm("1");
        bean.setQ("2");
        bean.setHh("3");
        bean.setCc("4");
        bean.setMm("5");

        expect(analyzeMethodCall(DirectResource.class, res -> res.get(bean)),
                """
                        	GET /direct/{sm}/2
                        	DirectResource.get(MappedBean):
                        		 1      PATH  (String sm)          	 sm = 1
                        		       QUERY  (String q)           	 q = 2
                        		      HEADER  (String hh)          	 HH = 3
                        		      COOKIE  (String cc)          	 cc = 4
                        		      MATRIX  (String mm)          	 mm = 5
                        """);
    }

    @Test
    void testForm() {
        expect(analyzeMethodCall(DirectResource.class, res -> res.form("1", 2)),
                """
                        	POST /direct/form [consumes: application/x-www-form-urlencoded]
                        	DirectResource.form(String, Integer):
                        		 1      FORM 	 p1 = 1
                        		 2      FORM 	 p2 = 2
                        """);
    }

    @Test
    void testMultipartForm() {
        expect(analyzeMethodCall(DirectResource.class, res ->
                        res.multipart("1", getClass().getResourceAsStream("/logback.xml"), new FormDataBodyPart("p2", "2"))),
                """
                        	POST /direct/multipart [consumes: multipart/form-data]
                        	DirectResource.multipart(String, InputStream, FormDataBodyPart):
                        		 1      FORM 	 p1 = 1
                        		 2      FORM 	 file1 = java.io.BufferedInputStream@111111
                        		 3      FORM 	 p2 = 2
                        """);
    }

    @Test
    void testMultipartForm2() {
        InputStream file = getClass().getResourceAsStream("/logback.xml");
        expect(analyzeMethodCall(DirectResource.class, res ->
                        res.multipart2("1", file, new FormDataContentDisposition("form-data; name=\"file\"; filename=\"filename.jpg\""))),
                """
                        	POST /direct/multipart [consumes: multipart/form-data]
                        	DirectResource.multipart2(String, InputStream, FormDataContentDisposition):
                        		 1      FORM 	 p1 = 1
                        		 2      FORM 	 file = org.glassfish.jersey.media.multipart.file.StreamDataBodyPart@111111
                        		 3      FORM 	 file = org.glassfish.jersey.media.multipart.file.StreamDataBodyPart@111111
                        """);
    }

    @Test
    void testDateInForm() {
        expect(analyzeMethodCall(FormResource.class, res ->
                        res.post("1", new SimpleDateFormat("dd/MM/yyyy").parse("12/11/2025"))),
                """
                        	POST /form/post [consumes: application/x-www-form-urlencoded]
                        	FormResource.post(String, Date):
                        		 1      FORM 	 name = 1
                        		 2      FORM 	 date = Wed Nov 12 00:00:00 UTC 2025
                        """);
    }

    @Test
    void testMapInForm() throws Exception{
        final MultivaluedHashMap<String, Object> map = new MultivaluedHashMap<>();
        map.add("name", "1");
        map.add("date", new SimpleDateFormat("dd/MM/yyyy").parse("12/11/2025"));
        expect(analyzeMethodCall(FormResource.class, res ->
                        res.post2(map)),
                """
                        	POST /form/post2 [consumes: application/x-www-form-urlencoded]
                        	FormResource.post2(MultivaluedMap<String, Object>):
                        		 1      FORM 	 date = Wed Nov 12 00:00:00 UTC 2025
                        		        FORM 	 name = 1
                        """);
    }

    @Test
    void testListInForm() {
        expect(analyzeMethodCall(FormResource.class, res ->
                        res.postMulti(Arrays.asList("1", "2"), new SimpleDateFormat("dd/MM/yyyy").parse("12/11/2025"))),
                """
                        	POST /form/postMulti [consumes: application/x-www-form-urlencoded]
                        	FormResource.postMulti(List<String>, Date):
                        		 1      FORM 	 name = [1, 2]
                        		 2      FORM 	 date = Wed Nov 12 00:00:00 UTC 2025
                        """);
    }

    @Test
    void testMultiDisposition() {
        final MultipartArgumentHelper helper = new MultipartArgumentHelper();
        expect(analyzeMethodCall(FormResource.class, res ->
                        res.multipartMulti2(Arrays.asList(
                                        helper.disposition("file", "logback.xml"),
                                        helper.disposition("file", "file.txt")),
                                Arrays.asList(
                                        helper.fromClasspath("/logback.xml"),
                                        helper.fromClasspath("/file.txt")
                                ))),
                """
                        	POST /form/multipartMulti2 [consumes: multipart/form-data]
                        	FormResource.multipartMulti2(List<FormDataContentDisposition>, List<InputStream>):
                        		 1      FORM 	 file = [org.glassfish.jersey.media.multipart.file.StreamDataBodyPart@111111, org.glassfish.jersey.media.multipart.file.StreamDataBodyPart@111111]
                        		 2      FORM 	 file = [org.glassfish.jersey.media.multipart.file.StreamDataBodyPart@111111, org.glassfish.jersey.media.multipart.file.StreamDataBodyPart@111111]
                        """);
    }

    @Test
    void testGenericMultipart() {
        expect(analyzeMethodCall(FormResource.class, res ->
                        res.multipartGeneric(new MultipartArgumentHelper().multipart()
                                .field("foo", "bar")
                                .stream("file", "/logback.xml")
                                .build())),
                """
                        	POST /form/multipartGeneric [consumes: multipart/form-data]
                        	FormResource.multipartGeneric(FormDataMultiPart):
                        		 1      FORM 	 file = org.glassfish.jersey.media.multipart.file.StreamDataBodyPart@111111
                        		        FORM 	 foo = bar
                        """);
    }

    @Test
    void testMappedCookie() {
        expect(analyzeMethodCall(DirectResource.class, res -> res.cookie(new Cookie.Builder("cc").value("tt").build())),
                """
                        	GET /direct/cookie
                        	DirectResource.cookie(Cookie):
                        		 1    COOKIE 	 cc = $Version=1;cc=tt
                        """);
    }

    void expect(ResourceMethodInfo info, String expected) {
        String report = info.getAnalysisReport();
        System.out.println(report);
        Assertions.assertThat(report.replace("\r", "").replaceAll("@[a-z\\d]+", "@111111").trim()).isEqualTo(expected.trim());
    }
}
