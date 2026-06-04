# Testing REST

Guicey provides lightweight REST testing support: the same as [Dropwizard resource testing support](https://www.dropwizard.io/en/stable/manual/testing.html#testing-resources),
but with Guicey-specific features.

Such tests would not start the web container: all REST calls are simulated (but still, they test every part of resource execution).

!!! important
    REST stubs work only with lightweight Guicey run (they are simply useless when the web container is started)

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

By default, all declared resources would be started with all existing Jersey extensions
(filters, exception mappers, etc.). **Servlets and HTTP filters are not started**
(Guicey disables all web extensions to avoid their (confusing) appearance in the console)

## Selecting resources

Real tests usually require just one resource (to be tested):

```java
final RestStubsRunner restHook = RestStubsRunner.builder()
        .resources(MyResource.class)
        .build();
```

This way only one resource would be started (and all resources directly registered in
application, not as a Guicey extension). All Jersey extensions will remain.

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

Often Jersey extensions required for the final application complicate testing.

For example, an exception mapper: Dropwizard registers a default exception mapper which
returns only the error message, instead of the actual exception (and so sometimes we can't check the real cause).

`.disableDropwizardExceptionMappers(true)` disables extensions registered by Dropwizard.

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

Without the Dropwizard exception mapper, we can verify the exact exception:

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
    Only extensions managed by Guicey could be disabled: extensions directly registered
    in Dropwizard would remain.

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

```text
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

To activate the Grizzly container, add the dependency (version managed by the Dropwizard BOM):

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

`RestClient` is the same as [ClientSupport#restClient()](client.md), available for Guicey extensions.
It extends the same `TestClient` class and so provides the same abilities:

* [Defaults](client.md#defaults)
* [Shortcut methods](client.md#simple-shortcuts)
* [Builder API](client.md#builder-api)
* [Response assertions](client.md#response-assertions)
* [Static resource client](client.md#resource-clients)
* [Forms builder](client.md#form-builder)

!!! note
    Just in case: `ClientSupport` would not work with rest stubs (because web container is actually 
    not started and so `ClientSupport` can't recognize a correct rest mapping path). Of course,
    it could be used with a full URLs.

!!! note
    Multipart support is enabled automatically when dropwizard-forms is available on the classpath.
    Creating a multipart request with the [form builder](client.md#form-builder):

    ```java
    client.buildForm("/some/path")
        .param("foo", "bar")     
        .param("file", new File("src/test/resources/test.txt"))
        .buildPost()
        .asVoid();
    ``` 

To clear defaults:

```java
rest.reset() 
```

Might be a part of call chain:

```java
rest.reset().post(...) 
```