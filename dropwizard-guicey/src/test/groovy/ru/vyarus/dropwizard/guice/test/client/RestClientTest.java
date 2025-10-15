package ru.vyarus.dropwizard.guice.test.client;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.client.support.ClientApp;
import ru.vyarus.dropwizard.guice.test.client.support.MatrixResource;
import ru.vyarus.dropwizard.guice.test.client.support.PrimitivesResource;
import ru.vyarus.dropwizard.guice.test.client.support.Resource;
import ru.vyarus.dropwizard.guice.test.client.support.sub.SubMatrix;
import ru.vyarus.dropwizard.guice.test.client.support.sub.SubResource;
import ru.vyarus.dropwizard.guice.test.client.support.sub.SubSubResource;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Vyacheslav Rusakov
 * @since 07.10.2025
 */
@TestDropwizardApp(value = ClientApp.class, useApacheClient = true)
public class RestClientTest {

    @Test
    @SuppressWarnings("unchecked")
    void testRestClient(ClientSupport client) throws Exception {
        final ResourceClient<Resource> rest = client.restClient(Resource.class);
        assertThat(rest.toString()).isEqualTo("Rest client for: Resource (http://localhost:8080/root)");

        // WHEN method string
        assertThat(rest.method("get").as(List.class)).containsExactly(1, 2, 3);
        assertThat(rest.method(Resource.class.getMethod("get")).as(List.class)).containsExactly(1, 2, 3);

        // WHEN method instance
        assertThat(rest.method(Resource.class.getMethod("get", String.class))
                .pathParam("name", "1")
                .as(List.class)).containsExactly(4, 5, 6);

        // WHEN method string with body
        assertThat(rest.method("post", "sample").asString()).isEqualTo("sample");

        // WHEN method instance with body
        assertThat(rest.method(Resource.class.getMethod("post", String.class), Entity.text("sample")).asString()).isEqualTo("sample");
        assertThat(rest.method(Resource.class.getMethod("post", String.class), Entity.text("sample")).asString()).isEqualTo("sample");
    }

    @Test
    void testBodyAsArgument(ClientSupport client) {
        final ResourceClient<Resource> rest = client.restClient(Resource.class);

        // WHEN body as argument
        final Resource.ModelType body = new Resource.ModelType("test");
        assertThat(rest.method(instance -> instance.post2(body)).asString()).isEqualTo("test");

        // WHEN annotated body
        assertThat(rest.method(instance -> instance.post3(body)).asString()).isEqualTo("test");
    }

    @Test
    void testSubResources(ClientSupport client) {
        final ResourceClient<Resource> rest = client.restClient(Resource.class);
        assertThat(rest.method(instance -> instance.sub().get()).asString()).isEqualTo("ok");
        assertThat(rest.subResourceClient("sub", SubResource.class)
                .method(SubResource::get)
                .asString())
                .isEqualTo("ok");

        assertThat(rest.subResourceClient("sub", SubResource.class)
                .subResourceClient("sub2", SubSubResource.class)
                .method(SubSubResource::get).asString()).isEqualTo("ko");

        assertThat(rest.subResourceClient(Resource::sub, SubResource.class)
                .subResourceClient("sub2", SubSubResource.class)
                .method(SubSubResource::get).asString()).isEqualTo("ko");

        assertThatThrownBy(() -> rest.restClient(SubResource.class))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("In context of resource, sub-resource client should be obtained with " +
                        "subResourceClient() method which ignores sub-resource @Path annotation (not used in sub-resource path building)");
    }

    @Test
    void testMatrixParams(ClientSupport client) {
        final ResourceClient<MatrixResource> rest = client.restClient(MatrixResource.class);

        assertThat(rest.method(instance -> instance.get("1", "2"))
                .assertRequest(tracker -> assertThat(tracker.getUrl()).endsWith("matrix/get;p1=1;p2=2"))
                .asString()).isEqualTo("1;2");

        assertThat(rest.method(instance -> instance.get(null, "1", "2"))
                .assertRequest(tracker -> assertThat(tracker.getUrl()).endsWith("matrix/get2;s=1/op;p1=1;p2=2"))
                .pathParam("vars", "get2;s=1")
                .asString()).isEqualTo("1;2");

        assertThat(rest.method(instance -> instance.sub(null).get("2"))
                .assertRequest(tracker -> assertThat(tracker.getUrl()).endsWith("matrix/sub;p1=1/get;s1=2"))
                .pathParam("vars", "sub;p1=1")
                .asString()).isEqualTo("2");
    }

    @Test
    void testBasicRestClient(ClientSupport client) {
        final TestClient<?> rest = client.restClient();
        assertThat(rest.toString()).isEqualTo("Client for: http://localhost:8080/");

        assertThat(rest.restClient(Resource.class).method(Resource::get).asString()).isEqualTo("[1,2,3]");

        assertThat(rest.subClient("matrix/sub;p1=1").asRestClient(SubMatrix.class)
                .method(instance -> instance.get("2")).asString())
                .isEqualTo("2");

        assertThat(rest.subClient(uriBuilder -> uriBuilder.path("matrix/sub").matrixParam("p1", 1), SubMatrix.class)
                .method(instance -> instance.get("2")).asString())
                .isEqualTo("2");

        assertThat(rest.subClient(uriBuilder -> uriBuilder.path("matrix/{sub}")
                        .resolveTemplate("sub", "sub")
                        .matrixParam("p1", 1), SubMatrix.class)
                .method(instance -> instance.get("2")).asString())
                .isEqualTo("2");
    }

