package ru.vyarus.dropwizard.guice.test.client.builder.track;

import jakarta.ws.rs.core.GenericType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * @author Vyacheslav Rusakov
 * @since 06.10.2025
 */
public class ResponseTypeTest {

    @Test
    void testResponseTypeMapping() {

        RequestTracker tracker = new RequestTracker();
        tracker.track()
                .request().get(new GenericType<List<Map<String, Object>>>(){});

        Assertions.assertThat(tracker.getResultMappingClass()).isEqualTo(List.class);
        Assertions.assertThat(tracker.getResultMappingString()).isEqualTo("List<Map<String, Object>>");

    }
}
