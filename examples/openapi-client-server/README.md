# OpenAPI 3 client and server examples

Source: https://petstore3.swagger.io/

Example show client and fake server generation from openapi declaration (json or yaml).

NOTE: example uses [gradle plugin](https://openapi-generator.tech/docs/integrations#gradle-integration),
but [maven plugin](https://openapi-generator.tech/docs/integrations#maven-integration) is also available

Build project to generate client and server (openapi generation tasks called implicitly).

`openApiGenerate` creates client in build/petstore/client
`openApiGenerateServer` creates server stub in build/petstore/server

(Refresh IDEA project to attach generated sources)

## General

Important dependencies:

```groovy
dependencies {
    implementation 'io.dropwizard:dropwizard-forms'
    implementation 'com.github.scribejava:scribejava-core:8.3.3'

    // OPENAPI CODEGEN
    // additional dependency required for codegen, but conflicts with dropwizard (need to disable feature)
    implementation 'org.glassfish.jersey.media:jersey-media-json-jackson'

    compileOnly 'io.swagger.core.v3:swagger-annotations:2.2.30'
}
```

## Client

Actual client interfaces are generated in:

```
/build/petstore/client/src/main/java/com/petstore/api
```

These are the main client classes:

* `PetApi`
* `StoreApi`
* `UserApi`

Guice bindings:

```java
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
```

(target url is in configuration)


## Server

Generated server stub:

```
/build/petstore/server/src/main/java/com/petstore/server
```

NOTE: server will also generate its own model, but this part will be attached directly (from `gen` folder).

Now copy server files into the main sources (preserving package):

```
/build/petstore/server/src/main/java/com/petstore/server -->  /src/main/java/com/petstore/server
```

(except `Bootstrap` class)

Implement fakes in `impl`. For example, to implement getPetById, change `PetApiServiceImpl` :

```java
@Override
public Response getPetById(Long petId, SecurityContext securityContext) throws NotFoundException {
    final Pet pet = new Pet();
    pet.setId(petId);
    pet.setName("Jack");
    final Tag tag = new Tag();
    tag.setName("puppy");
    pet.getTags().add(tag);
    return Response.ok().entity(pet).build();
}
```

Now implement root fake resource:

```java
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
```

## Bundle

Use GuiceyBundle for activation:

```java
public class PetStoreBundle implements GuiceyBundle {

    @Override
    public void run(GuiceyEnvironment environment) throws Exception {
        // because of required conflicting dependency jersey-media-json-jackson
        // https://github.com/dropwizard/dropwizard/issues/1341#issuecomment-251503011
        environment.environment().jersey()
                .property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, Boolean.TRUE);

        // register client api
        environment.modules(new PetStoreApiModule());

        // optional fake server start
        if (environment.<ExampleConfig>configuration().isStartFakeStore()) {
            environment.register(FakePetStoreServer.class);
        }
    }
}
```

Registration in app:

```java
@Override
public void initialize(Bootstrap<ExampleConfig> bootstrap) {
    bootstrap.addBundle(GuiceBundle.builder()
            .bundles(new PetStoreBundle())
            .build());
}
```

## Usage

```java
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
```

