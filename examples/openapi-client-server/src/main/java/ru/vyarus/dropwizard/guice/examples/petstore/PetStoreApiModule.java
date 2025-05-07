package ru.vyarus.dropwizard.guice.examples.petstore;

import com.petstore.ApiClient;
import com.petstore.api.PetApi;
import com.petstore.api.StoreApi;
import com.petstore.api.UserApi;
import ru.vyarus.dropwizard.guice.examples.ExampleConfig;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;

/**
 * @author Vyacheslav Rusakov
 * @since 07.05.2025
 */
public class PetStoreApiModule extends DropwizardAwareModule<ExampleConfig> {

    @Override
    protected void configure() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(configuration().getPetStoreUrl());
        // optional
        apiClient.setDebugging(true);

        bind(ApiClient.class).toInstance(apiClient);
        bind(PetApi.class).toInstance(new PetApi(apiClient));
        bind(StoreApi.class).toInstance(new StoreApi(apiClient));
        bind(UserApi.class).toInstance(new UserApi(apiClient));
    }
}
