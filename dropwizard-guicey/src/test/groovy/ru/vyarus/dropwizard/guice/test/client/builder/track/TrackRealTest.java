package ru.vyarus.dropwizard.guice.test.client.builder.track;

import com.google.common.collect.ImmutableMap;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
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
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Vyacheslav Rusakov
 * @since 06.10.2025
 */
@TestDropwizardApp(value = TrackRealTest.App.class, randomPorts = true)
public class TrackRealTest {

    @Test
    void testTargetMethods(ClientSupport client) {

        final RequestTracker tracker = new RequestTracker();
        final WebTarget target = tracker.track(client.target("/"));
        target.path("root")
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

        assertThat(tracker.getPaths()).containsOnly("root");
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
                
                	Path                                      at r.v.d.g.t.c.b.track.(TrackRealTest.java:46)
                		root
                
                	Resolve template                          at r.v.d.g.t.c.b.track.(TrackRealTest.java:47)
                		(encodeSlashInPath=false encoded=false)
                		name=nm
                
                	Resolve template                          at r.v.d.g.t.c.b.track.(TrackRealTest.java:48)
                		(encodeSlashInPath=true encoded=false)
                		name2=vv//vv
                
                	Resolve template                          at r.v.d.g.t.c.b.track.(TrackRealTest.java:49)
                		(encodeSlashInPath=false encoded=true)
                		name3=3
                
                	Resolve template                          at r.v.d.g.t.c.b.track.(TrackRealTest.java:50)
                		(encodeSlashInPath=false encoded=false)
                		name4=4
                
                	Resolve template                          at r.v.d.g.t.c.b.track.(TrackRealTest.java:51)
                		(encodeSlashInPath=true encoded=false)
                		name5=5
                
                	Resolve template                          at r.v.d.g.t.c.b.track.(TrackRealTest.java:52)
                		(encodeSlashInPath=false encoded=true)
                		name6=6
                
                	Matrix param                              at r.v.d.g.t.c.b.track.(TrackRealTest.java:53)
                		mx=1
                
                	Matrix param                              at r.v.d.g.t.c.b.track.(TrackRealTest.java:54)
                		mx2=[1, 2]
                
                	Query param                               at r.v.d.g.t.c.b.track.(TrackRealTest.java:55)
                		qq=qq
                
                	Query param                               at r.v.d.g.t.c.b.track.(TrackRealTest.java:56)
                		qq2=[1, 2]
                
                	Property                                  at r.v.d.g.t.c.b.track.(TrackRealTest.java:57)
                		foo=bar
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackRealTest.java:58)
                		Ext1                         (r.v.d.g.t.c.b.t.TrackRealTest)\s
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackRealTest.java:59)
                		Ext2                         (r.v.d.g.t.c.b.t.TrackRealTest)\s
                			priority=10
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackRealTest.java:60)
                		Ext3                         (r.v.d.g.t.c.b.t.TrackRealTest)\s
                			 contracts=
                				Contr1                       (r.v.d.g.t.c.b.t.TrackRealTest)\s
                				Contr2                       (r.v.d.g.t.c.b.t.TrackRealTest)\s
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackRealTest.java:61)
                		Ext4                         (r.v.d.g.t.c.b.t.TrackRealTest)\s
                			contracts=
                				Contr1                       (r.v.d.g.t.c.b.t.TrackRealTest) =11
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackRealTest.java:62)
                		Ext5                         (r.v.d.g.t.c.b.t.TrackRealTest)\s
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackRealTest.java:63)
                		Ext6                         (r.v.d.g.t.c.b.t.TrackRealTest)\s
                			priority=10
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackRealTest.java:64)
                		Ext7                         (r.v.d.g.t.c.b.t.TrackRealTest)\s
                			 contracts=
                				Contr1                       (r.v.d.g.t.c.b.t.TrackRealTest)\s
                				Contr2                       (r.v.d.g.t.c.b.t.TrackRealTest)\s
                
                	Register                                  at r.v.d.g.t.c.b.track.(TrackRealTest.java:65)
                		Ext8                         (r.v.d.g.t.c.b.t.TrackRealTest)\s
                			contracts=
                				Contr1                       (r.v.d.g.t.c.b.t.TrackRealTest) =11
                
                """);


        assertThat(target.getUriBuilder()).isNotNull();
        assertThat(target.getConfiguration()).isNotNull();
        assertThat(target.getUri().toString()).isEqualTo("http://localhost:"+client.getPort() + "/root;mx=1;mx2=1;mx2=2?qq=qq&qq2=1&qq2=2");

        assertThat(target.request()).isNotNull();

        assertThat(target.request(MediaType.TEXT_PLAIN)).isNotNull();
        assertThat(tracker.getAcceptHeader()).containsOnly(MediaType.TEXT_PLAIN);

        assertThat(target.request(MediaType.APPLICATION_JSON_TYPE)).isNotNull();
        assertThat(tracker.getAcceptHeader()).containsOnly(MediaType.APPLICATION_JSON);

        assertThat(tracker.getUrl()).isEqualTo("http://localhost:"+client.getPort() + "/root;mx=1;mx2=1;mx2=2?qq=qq&qq2=1&qq2=2");
    }

    @Test
    void testBuilderMethods(ClientSupport client) {
        final RequestTracker tracker = new RequestTracker();
        final Invocation.Builder builder = tracker.track(client.targetRest("root")).request();

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
                
                	Property                                  at r.v.d.g.t.c.b.track.(TrackRealTest.java:198)
                		foo=bar
                
                	Accept                                    at r.v.d.g.t.c.b.track.(TrackRealTest.java:199)
                		[text/plain]
                
                	Accept                                    at r.v.d.g.t.c.b.track.(TrackRealTest.java:200)
                		[application/json]
                
                	Accept Language                           at r.v.d.g.t.c.b.track.(TrackRealTest.java:201)
                		[EN]
                
                	Accept Language                           at r.v.d.g.t.c.b.track.(TrackRealTest.java:202)
                		[en_CA]
                
                	Accept Encoding                           at r.v.d.g.t.c.b.track.(TrackRealTest.java:203)
                		[gzip]
                
                	Cookie                                    at r.v.d.g.t.c.b.track.(TrackRealTest.java:204)
                		$Version=1;c1=1
                
                	Cookie                                    at r.v.d.g.t.c.b.track.(TrackRealTest.java:205)
                		$Version=1;c2=2
                
                	Cache                                     at r.v.d.g.t.c.b.track.(TrackRealTest.java:206)
                		must-revalidate, max-age=604800
                
                	Header                                    at r.v.d.g.t.c.b.track.(TrackRealTest.java:208)
                		h1=1
                
                	Headers                                   at r.v.d.g.t.c.b.track.(TrackRealTest.java:209)
                		h2=[2]
                		h3=[3]
                
                """);

        builder.build("GET");
        verifyMethod(tracker, "GET", null);


        builder.buildGet();
        verifyMethod(tracker, "GET", null);

        assertThat(builder.get()).isNotNull();
        verifyMethod(tracker, "GET", null);

        assertThat(builder.get(Integer.class)).isNotNull();
        verifyMethod(tracker, "GET", null);

        assertThat(builder.get(new GenericType<Integer>() {})).isNotNull();
        verifyMethod(tracker, "GET", null);


        builder.buildDelete();
        verifyMethod(tracker, "DELETE", null);

        assertThat(builder.delete()).isNotNull();
        verifyMethod(tracker, "DELETE", null);

        assertThat(builder.delete(Integer.class)).isNotNull();
        verifyMethod(tracker, "DELETE", null);

        assertThat(builder.delete(new GenericType<Integer>() {})).isNotNull();
        verifyMethod(tracker, "DELETE", null);


        assertThat(builder.head()).isNotNull();
        verifyMethod(tracker, "HEAD", null);


        assertThat(builder.options()).isNotNull();
        verifyMethod(tracker, "OPTIONS", null);

        assertThat(builder.options(Integer.class)).isNotNull();
        verifyMethod(tracker, "OPTIONS", null);

        assertThat(builder.options(new GenericType<Integer>() {})).isNotNull();
        verifyMethod(tracker, "OPTIONS", null);


        assertThat(builder.trace()).isNotNull();
        verifyMethod(tracker, "TRACE", null);

        assertThatThrownBy(() -> builder.trace(Integer.class));
        verifyMethod(tracker, "TRACE", null);

        assertThatThrownBy(() ->builder.trace(new GenericType<Integer>() {}));
        verifyMethod(tracker, "TRACE", null);


        assertThat(builder.method("TRACE")).isNotNull();
        verifyMethod(tracker, "TRACE", null);

        assertThat(builder.method("GET", Integer.class)).isNotNull();
        verifyMethod(tracker, "GET", null);

        assertThat(builder.method("GET", new GenericType<Integer>() {})).isNotNull();
        verifyMethod(tracker, "GET", null);




        builder.build("POST", Entity.text("test"));
        verifyMethod(tracker, "POST", Entity.text("test"));

        builder.buildPost(Entity.text("test"));
        verifyMethod(tracker, "POST", Entity.text("test"));

        assertThat(builder.post(Entity.text("test"))).isNotNull();
        verifyMethod(tracker, "POST", Entity.text("test"));

        assertThat(builder.post(Entity.text("test"), Integer.class)).isNotNull();
        verifyMethod(tracker, "POST", Entity.text("test"));

        assertThat(builder.post(Entity.text("test"), new GenericType<Integer>() {})).isNotNull();
        verifyMethod(tracker, "POST", Entity.text("test"));



        builder.buildPut(Entity.text("test"));
        verifyMethod(tracker, "PUT", Entity.text("test"));

        assertThat(builder.put(Entity.text("test"))).isNotNull();
        verifyMethod(tracker, "PUT", Entity.text("test"));

        assertThat(builder.put(Entity.text("test"), Integer.class)).isNotNull();
        verifyMethod(tracker, "PUT", Entity.text("test"));

        assertThat(builder.put(Entity.text("test"), new GenericType<Integer>() {})).isNotNull();
        verifyMethod(tracker, "PUT", Entity.text("test"));


        assertThat(builder.method("POST", Entity.text("test"))).isNotNull();
        verifyMethod(tracker, "POST", Entity.text("test"));

        assertThat(builder.method("POST", Entity.text("test"), Integer.class)).isNotNull();
        verifyMethod(tracker, "POST", Entity.text("test"));

        assertThat(builder.method("POST", Entity.text("test"), new GenericType<Integer>() {})).isNotNull();
        verifyMethod(tracker, "POST", Entity.text("test"));


        assertThat(builder.async()).isNotNull();
        assertThat(builder.rx()).isNotNull();
        assertThat(builder.rx(JerseyCompletionStageRxInvoker.class)).isNotNull();
    }

    @Test
    void testTrackerLookup(ClientSupport client) {
        RequestTracker tracker = new RequestTracker();
        final WebTarget target = tracker.track(client.target("/"));

        assertThat(RequestTracker.lookupTracker(target).get()).isEqualTo(tracker);
        assertThat(RequestTracker.lookupTracker(target.request()).get()).isEqualTo(tracker);
    }

    private void verifyMethod(RequestTracker tracker, String method, Entity<?> entity) {
        assertThat(tracker.getHttpMethod()).isEqualTo(method);
        if (entity == null) {
            assertThat(tracker.getEntity()).isNull();
        } else {
            assertThat(tracker.getEntity()).isEqualTo(entity);
        }
    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(Resource.class)
                    .build();
        }
    }

    @Path("/root")
    public static class Resource {

        @GET
        @Path("/")
        public int get() {
            return 1;
        }

        @POST
        @Path("/")
        public int post(String text) {
            return 1;
        }

        @PUT
        @Path("/")
        public int put(String text) {
            return 1;
        }

        @DELETE
        @Path("/")
        public int delete() {
            return 1;
        }

        @OPTIONS
        @Path("/")
        public int post() {
            return 1;
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
