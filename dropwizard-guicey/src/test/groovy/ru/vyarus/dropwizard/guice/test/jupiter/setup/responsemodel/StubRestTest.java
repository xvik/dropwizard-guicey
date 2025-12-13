package ru.vyarus.dropwizard.guice.test.jupiter.setup.responsemodel;

import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.client.ResourceClient;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.rest.WebResourceClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.responsemodel.InterceptModel;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.responsemodel.ModelTracker;
import ru.vyarus.dropwizard.guice.test.responsemodel.model.ResponseModel;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 12.12.2025
 */
@TestGuiceyApp(ResponseModelInterceptionTest.App.class)
public class StubRestTest {

    @InterceptModel
    ModelTracker tracker;

    @StubRest
    RestClient client;

    @WebResourceClient
    ResourceClient<ResponseModelInterceptionTest.TestResource> rest;

    @Test
    void testSimpleModel() {

        // WHEN calling simple get
        rest.method(ResponseModelInterceptionTest.TestResource::get).asString();

        final ResponseModel model = tracker.getLastModel();
        assertThat((Integer) model.getModel()).isEqualTo(12);
    }

    @Test
    void testNullModel() {

        // WHEN calling simple get
        rest.method(ResponseModelInterceptionTest.TestResource::getNull).asString();

        final ResponseModel model = tracker.getLastModel();
        assertThat(model).isNull();
    }

    @Test
    void testRawResponse() {

        // WHEN calling simple get
        rest.method(ResponseModelInterceptionTest.TestResource::getResponse).asString();

        final ResponseModel model = tracker.getLastModel();
        assertThat((Integer) model.getModel()).isEqualTo(12);
    }

    @Test
    void testVoidResponse() {

        // WHEN calling simple get
        rest.method(instance -> instance.post(11)).asString();

        final ResponseModel model = tracker.getLastModel();
        assertThat(model).isNull();
    }

    @Test
    void testFileDownload() {

        // WHEN calling simple get
        rest.method(ResponseModelInterceptionTest.TestResource::download).asString();

        final ResponseModel model = tracker.getLastModel();
        assertThat((File) model.getModel()).isFile();

        File file = model.getModel();
        assertThat(file.getName()).isEqualTo("logback.xml");
    }

    @Test
    void testErrorResponse() {

        // WHEN calling simple get
        rest.method(ResponseModelInterceptionTest.TestResource::getError).invoke();

        final ResponseModel model = tracker.getLastModel();
        assertThat(model).isNull();
    }
}
