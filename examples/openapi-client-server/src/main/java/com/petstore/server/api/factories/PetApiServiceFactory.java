package com.petstore.server.api.factories;

import com.petstore.server.api.PetApiService;
import com.petstore.server.api.impl.PetApiServiceImpl;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", comments = "Generator version: 7.12.0")
public class PetApiServiceFactory {
    private static final PetApiService service = new PetApiServiceImpl();

    public static PetApiService getPetApi() {
        return service;
    }
}
