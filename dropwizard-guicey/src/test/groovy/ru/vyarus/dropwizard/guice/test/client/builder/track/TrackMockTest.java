package ru.vyarus.dropwizard.guice.test.client.builder.track;

import com.google.common.collect.ImmutableMap;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.glassfish.jersey.client.JerseyCompletionStageRxInvoker;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 06.10.2025
 */
public class TrackMockTest {

    @Test
    void testTargetMethods() {

        final RequestTracker tracker = new RequestTracker();
        final WebTarget target = tracker.track();
        target.path("some/{name}")
                .resolveTemplate("name", "nm")
                .resolveTemplate("name2", "vv//vv", true)
                .resolveTemplateFromEncoded("name3", "3")
                .resolveTemplates(ImmutableMap.of("name4", "4"))
                .resolveTemplates(ImmutableMap.of("name5", "5"), true)
                .resolveTemplatesFromEncoded(ImmutableMap.of("name6", "6"))
                .matrixParam("mx", "1")
                .matrixParam("mx2", "1", "2")
                .queryParam("qq", "qq")
                .queryParam("qq2", "1", "2")
                .property("foo", "bar")
                .register(Ext1.class)
                .register(Ext2.class, 10)
                .register(Ext3.class, Contr1.class, Contr2.class)
                .register(Ext4.class, ImmutableMap.of(Contr1.class, 11))
                .register(new Ext5())
                .register(new Ext6(), 10)
                .register(new Ext7(), Contr1.class, Contr2.class)
                .register(new Ext8(), ImmutableMap.of(Contr1.class, 11));

        assertThat(tracker.getPaths()).containsOnly("some/{name}");
        assertThat(tracker.getPathParams()).hasSize(6)
                .containsEntry("name", "nm")
                .containsEntry("name2", "vv//vv")
                .containsEntry("name3", "3")
                .containsEntry("name4", "4")
                .containsEntry("name5", "5")
                .containsEntry("name6", "6");
        assertThat(tracker.getRawData().getPathParams().get(1).get())
                        .extracting("name", "value", "encodeSlashInPath", "encoded")
                        .containsExactly("name2", "vv//vv", true, false);
        assertThat(tracker.getMatrixParams())
                .hasSize(2)
                .containsEntry("mx", "1")
                .containsEntry("mx2", new Object[]{"1", "2"});
        assertThat(tracker.getQueryParams())
                .hasSize(2)
                .containsEntry("qq", "qq")
                .containsEntry("qq2", new Object[]{"1", "2"});
        assertThat(tracker.getProperties()).hasSize(1)
                .containsEntry("foo", "bar");
        assertThat(tracker.getExtensions()).hasSize(8)
                .containsKeys(Ext1.class, Ext2.class, Ext3.class, Ext4.class, Ext5.class, Ext6.class, Ext7.class, Ext8.class);
        assertThat(tracker.getRawData().getExtensions().get(Ext3.class).get())
                .extracting("type", "value", "contracts")
                .containsExactly(Ext3.class, Ext3.class, ImmutableMap.of(Contr1.class, -1, Contr2.class, -1));
        assertThat(tracker.getUrl()).isNull();

        assertThat(tracker.getLog()).isEqualTo("""
                
                	Path                                      at r.v.d.g.t.c.b.track.(TrackMockTest.java:32)
                		some/{name}
                
                	Resolve template                          at r.v.d.g.t.c.b.track.(TrackMockTest.java:33)
                		(encodeSlashInPath=false encoded=false)
                		name=nm
                
                	Resolve template                          at r.v.d.g.t.c.b.track.(TrackMockTest.java:34)
                		(encodeSlashInPath=true encoded=false)
                		name2=vv//vv
                
                	Resolve template                          at r.v.d.g.t.c.b.track.(TrackMockTest.java:35)
                		(encodeSlashInPath=false encoded=true)
                		name3=3
                
                	Resolve template                          at r.v.d.g.t.c.b.track.(TrackMockTest.java:36)
                		(encodeSlashInPath=false encoded=false)
                		name4=4
                
                	Resolve template                          at r.v.d.g.t.c.b.track.(TrackMockTest.java:37)
                		(encodeSlashInPath=true encoded=false)
                		name5=5
                
                	Resolve template                          at r.v.d.g.t.c.b.track.(TrackMockTest.java:38)
                		(encodeSlashInPath=false encoded=true)
                		name6=6
                
                	Matrix param                              at r.v.d.g.t.c.b.track.(TrackMockTest.java:39)
                		mx=1
                
                	Matrix param                              at r.v.d.g.t.c.b.track.(TrackMockTest.java:40)
                		mx2=[1, 2]
                
                	Query param                               at r.v.d.g.t.c.b.track.(TrackMockTest.java:41)
                		qq=qq
                
                	Query param                               at r.v.d.g.t.c.b.track.(TrackMockTest.java:42)
                		qq2=[1, 2]
                
                	Property                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:43)
                		foo=bar
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:44)
                		Ext1                         (r.v.d.g.t.c.b.t.TrackMockTest)\s
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:45)
                		Ext2                         (r.v.d.g.t.c.b.t.TrackMockTest)\s
                			priority=10
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:46)
                		Ext3                         (r.v.d.g.t.c.b.t.TrackMockTest)\s
                			 contracts=
                				Contr1                       (r.v.d.g.t.c.b.t.TrackMockTest)\s
                				Contr2                       (r.v.d.g.t.c.b.t.TrackMockTest)\s
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:47)
                		Ext4                         (r.v.d.g.t.c.b.t.TrackMockTest)\s
                			contracts=
                				Contr1                       (r.v.d.g.t.c.b.t.TrackMockTest) =11
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:48)
                		Ext5                         (r.v.d.g.t.c.b.t.TrackMockTest)\s
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:49)
                		Ext6                         (r.v.d.g.t.c.b.t.TrackMockTest)\s
                			priority=10
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:50)
                		Ext7                         (r.v.d.g.t.c.b.t.TrackMockTest)\s
                			 contracts=
                				Contr1                       (r.v.d.g.t.c.b.t.TrackMockTest)\s
                				Contr2                       (r.v.d.g.t.c.b.t.TrackMockTest)\s
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:51)
                		Ext8                         (r.v.d.g.t.c.b.t.TrackMockTest)\s
                			contracts=
                				Contr1                       (r.v.d.g.t.c.b.t.TrackMockTest) =11
                
                """);


        assertThat(target.getUriBuilder()).isNotNull();
        assertThat(target.getConfiguration()).isNull();
        assertThat(target.getUri().toString()).isEqualTo("some/nm");

        assertThat(target.request()).isNotNull();

        assertThat(target.request(MediaType.TEXT_PLAIN)).isNotNull();
        assertThat(tracker.getAcceptHeader()).containsOnly(MediaType.TEXT_PLAIN);

        assertThat(target.request(MediaType.APPLICATION_JSON_TYPE)).isNotNull();
        assertThat(tracker.getAcceptHeader()).containsOnly(MediaType.APPLICATION_JSON);

        assertThat(tracker.getUrl()).isEqualTo("some/nm");
    }

