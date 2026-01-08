package ru.vyarus.dropwizard.guice.test.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 15.12.2025
 */
@TestGuiceyApp(ParameterConvertersSupportTest.App.class)
public class ParameterConvertersSupportTest {

    @StubRest
    RestClient client;

    @Test
    void testCustomConverterSupported() {

        String res = client.restClient(SampleRest.class)
                .method(instance -> instance.get(new CustomParam("1-2")))
                .assertRequest(requestTracker -> {
                    assertThat(requestTracker.getQueryParams().get("param")).isEqualTo("1-2");
                })
                .asString();

        assertThat(res).isEqualTo("1+2");
    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(SampleRest.class, CustomParamConverterProvider.class)
                    .build();
        }
    }

    @Path("/")
    public static class SampleRest {

        @GET
        @Path("/get")
        public String get(@QueryParam("param") CustomParam param) {
            return param.key + "+" + param.value;
        }
    }

    public static class CustomParam {
        private String key;
        private String value;

        public CustomParam(String value) {
            String[] split = value.split("-");
            this.key = split[0];
            this.value = split[1];
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            // intentionally different from required format
            return key + " with " + value;
        }
    }

    public static class CustomParamConverter implements ParamConverter<CustomParam> {

        @Override
        public CustomParam fromString(String value) {
            return new CustomParam(value);
        }

        @Override
        public String toString(CustomParam value) {
            return value.getKey() + "-" + value.getValue();
        }
    }

    @Provider
    public static class CustomParamConverterProvider implements ParamConverterProvider {

        @Override
        @SuppressWarnings("unchecked")
        public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
            if (rawType.equals(CustomParam.class)) {
                return (ParamConverter<T>) new CustomParamConverter();
            }
            return null;
        }
    }
}
