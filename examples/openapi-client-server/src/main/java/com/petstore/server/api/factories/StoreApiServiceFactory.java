package com.petstore.server.api.factories;

import com.petstore.server.api.StoreApiService;
import com.petstore.server.api.impl.StoreApiServiceImpl;


@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", comments = "Generator version: 7.12.0")
public class StoreApiServiceFactory {
    private static final StoreApiService service = new StoreApiServiceImpl();

    public static StoreApiService getStoreApi() {
        return service;
    }
}
