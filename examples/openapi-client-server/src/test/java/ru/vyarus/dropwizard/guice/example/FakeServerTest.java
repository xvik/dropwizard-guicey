package ru.vyarus.dropwizard.guice.example;

import com.google.inject.Inject;
import com.petstore.api.model.Pet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.examples.ExampleApp;
import ru.vyarus.dropwizard.guice.examples.service.SampleService;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * @author Vyacheslav Rusakov
 * @since 07.05.2025
 */
@TestDropwizardApp(value = ExampleApp.class,
        configOverride = {
                "petStoreUrl: http://localhost:8080/fake/petstore",
                "startFakeStore: true"})
public class FakeServerTest {

    @Inject
    SampleService sampleService;

    @Test
    void testServer() {

        final Pet pet = sampleService.findPet(1);
        Assertions.assertNotNull(pet);
        Assertions.assertEquals("Jack", pet.getName());
    }
}