    @Test
    void testBuilderMethods() {
        final RequestTracker tracker = new RequestTracker();
        final Invocation.Builder builder = tracker.track().path("some/nm").request();

        builder.property("foo", "bar")
                .accept(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .acceptLanguage("EN")
                .acceptLanguage(Locale.CANADA)
                .acceptEncoding("gzip")
                .cookie("c1", "1")
                .cookie(new NewCookie.Builder("c2").value("2").build())
                .cacheControl(RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class)
                        .fromString("max-age=604800, must-revalidate"))
                .header("h1", "1")
                .headers(new MultivaluedHashMap<>(ImmutableMap.<String, Object>of("h2", "2", "h3", "3")));

        assertThat(tracker.getProperties()).hasSize(1)
                .containsEntry("foo", "bar");
        assertThat(tracker.getAcceptHeader()).hasSize(1)
                .containsOnly(MediaType.APPLICATION_JSON_TYPE.toString());
        assertThat(tracker.getLanguageHeader()).hasSize(1)
                .containsOnly(Locale.CANADA.toString());
        assertThat(tracker.getEncodingHeader()).hasSize(1)
                .containsOnly("gzip");
        assertThat(tracker.getCookies()).hasSize(2)
                .containsEntry("c1", new NewCookie.Builder("c1").value("1").build())
                .containsEntry("c2", new NewCookie.Builder("c2").value("2").build());
        assertThat(tracker.getCacheHeader()).isEqualTo("must-revalidate, max-age=604800");
        assertThat(tracker.getHeaders()).hasSize(3)
                .containsEntry("h1", "1")
                .containsEntry("h2", "2")
                .containsEntry("h3", "3");

        assertThat(tracker.getLog()).isEqualTo("""
                
                	Path                                      at r.v.d.g.t.c.b.track.(TrackMockTest.java:182)
                		some/nm
                
                	Property                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:184)
                		foo=bar
                
                	Accept                                    at r.v.d.g.t.c.b.track.(TrackMockTest.java:185)
                		[text/plain]
                
                	Accept                                    at r.v.d.g.t.c.b.track.(TrackMockTest.java:186)
                		[application/json]
                
                	Accept Language                           at r.v.d.g.t.c.b.track.(TrackMockTest.java:187)
                		[EN]
                
                	Accept Language                           at r.v.d.g.t.c.b.track.(TrackMockTest.java:188)
                		[en_CA]
                
                	Accept Encoding                           at r.v.d.g.t.c.b.track.(TrackMockTest.java:189)
                		[gzip]
                
                	Cookie                                    at r.v.d.g.t.c.b.track.(TrackMockTest.java:190)
                		$Version=1;c1=1
                
                	Cookie                                    at r.v.d.g.t.c.b.track.(TrackMockTest.java:191)
                		$Version=1;c2=2
                
                	Cache                                     at r.v.d.g.t.c.b.track.(TrackMockTest.java:192)
                		must-revalidate, max-age=604800
                
                	Header                                    at r.v.d.g.t.c.b.track.(TrackMockTest.java:194)
                		h1=1
                
                	Headers                                   at r.v.d.g.t.c.b.track.(TrackMockTest.java:195)
                		h2=[2]
                		h3=[3]
                
                """);

        builder.build("GET");
        verifyMethod(tracker, "GET", null, null);


        builder.buildGet();
        verifyMethod(tracker, "GET", null, null);

        assertThat(builder.get()).isNull();
        verifyMethod(tracker, "GET", null, null);

        assertThat(builder.get(Integer.class)).isNull();
        verifyMethod(tracker, "GET", null, Integer.class);

        assertThat(builder.get(new GenericType<Integer>() {})).isNull();
        verifyMethod(tracker, "GET", null, Integer.class);



        builder.build("POST", Entity.text("test"));
        verifyMethod(tracker, "POST", Entity.text("test"), null);

        builder.buildPost(Entity.text("test"));
        verifyMethod(tracker, "POST", Entity.text("test"), null);

        assertThat(builder.post(Entity.text("test"))).isNull();
        verifyMethod(tracker, "POST", Entity.text("test"), null);

        assertThat(builder.post(Entity.text("test"), Integer.class)).isNull();
        verifyMethod(tracker, "POST", Entity.text("test"), Integer.class);

        assertThat(builder.post(Entity.text("test"), new GenericType<Integer>() {})).isNull();
        verifyMethod(tracker, "POST", Entity.text("test"), Integer.class);


        builder.buildDelete();
        verifyMethod(tracker, "DELETE", null, null);

        assertThat(builder.delete()).isNull();
        verifyMethod(tracker, "DELETE", null, null);

        assertThat(builder.delete(Integer.class)).isNull();
        verifyMethod(tracker, "DELETE", null, Integer.class);

        assertThat(builder.delete(new GenericType<Integer>() {})).isNull();
        verifyMethod(tracker, "DELETE", null, Integer.class);



        builder.buildPut(Entity.text("test"));
        verifyMethod(tracker, "PUT", Entity.text("test"), null);

        assertThat(builder.put(Entity.text("test"))).isNull();
        verifyMethod(tracker, "PUT", Entity.text("test"), null);

        assertThat(builder.put(Entity.text("test"), Integer.class)).isNull();
        verifyMethod(tracker, "PUT", Entity.text("test"), Integer.class);

        assertThat(builder.put(Entity.text("test"), new GenericType<Integer>() {})).isNull();
        verifyMethod(tracker, "PUT", Entity.text("test"), Integer.class);


        assertThat(builder.head()).isNull();
        verifyMethod(tracker, "HEAD", null, null);


        assertThat(builder.options()).isNull();
        verifyMethod(tracker, "OPTIONS", null, null);

        assertThat(builder.options(Integer.class)).isNull();
        verifyMethod(tracker, "OPTIONS", null, Integer.class);

        assertThat(builder.options(new GenericType<Integer>() {})).isNull();
        verifyMethod(tracker, "OPTIONS", null, Integer.class);


        assertThat(builder.trace()).isNull();
        verifyMethod(tracker, "TRACE", null, null);

        assertThat(builder.trace(Integer.class)).isNull();
        verifyMethod(tracker, "TRACE", null, Integer.class);

        assertThat(builder.trace(new GenericType<Integer>() {})).isNull();
        verifyMethod(tracker, "TRACE", null, Integer.class);


        assertThat(builder.method("TRACE")).isNull();
        verifyMethod(tracker, "TRACE", null, null);

        assertThat(builder.method("GET", Integer.class)).isNull();
        verifyMethod(tracker, "GET", null, Integer.class);

        assertThat(builder.method("GET", new GenericType<Integer>() {})).isNull();
        verifyMethod(tracker, "GET", null, Integer.class);

        assertThat(builder.method("POST", Entity.text("test"))).isNull();
        verifyMethod(tracker, "POST", Entity.text("test"), null);

        assertThat(builder.method("POST", Entity.text("test"), Integer.class)).isNull();
        verifyMethod(tracker, "POST", Entity.text("test"), Integer.class);

        assertThat(builder.method("POST", Entity.text("test"), new GenericType<Integer>() {})).isNull();
        verifyMethod(tracker, "POST", Entity.text("test"), Integer.class);


        assertThat(builder.async()).isNull();
        assertThat(builder.rx()).isNull();
        assertThat(builder.rx(JerseyCompletionStageRxInvoker.class)).isNull();
    }

    @Test
    void testTrackerLookup() {
        RequestTracker tracker = new RequestTracker();
        final WebTarget target = tracker.track();

        assertThat(RequestTracker.lookupTracker(target).get()).isEqualTo(tracker);
        assertThat(RequestTracker.lookupTracker(target.request()).get()).isEqualTo(tracker);
    }

    private void verifyMethod(RequestTracker tracker, String method, Entity<?> entity, Class<?> result) {
        assertThat(tracker.getHttpMethod()).isEqualTo(method);
        if (entity == null) {
            assertThat(tracker.getEntity()).isNull();
        } else {
            assertThat(tracker.getEntity()).isEqualTo(entity);
        }
        if (result != null) {
            assertThat(tracker.getResultMappingClass()).isEqualTo(result);
        } else {
            assertThat(tracker.getResultMapping()).isNull();
        }
    }

    public static class Ext1 {}

    public static class Ext2 {}

    public static class Ext3 {}

    public static class Ext4 {}

    public static class Ext5 {}

    public static class Ext6 {}

    public static class Ext7 {}

    public static class Ext8 {}

    public static class Contr1 {}

    public static class Contr2 {}
}
