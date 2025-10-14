package ru.vyarus.dropwizard.guice.test.client.builder.call;

import org.assertj.core.api.Assertions;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Vyacheslav Rusakov
 * @since 12.10.2025
 */
public class MultipartArgumentHelperTest {

    @Test
    void testContentDispositionBuilding() {
        assertThat(MultipartArgumentHelper.createDispositionHeader("file", "text.xml"))
                .isEqualTo("form-data; name=\"file\"; filename=\"text.xml\"; filename*=UTF-8''text.xml");

        assertThat(MultipartArgumentHelper.createDispositionHeader("file", "файл_logback.xml"))
                .isEqualTo("form-data; name=\"file\"; filename=\"_logback.xml\"; filename*=UTF-8''%D1%84%D0%B0%D0%B9%D0%BB_logback.xml");
    }

    @Test
    void testDirectMethods() {
        MultipartArgumentHelper helper = new MultipartArgumentHelper();

        assertThat(helper.fromClasspath("/logback.xml")).isNotNull();
        assertThatThrownBy(() -> helper.fromClasspath("/unknown.txt"))
                .isInstanceOf(IllegalStateException.class)
                        .hasMessage("Classpath resource '/unknown.txt' not found");

        assertThat(helper.fromFile("src/test/resources/logback.xml")).isNotNull();
        assertThatThrownBy(() -> helper.fromFile("unknown.txt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to read file 'unknown.txt' stream");

        assertThat(helper.file("src/test/resources/logback.xml")).isNotNull();
        assertThatThrownBy(() -> helper.file("unknown.txt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("'unknown.txt' does not exist or is a directory");
        assertThatThrownBy(() -> helper.file("src"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("'src' does not exist or is a directory");

        assertThat(helper.disposition("file", new File("logback.xml"))).isNotNull()
                .extracting(disp -> disp.getFileName(true)).isEqualTo("logback.xml");
        assertThat(helper.disposition("file", "logback.xml")).isNotNull()
                .extracting(disp -> disp.getFileName(true)).isEqualTo("logback.xml");
        assertThat(helper.disposition("file", new File("файл_logback.xml"))).isNotNull()
                .extracting(ContentDisposition::getFileName)
                .isEqualTo("файл_logback.xml");

        assertThat(helper.part("foo", "bar"))
                .extracting(FormDataBodyPart::getName, FormDataBodyPart::getValue)
                .containsExactly("foo", "bar");
        assertThat(helper.filePart("foo", new File("src/test/resources/logback.xml")))
                .extracting(FormDataBodyPart::getName, fileDataBodyPart -> fileDataBodyPart.getFileName().get())
                .containsExactly("foo", "logback.xml");
        assertThat(helper.filePart("foo", "src/test/resources/logback.xml"))
                .extracting(FormDataBodyPart::getName, fileDataBodyPart -> fileDataBodyPart.getFileName().get())
                .containsExactly("foo", "logback.xml");

        assertThat(helper.streamPart("foo", getClass().getResourceAsStream("/logback.xml")))
                .extracting(StreamDataBodyPart::getName, streamDataBodyPart -> streamDataBodyPart.getFileName().isPresent())
                .containsExactly("foo", false);
        assertThat(helper.streamPart("foo", getClass().getResourceAsStream("/logback.xml"), "logback.xml"))
                .extracting(StreamDataBodyPart::getName, streamDataBodyPart -> streamDataBodyPart.getFileName().get())
                .containsExactly("foo", "logback.xml");
        assertThat(helper.streamPart("foo", "/logback.xml"))
                .extracting(StreamDataBodyPart::getName, streamDataBodyPart -> streamDataBodyPart.getFileName().get())
                .containsExactly("foo", "logback.xml");
    }

    @Test
    void testBuilder() {
        MultipartArgumentHelper helper = new MultipartArgumentHelper();

        assertThat(helper.multipart().field("foo", "bar").build().getBodyParts()).hasSize(1)
                .element(0).isInstanceOf(FormDataBodyPart.class);

        assertThat(helper.multipart().file("foo", new File("fl.txt")).build().getBodyParts()).hasSize(1)
                .element(0).isInstanceOf(FileDataBodyPart.class);
        assertThat(helper.multipart().file("foo", new File("fl.txt"), new File("fl2.txt")).build().getBodyParts()).hasSize(2);

        assertThat(helper.multipart().file("foo", "src/test/resources/logback.xml").build().getBodyParts()).hasSize(1)
                .element(0).isInstanceOf(FileDataBodyPart.class);

        assertThat(helper.multipart().stream("foo", "/logback.xml").build().getBodyParts()).hasSize(1)
                .element(0).isInstanceOf(StreamDataBodyPart.class);

        assertThat(helper.multipart().stream("foo", helper.fromClasspath("/logback.xml")).build().getBodyParts()).hasSize(1)
                .element(0).isInstanceOf(StreamDataBodyPart.class);
        assertThat(helper.multipart().stream("foo", helper.fromClasspath("/logback.xml"), "logback.xml").build().getBodyParts()).hasSize(1)
                .element(0).isInstanceOf(StreamDataBodyPart.class)
                .extracting(bodyPart -> bodyPart.getContentDisposition().getFileName()).isEqualTo("logback.xml");
    }
}