    @Test
    void testShortcuts(ClientSupport client) {
        final TestClient<?> rest = client.restClient().subClient("/root");

        rest.get("/%s", "get");
        assertThat(rest.get("/%s", List.class, "get")).isEqualTo(Arrays.asList(1, 2, 3));
        assertThat(rest.get("/%s", new GenericType<List<Integer>>() {}, "get")).isEqualTo(Arrays.asList(1, 2, 3));
        assertThat(rest.buildGet("/%s", "get").as(List.class)).isEqualTo(Arrays.asList(1, 2, 3));

        rest.delete("/%s", "delete");
        assertThat(rest.delete("/%s", Integer.class, "delete")).isEqualTo(1);
        assertThat(rest.delete("/%s", new GenericType<Integer>() {}, "delete")).isEqualTo(1);
        assertThat(rest.buildDelete("/%s", "delete").as(Integer.class)).isEqualTo(1);

        rest.post("/%s", "text", "post");
        rest.post("/%s", Entity.text("text"), "post");
        assertThat(rest.post("/%s", "text", String.class, "post")).isEqualTo("text");
        assertThat(rest.post("/%s", Entity.text("text"), String.class, "post")).isEqualTo("text");
        assertThat(rest.post("/%s", "text", new GenericType<String>() {}, "post")).isEqualTo("text");
        assertThat(rest.post("/%s", Entity.text("text"), new GenericType<String>() {}, "post")).isEqualTo("text");
        assertThat(rest.buildPost("/%s", "text", "post").asString()).isEqualTo("text");
        assertThat(rest.buildPost("/%s", Entity.text("text"), "post").asString()).isEqualTo("text");

        rest.put("/%s", "text", "put");
        rest.put("/%s", Entity.text("text"), "put");
        assertThat(rest.put("/%s", "text", String.class, "put")).isEqualTo("text");
        assertThat(rest.put("/%s", Entity.text("text"), String.class, "put")).isEqualTo("text");
        assertThat(rest.put("/%s", "text", new GenericType<String>() {}, "put")).isEqualTo("text");
        assertThat(rest.put("/%s", Entity.text("text"), new GenericType<String>() {}, "put")).isEqualTo("text");
        assertThat(rest.buildPut("/%s", "text", "put").asString()).isEqualTo("text");
        assertThat(rest.buildPut("/%s", Entity.text("text"), "put").asString()).isEqualTo("text");

        rest.patch("/%s", "text", "patch");
        rest.patch("/%s", Entity.text("text"), "patch");
        assertThat(rest.patch("/%s", "text", String.class, "patch")).isEqualTo("text");
        assertThat(rest.patch("/%s", Entity.text("text"), String.class, "patch")).isEqualTo("text");
        assertThat(rest.patch("/%s", "text", new GenericType<String>() {}, "patch")).isEqualTo("text");
        assertThat(rest.patch("/%s", Entity.text("text"), new GenericType<String>() {}, "patch")).isEqualTo("text");
        assertThat(rest.buildPatch("/%s", "text", "patch").asString()).isEqualTo("text");
        assertThat(rest.buildPatch("/%s", Entity.text("text"), "patch").asString()).isEqualTo("text");
    }

    @Test
    void testPrimitiveResponses(ClientSupport client) {
        final ResourceClient<PrimitivesResource> rest = client.restClient(PrimitivesResource.class);

        assertThat(rest.method(PrimitivesResource::getByte).asString()).isEqualTo("1");
        assertThat(rest.method(PrimitivesResource::getBytes).as(byte[].class)).isEqualTo(new byte[]{1});
        assertThat(rest.method(PrimitivesResource::getLong).asString()).isEqualTo("1");
        assertThat(rest.method(PrimitivesResource::getLong).asString()).isEqualTo("1");
        assertThat(rest.method(PrimitivesResource::getBoolean).asString()).isEqualTo("false");
        assertThat(rest.method(PrimitivesResource::getShort).asString()).isEqualTo("1");
        assertThat(rest.method(PrimitivesResource::getFloat).asString()).isEqualTo("1.0");
        assertThat(rest.method(PrimitivesResource::getChar).as(char.class)).isEqualTo((char) 1);
        assertThat(rest.method(PrimitivesResource::getInt).asString()).isEqualTo("1");
        assertThat(rest.method(PrimitivesResource::getDouble).asString()).isEqualTo("1.0");
    }

    @Test
    void testManualRestCall(ClientSupport client) {
        assertThat(client.appClient()
                .subClient("/root")
                .asRestClient(Resource.class)
                .get("/get", String.class))
                .isEqualTo("[1,2,3]");
    }
}
