package ru.vyarus.dropwizard.guice.test.client.builder;

import com.google.common.collect.ImmutableMap;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.client.TestClient;
import ru.vyarus.dropwizard.guice.test.client.builder.track.impl.mock.TargetMock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 09.10.2025
 */
public class FormBuilderTest {

    @Test
    void testSimpleFormBuild() {
        FormBuilder builder = new FormBuilder(new TargetMock(), null);

        builder.param("foo", "bar")
                .param("bar", "baz")
                .param("tt", 12);

        assertThat(builder.toString()).isEqualTo("Form builder for: ");


        // WHEN build entity
        Entity<?> entity = builder.buildEntity();
        assertThat(entity.getEntity()).isInstanceOf(Form.class);
        final MultivaluedMap<String, String> map = ((Form) entity.getEntity()).asMap();
        assertThat(map).hasSize(3)
                .containsEntry("foo", List.of("bar"))
                .containsEntry("bar", List.of("baz"))
                .containsEntry("tt", List.of("12"));

        // WHEN build query params
        final Map<String, Object> res = builder.buildQueryParams();
        assertThat(res).hasSize(3)
                .containsEntry("foo", "bar")
                .containsEntry("bar", "baz")
                .containsEntry("tt", "12");

        // WHEN build multipart
        entity = builder.forceMultipart().buildEntity();
        assertThat(entity.getEntity()).isInstanceOf(FormDataMultiPart.class);
        final FormDataMultiPart body = (FormDataMultiPart) entity.getEntity();
        assertThat(body.getBodyParts()).hasSize(3);
        assertThat(body.getField("foo").getValue()).isEqualTo("bar");
        assertThat(body.getField("bar").getValue()).isEqualTo("baz");
        assertThat(body.getField("tt").getValue()).isEqualTo("12");
    }

    @Test
    void testMultiValueParameter() {
        FormBuilder builder = new FormBuilder(new TargetMock(), null);

        builder.param("foo", "bar", "baz")
                .param("foo2", Arrays.asList("bar", "baz"))
                .param("tt", 11, 12)
                .param("tt2", (Object) new Integer[]{11, 12});

        // WHEN build entity
        Entity<?> entity = builder.buildEntity();
        assertThat(entity.getEntity()).isInstanceOf(Form.class);
        final MultivaluedMap<String, String> map = ((Form) entity.getEntity()).asMap();
        assertThat(map).hasSize(4)
                .containsEntry("foo", List.of("bar", "baz"))
                .containsEntry("foo2", List.of("bar", "baz"))
                .containsEntry("tt", List.of("11", "12"))
                .containsEntry("tt2", List.of("11", "12"));

        // WHEN build query params
        final Map<String, Object> res = builder.buildQueryParams();
        assertThat(res).hasSize(4)
                .containsEntry("foo", List.of("bar", "baz"))
                .containsEntry("foo2", List.of("bar", "baz"))
                .containsEntry("tt", List.of("11", "12"))
                .containsEntry("tt", List.of("11", "12"));

        // WHEN build multipart
        entity = builder.forceMultipart().buildEntity();
        assertThat(entity.getEntity()).isInstanceOf(FormDataMultiPart.class);
        final FormDataMultiPart body = (FormDataMultiPart) entity.getEntity();
        assertThat(body.getBodyParts()).hasSize(8);
        assertThat(body.getFields().get("foo").size()).isEqualTo(2);
        assertThat(body.getFields().get("foo")).extracting(FormDataBodyPart::getValue).containsExactly("bar", "baz");
        assertThat(body.getFields().get("foo2")).extracting(FormDataBodyPart::getValue).containsExactly("bar", "baz");
        assertThat(body.getFields().get("tt")).extracting(FormDataBodyPart::getValue).containsExactly("11", "12");
        assertThat(body.getFields().get("tt2")).extracting(FormDataBodyPart::getValue).containsExactly("11", "12");
    }

    @Test
    void testConfigurationFromMap() {
        FormBuilder builder = new FormBuilder(new TargetMock(), null);

        builder.params(ImmutableMap.<String, Object>builder()
                .put("foo", "bar")
                .put("foo2", Arrays.asList("bar", "baz"))
                .put("tt", 11)
                .put("tt2", new Integer[]{11, 12})
                .build());

        // WHEN build entity
        Entity<?> entity = builder.buildEntity();
        assertThat(entity.getEntity()).isInstanceOf(Form.class);
        final MultivaluedMap<String, String> map = ((Form) entity.getEntity()).asMap();
        assertThat(map).hasSize(4)
                .containsEntry("foo", List.of("bar"))
                .containsEntry("foo2", List.of("bar", "baz"))
                .containsEntry("tt", List.of("11"))
                .containsEntry("tt2", List.of("11", "12"));

        // WHEN build query params
        final Map<String, Object> res = builder.buildQueryParams();
        assertThat(res).hasSize(4)
                .containsEntry("foo", "bar")
                .containsEntry("foo2", List.of("bar", "baz"))
                .containsEntry("tt", "11")
                .containsEntry("tt2", List.of("11", "12"));

        // WHEN build multipart
        entity = builder.forceMultipart().buildEntity();
        assertThat(entity.getEntity()).isInstanceOf(FormDataMultiPart.class);
        final FormDataMultiPart body = (FormDataMultiPart) entity.getEntity();
        assertThat(body.getBodyParts()).hasSize(6);
        assertThat(body.getFields().get("foo")).extracting(FormDataBodyPart::getValue).containsExactly("bar");
        assertThat(body.getFields().get("foo2")).extracting(FormDataBodyPart::getValue).containsExactly("bar", "baz");
        assertThat(body.getFields().get("tt")).extracting(FormDataBodyPart::getValue).containsExactly("11");
        assertThat(body.getFields().get("tt2")).extracting(FormDataBodyPart::getValue).containsExactly("11", "12");
    }

