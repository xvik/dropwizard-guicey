package ru.vyarus.dropwizard.guice.examples.petstore;

import com.petstore.server.api.PetApi;
import com.petstore.server.api.StoreApi;
import com.petstore.server.api.UserApi;
import jakarta.ws.rs.Path;

/**
 * @author Vyacheslav Rusakov
 * @since 07.05.2025
 */
@Path("/fake/petstore/")
public class FakePetStoreServer {

    // IMPORTANT: paths in console would contain /pet/pet duplicate, but ACTUAL path matching would IGNORE
    // @Path("/pet") declared on ApiApi class, so such declaration is correct for runtime

    @Path("/pet")
    public Class<PetApi> getPetApi() {
        return PetApi.class;
    }

    @Path("/store")
    public Class<StoreApi> getStoreApi() {
        return StoreApi.class;
    }

    @Path("/user")
    public Class<UserApi> getUserApi() {
        return UserApi.class;
    }
}
