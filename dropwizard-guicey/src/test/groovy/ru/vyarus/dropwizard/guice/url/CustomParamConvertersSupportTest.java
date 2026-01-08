package ru.vyarus.dropwizard.guice.url;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.client.ParameterConvertersSupportTest;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.param.Jit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Vyacheslav Rusakov
 * @since 08.01.2026
 */
@TestDropwizardApp(CustomParamConvertersSupportTest.App.class)
public class CustomParamConvertersSupportTest {

    @Test
    void testCustomConverterSupportedForUrlBuilder(@Jit AppUrlBuilder urlBuilder) {

        String url = urlBuilder.rest(SampleRest.class)
                .method(instance -> instance.get(new CustomParam("1-2")))
                .build();
        Assertions.assertThat(url).isEqualTo("http://localhost:8080/get?param=1+with+2");
    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(ParameterConvertersSupportTest.SampleRest.class, ParameterConvertersSupportTest.CustomParamConverterProvider.class)
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
