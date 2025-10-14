package ru.vyarus.dropwizard.guice.test.client.builder.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.client.builder.util.conf.MultipartSupport;
import ru.vyarus.dropwizard.guice.test.client.util.FileDownloadUtil;

/**
 * @author Vyacheslav Rusakov
 * @since 09.10.2025
 */
public class DispositionParseTest {

    @Test
    void testManualDispositionParse() {
        check("attachment; filename=document.pdf", "document.pdf");
        check("attachment; filename=\"document.pdf\"", "document.pdf");
        check("attachment; filename*=UTF-8''%e2%82%ac%20rates", "€ rates");
        check("attachment; filename*=\"UTF-8''%e2%82%ac%20rates\"", "€ rates");
        check("attachment; filename=doc.doc; filename*=UTF-8''%e2%82%ac%20rates", "€ rates");
    }

    private void check(String header, String result) {
        String directParse = MultipartSupport.readFilename(header);
        Assertions.assertThat(directParse).as("Incorrect reference result").isEqualTo(result);
        Assertions.assertThat(directParse).as("incorrect manual parsing").isEqualTo(FileDownloadUtil.parseFileName(header));
    }
}
