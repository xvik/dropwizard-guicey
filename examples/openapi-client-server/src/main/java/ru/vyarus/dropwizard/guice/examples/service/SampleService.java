package ru.vyarus.dropwizard.guice.examples.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.petstore.ApiException;
import com.petstore.api.PetApi;
import com.petstore.api.model.Pet;

/**
 * @author Vyacheslav Rusakov
 * @since 07.05.2025
 */
@Singleton
public class SampleService {

    @Inject
    PetApi petApi;

    public Pet findPet(long id) {
        try {
            return petApi.getPetById(id);
        } catch (ApiException e) {
            throw new IllegalStateException("Failed to call petclinic", e);
        }
    }
}
