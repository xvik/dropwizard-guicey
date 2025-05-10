# Testing REST

Guicey provides lightweight REST testing support: same as [dropwizard resource testing support](https://www.dropwizard.io/en/stable/manual/testing.html#testing-resources),
but with guicey-specific features.

Such tests would not start web container: all rest calls are simulated (but still, it tests every part of resource execution).

!!! important
    Rest stubs work only with lightweight guicey run (they are simply useless when web container started)

Lightweight REST could be started with `RestStubsRunner` hook:

```java
final RestStubsRunner restHook = RestStubsRunner.builder()
        .disableDropwizardExceptionMappers(true)
        .build();

TestSupport.build(App.class)
        .hooks(restHook)
        .runCore(injector -> {
            // pre-configured client to call resources with relative paths
            RestClient rest = restHook.getRestClient();
            
            String res = rest.get("/foo", String.class);
            Assertions.assertEquals("something", res);
            
            WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class,
                () -> rest.get("/error", String.class));
                Assertions.assertEquals("error message", ex.getResponse().readEntity(String.class));
        });
```

!!! note
    Extension naming is not quite correct: it is not a stub, but real application resources are used.
    The word "stub" used to highlight the fact of incomplete startup: only rest without web.

By default, all declared resources would be started with all existing jersey extensions
(filters, exception mappers, etc.). **Servlets and http filters are not started**
(guicey disables all web extensions to avoid their (confusing) appearance in console)

## Selecting resources

Real tests usually require just one resource (to be tested):

```java
final RestStubsRunner restHook = RestStubsRunner.builder()
        .resources(MyResource.class)
        .build();
```

This way only one resource would be started (and all resources directly registered in
application, not as guicey extension). All jersey extensions will remain.

Or a couple of resources:

```java
final RestStubsRunner restHook = RestStubsRunner.builder()
        .resources(MyResource.class, MyResource2.class)
        .build();
```

Or you may disable some resources:

```java
final RestStubsRunner restHook = RestStubsRunner.builder()
        .disableResources(MyResource2.class, MyResource3.class)
        .build();
```

## Disabling jersey extensions

Often jersey extensions, required for the final application, make complications for testing.

For example, exception mapper: dropwizard register default exception mapper which
returns only the error message, instead of actual exception (and so sometimes we can't check the real cause).

`.disableDropwizardExceptionMappers(true)` disables extensions, registered by dropwizard.

When default exception mapper enabled, resource throwing runtime error would return just error code:

```java
@Path("/some/")
@Produces("application/json")
public class ErrorResource {

    @GET
    @Path("/error")
    public String get() {
        throw new IllegalStateException("error");
    }
}    
```

```java
WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class,
        () -> rest.get("/some/error", String.class));

// exception hidden, only generic error code
Assertions.assertTrue(ex.getResponse().readEntity(String.class)
        .startsWith("{\"code\":500,\"message\":\"There was an error processing your request. It has been logged"));

```

Without dropwizard exception mapper, we can verify exact exception:

```java
final RestStubsRunner restHook = RestStubsRunner.builder()
        .disableDropwizardExceptionMappers(true)
        .build();

...

ProcessingException ex = Assertions.assertThrows(ProcessingException.class,
        () -> rest.get("/error", String.class));
// exception available
Assertions.assertTrue(ex.getCause() instanceof IllegalStateException);

```

It might be useful to disable application extensions also with `.disableAllJerseyExtensions(true)`:

```java
final RestStubsRunner restHook = RestStubsRunner.builder()
        .disableDropwizardExceptionMappers(true)
        .disableAllJerseyExtensions(true)
        .build();
```

This way raw resource would be called without any additional logic.

!!! note
    Only extensions, managed by guicey could be disabled: extensions directly registered
    in dropwizard would remain.

Also, you can select exact extensions to use (e.g., to test it):

```java
final RestStubsRunner restHook = RestStubsRunner.builder()
        .jerseyExtensions(CustomExceptionMapper.class)
        .build();
```

Or disable only some extensions (for example, disabling extension implementing security):

```java
final RestStubsRunner restHook = RestStubsRunner.builder()
        .disableJerseyExtensions(CustomSecurityFilter.class)
        .build();
```

## Requests logging

By default, rest client would log requests and responses, 

```java
String res = rest.get("/foo", String.class);
Assertions.assertEquals("something", res);
```

```
[Client action]---------------------------------------------{
1 * Sending client request on thread main
1 > GET http://localhost:0/foo

}----------------------------------------------------------


[Client action]---------------------------------------------{
1 * Client response received on thread main
1 < 200
1 < Content-Length: 3
1 < Content-Type: application/json
something

}----------------------------------------------------------
```

Logging could be disabled with `.logRequests(false)`:

```java
final RestStubsRunner restHook = RestStubsRunner.builder()
        .logRequests(false)
        .build();
```

## Container

By default, [InMemoryTestContainerFactory](https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest/test-framework.html#d0e18552)
used.

    In-Memory container is not a real container. It starts Jersey application and 
    directly calls internal APIs to handle request created by client provided by 
    test framework. There is no network communication involved. This containers 
    does not support servlet and other container dependent features, but it is a 
    perfect choice for simple unit tests.

If it is not enough (in-memory container does not support all functions), then
use `GrizzlyTestContainerFactory`

    The GrizzlyTestContainerFactory creates a container that can run as a light-weight, 
    plain HTTP container. Almost all Jersey tests are using Grizzly HTTP test container 
    factory.

To activate grizzly container add dependency (version managed by dropwizard BOM):

```groovy
testImplementation 'org.glassfish.jersey.test-framework.providers:jersey-test-framework-provider-grizzly2'
```

By default, grizzly would be used if it's available on classpath, otherwise in-memory used.
If you need to force any container type use:

```java
// use in-memory container, even if grizly available in classpath
// (use to force more lightweight container, even if some tests require grizzly)
.container(TestContainerPolicy.IN_MEMORY)
```

```java
// throw error if grizzly container not available in classpath
// (use to avoid accidental in-memory use)
.container(TestContainerPolicy.GRIZZLY)
```

## Rest client

`RestClient` is almost the same as [ClientSupport](client.md), available for guicey extensions.
It is just limited only for rest (and so simpler to use).

!!! note
    Just in case: `ClientSupport` would not work with rest stubs (because web container is actually 
    not started and so `ClientSupport` can't recognize a correct rest mapping path). Of course,
    it could be used with a full URLs.

Client provides base methods with response mapping:

```java
RestClient rest = restHook.getRestClient();
```

* `rest.get(path, Class)`
* `rest.post(path, Object/Entity, Class)`
* `rest.put(path, Object/Entity, Class)`
* `rest.delete(path, Class)`

To not overload default methods with parameters, additional data could be set with defaults:

* `rest.defaultHeader(String, String)`
* `rest.defaultQueryParam(String, String)`
* `rest.defaultAccept(String...)`
* `rest.defaultOk(Integer...)`

`defaultOk` used for void responses (response class == null) to check correct response
status (default 200 (OK) and 204 (NO_CONTENT)).

So if we need to perform a post request with query param and custom header:

```java
rest.defaultHeader("Secret", "unreadable")
    .defaultQueryParam("foo", "bar");
OtherModel res = rest.post("/somehere", new SomeModel(), OtherModel.class);
```

!!! note
    Multipart support is enabled automatically when dropwizard-forms available in classpath

    ```java
    FormDataMultiPart multiPart = new FormDataMultiPart();
    multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file",
            file.toFile(),
            MediaType.APPLICATION_OCTET_STREAM_TYPE);
    multiPart.bodyPart(fileDataBodyPart);

    rest.post(path, Entity.entity(multiPart, multiPart.getMediaType()), Something.class);
    ```

To clear defaults:

```java
rest.reset() 
```

Might be a part of call chain:

```java
rest.reset().post(...) 
```

When test needs to verify cookies, response headers, etc. use `.request(path)`:

```java
Response response = rest.request(path).get() // .post(), .put(), .delete();
```

All defaults are also applied in this case.

To avoid applying configured defaults, raw `rest.target(path)...` could be used.