    @Test
    void testMultipartFormRecognition() {
        FormBuilder builder = new FormBuilder(new TargetMock(), null);
        final File file = new File("src/test/resources/test.txt");
        builder.param("file", file);

        // WHEN build simple entity
        Entity<?> entity = builder.buildEntity();
        assertThat(entity.getEntity()).isInstanceOf(FormDataMultiPart.class);
        assertThat(((FormDataMultiPart) entity.getEntity()).getField("file"))
                .isInstanceOf(FileDataBodyPart.class)
                .extracting(part -> ((FileDataBodyPart) part).getFileEntity()).isEqualTo(file);

        // WHEN build from stream
        builder = new FormBuilder(new TargetMock(), null);
        InputStream stream = new ByteArrayInputStream(new byte[0]);
        builder.param("file", stream);
        entity = builder.buildEntity();
        assertThat(entity.getEntity()).isInstanceOf(FormDataMultiPart.class);
        assertThat(((FormDataMultiPart) entity.getEntity()).getField("file"))
                .isInstanceOf(StreamDataBodyPart.class)
                .extracting(part -> ((StreamDataBodyPart) part).getStreamEntity()).isEqualTo(stream);

        // WHEN build from body part
        builder = new FormBuilder(new TargetMock(), null);
        FileDataBodyPart bodyPart = new FileDataBodyPart("file", file);
        builder.param("file", bodyPart);
        entity = builder.buildEntity();
        assertThat(entity.getEntity()).isInstanceOf(FormDataMultiPart.class);
        assertThat(((FormDataMultiPart) entity.getEntity()).getField("file"))
                .isInstanceOf(FileDataBodyPart.class)
                .isEqualTo(bodyPart);
    }

    @Test
    void testDateFormatters() throws Exception {
        // WHEN direct pattern
        FormBuilder builder = new FormBuilder(new TargetMock(), null)
                .formDateFormat("yyyy");

        builder.param("util", new SimpleDateFormat("dd/MM/yyyy").parse("12/12/2012"))
                .param("time", DateTimeFormatter.ofPattern("dd/MM/yyyy").parse("11/11/2011"));

        assertThat(builder.buildQueryParams()).hasSize(2)
                .containsEntry("util", "2012")
                .containsEntry("time", "2011");

        // WHEN direct separate objects
        builder = new FormBuilder(new TargetMock(), null)
                .formDateFormatter(new SimpleDateFormat("yyyy"))
                .formDateTimeFormatter(DateTimeFormatter.ofPattern("yyyy"));

        builder.param("util", new SimpleDateFormat("dd/MM/yyyy").parse("12/12/2012"))
                .param("time", DateTimeFormatter.ofPattern("dd/MM/yyyy").parse("11/11/2011"));

        assertThat(builder.buildQueryParams()).hasSize(2)
                .containsEntry("util", "2012")
                .containsEntry("time", "2011");


        // WHEN pattern from config
        TestRequestConfig config = new TestRequestConfig(null)
                .formDateFormatter(new SimpleDateFormat("yyyy"))
                .formDateTimeFormatter(DateTimeFormatter.ofPattern("yyyy"));
        builder = new FormBuilder(new TargetMock(), config);

        builder.param("util", new SimpleDateFormat("dd/MM/yyyy").parse("12/12/2012"))
                .param("time", DateTimeFormatter.ofPattern("dd/MM/yyyy").parse("11/11/2011"));

        assertThat(builder.buildQueryParams()).hasSize(2)
                .containsEntry("util", "2012")
                .containsEntry("time", "2011");

        // WHEN pattern from default
        TestClient<?> client = new TestClient<>(TargetMock::new, null);
        client.defaultFormDateFormat("yyyy");


        assertThat(client.buildForm("/test")
                .param("util", new SimpleDateFormat("dd/MM/yyyy").parse("12/12/2012"))
                .param("time", DateTimeFormatter.ofPattern("dd/MM/yyyy").parse("11/11/2011"))
                .buildQueryParams())
                .hasSize(2)
                .containsEntry("util", "2012")
                .containsEntry("time", "2011");
    }
}
