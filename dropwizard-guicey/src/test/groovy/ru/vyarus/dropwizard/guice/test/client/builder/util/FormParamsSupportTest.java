package ru.vyarus.dropwizard.guice.test.client.builder.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.client.builder.util.conf.FormParamsSupport;

import java.util.Arrays;

/**
 * @author Vyacheslav Rusakov
 * @since 09.10.2025
 */
public class FormParamsSupportTest {

    @Test
    void testParametersToString() throws Exception {
        verify(null, "");
        verify("", "");
        verify(" ", " ");
        verify("abc", "abc");
        verify("", "");
        verify(1, "1");
        verify(Arrays.asList(1,2,3), "1,2,3");
        verify(new Integer[]{1,2,3}, "1,2,3");
        verify(FormParamsSupport.DEFAULT_DATE_FORMAT.parse("2011-11-01T11:01:00"), "2011-11-01T11:01:00.000+00:00");
        verify(FormParamsSupport.DEFAULT_DATE_TIME_FORMAT.parse("2011-11-01T11:01:00Z"), "2011-11-01T11:01:00Z");
    }

    private void verify(Object value, String result) {
        Assertions.assertThat(FormParamsSupport.parameterToString(value)).as(result + " verification").isEqualTo(result);
    }
}
