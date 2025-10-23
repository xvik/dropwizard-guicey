package ru.vyarus.dropwizard.guice.test.client.builder.track;

import com.google.common.collect.ImmutableMap;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.CacheControl;
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

        assertThat(tracker.getLog()).isEqualTo("\n" +
                "\tPath                                      at r.v.d.g.t.c.b.track.(TrackMockTest.java:31)\n" +
                "\t\tsome/{name}\n" +
                "\n" +
                "\tResolve template                          at r.v.d.g.t.c.b.track.(TrackMockTest.java:32)\n" +
                "\t\t(encodeSlashInPath=false encoded=false)\n" +
                "\t\tname=nm\n" +
                "\n" +
                "\tResolve template                          at r.v.d.g.t.c.b.track.(TrackMockTest.java:33)\n" +
                "\t\t(encodeSlashInPath=true encoded=false)\n" +
                "\t\tname2=vv//vv\n" +
                "\n" +
                "\tResolve template                          at r.v.d.g.t.c.b.track.(TrackMockTest.java:34)\n" +
                "\t\t(encodeSlashInPath=false encoded=true)\n" +
                "\t\tname3=3\n" +
                "\n" +
                "\tResolve template                          at r.v.d.g.t.c.b.track.(TrackMockTest.java:35)\n" +
                "\t\t(encodeSlashInPath=false encoded=false)\n" +
                "\t\tname4=4\n" +
                "\n" +
                "\tResolve template                          at r.v.d.g.t.c.b.track.(TrackMockTest.java:36)\n" +
                "\t\t(encodeSlashInPath=true encoded=false)\n" +
                "\t\tname5=5\n" +
                "\n" +
                "\tResolve template                          at r.v.d.g.t.c.b.track.(TrackMockTest.java:37)\n" +
                "\t\t(encodeSlashInPath=false encoded=true)\n" +
                "\t\tname6=6\n" +
                "\n" +
                "\tMatrix param                              at r.v.d.g.t.c.b.track.(TrackMockTest.java:38)\n" +
                "\t\tmx=1\n" +
                "\n" +
                "\tMatrix param                              at r.v.d.g.t.c.b.track.(TrackMockTest.java:39)\n" +
                "\t\tmx2=[1, 2]\n" +
                "\n" +
                "\tQuery param                               at r.v.d.g.t.c.b.track.(TrackMockTest.java:40)\n" +
                "\t\tqq=qq\n" +
                "\n" +
                "\tQuery param                               at r.v.d.g.t.c.b.track.(TrackMockTest.java:41)\n" +
                "\t\tqq2=[1, 2]\n" +
                "\n" +
                "\tProperty                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:42)\n" +
                "\t\tfoo=bar\n" +
                "\n" +
                "\tRegister                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:43)\n" +
                "\t\tExt1                         (r.v.d.g.t.c.b.t.TrackMockTest) \n" +
                "\n" +
                "\tRegister                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:44)\n" +
                "\t\tExt2                         (r.v.d.g.t.c.b.t.TrackMockTest) \n" +
                "\t\t\tpriority=10\n" +
                "\n" +
                "\tRegister                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:45)\n" +
                "\t\tExt3                         (r.v.d.g.t.c.b.t.TrackMockTest) \n" +
                "\t\t\t contracts=\n" +
                "\t\t\t\tContr1                       (r.v.d.g.t.c.b.t.TrackMockTest) \n" +
                "\t\t\t\tContr2                       (r.v.d.g.t.c.b.t.TrackMockTest) \n" +
                "\n" +
                "\tRegister                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:46)\n" +
                "\t\tExt4                         (r.v.d.g.t.c.b.t.TrackMockTest) \n" +
                "\t\t\tcontracts=\n" +
                "\t\t\t\tContr1                       (r.v.d.g.t.c.b.t.TrackMockTest) =11\n" +
                "\n" +
                "\tRegister                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:47)\n" +
                "\t\tExt5                         (r.v.d.g.t.c.b.t.TrackMockTest) \n" +
                "\n" +
                "\tRegister                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:48)\n" +
                "\t\tExt6                         (r.v.d.g.t.c.b.t.TrackMockTest) \n" +
                "\t\t\tpriority=10\n" +
                "\n" +
                "\tRegister                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:49)\n" +
                "\t\tExt7                         (r.v.d.g.t.c.b.t.TrackMockTest) \n" +
                "\t\t\t contracts=\n" +
                "\t\t\t\tContr1                       (r.v.d.g.t.c.b.t.TrackMockTest) \n" +
                "\t\t\t\tContr2                       (r.v.d.g.t.c.b.t.TrackMockTest) \n" +
                "\n" +
                "\tRegister                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:50)\n" +
                "\t\tExt8                         (r.v.d.g.t.c.b.t.TrackMockTest) \n" +
                "\t\t\tcontracts=\n" +
                "\t\t\t\tContr1                       (r.v.d.g.t.c.b.t.TrackMockTest) =11\n\n");


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
                .cookie(new NewCookie("c2", "2"))
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
                .containsEntry("c1", new NewCookie("c1", "1"))
                .containsEntry("c2", new NewCookie("c2", "2"));
        assertThat(tracker.getCacheHeader()).isEqualTo("must-revalidate, max-age=604800");
        assertThat(tracker.getHeaders()).hasSize(3)
                .containsEntry("h1", "1")
                .containsEntry("h2", "2")
                .containsEntry("h3", "3");

        assertThat(tracker.getLog()).isEqualTo("\n" +
                "\tPath                                      at r.v.d.g.t.c.b.track.(TrackMockTest.java:178)\n" +
                "\t\tsome/nm\n" +
                "\n" +
                "\tProperty                                  at r.v.d.g.t.c.b.track.(TrackMockTest.java:180)\n" +
                "\t\tfoo=bar\n" +
                "\n" +
                "\tAccept                                    at r.v.d.g.t.c.b.track.(TrackMockTest.java:181)\n" +
                "\t\t[text/plain]\n" +
                "\n" +
                "\tAccept                                    at r.v.d.g.t.c.b.track.(TrackMockTest.java:182)\n" +
                "\t\t[application/json]\n" +
                "\n" +
                "\tAccept Language                           at r.v.d.g.t.c.b.track.(TrackMockTest.java:183)\n" +
                "\t\t[EN]\n" +
                "\n" +
                "\tAccept Language                           at r.v.d.g.t.c.b.track.(TrackMockTest.java:184)\n" +
                "\t\t[en_CA]\n" +
                "\n" +
                "\tAccept Encoding                           at r.v.d.g.t.c.b.track.(TrackMockTest.java:185)\n" +
                "\t\t[gzip]\n" +
                "\n" +
                "\tCookie                                    at r.v.d.g.t.c.b.track.(TrackMockTest.java:186)\n" +
                "\t\t$Version=1;c1=1\n" +
                "\n" +
                "\tCookie                                    at r.v.d.g.t.c.b.track.(TrackMockTest.java:187)\n" +
                "\t\t$Version=1;c2=2\n" +
                "\n" +
                "\tCache                                     at r.v.d.g.t.c.b.track.(TrackMockTest.java:188)\n" +
                "\t\tmust-revalidate, max-age=604800\n" +
                "\n" +
                "\tHeader                                    at r.v.d.g.t.c.b.track.(TrackMockTest.java:190)\n" +
                "\t\th1=1\n" +
                "\n" +
                "\tHeaders                                   at r.v.d.g.t.c.b.track.(TrackMockTest.java:191)\n" +
                "\t\th2=[2]\n" +
                "\t\th3=[3]\n\n");

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
