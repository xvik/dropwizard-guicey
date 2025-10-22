# Testing rest
 
Guicey provides lightweight REST testing support: same as [dropwizard resource testing support](https://www.dropwizard.io/en/stable/manual/testing.html#testing-resources),
but with guicey-specific features.

Such tests would not start web container: all rest calls are simulated (but still, it tests every part of resource execution).

!!! important
    Rest stubs work only with lightweight guicey run (they are simply useless when web container started)

Lightweight REST could be declared with `@StubRest` annotation.

```java
@TestGuiceyApp(App.class)
public class Test {

    @StubRest
    RestClient rest;

    @Test
    public void test() {
        String res = rest.get("/foo", String.class);
        Assertions.assertEquals("something", res);

        WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class,
                () -> rest.get("/error", String.class));
        Assertions.assertEquals("error message", ex.getResponse().readEntity(String.class));
    }
}
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
@StubRest(MyResource.class)
RestClient rest;
```

This way only one resource would be started (and all resources directly registered in
application, not as guicey extension). All jersey extensions will remain.

Or a couple of resources:

```java
@StubRest({MyResource.class, MyResource2.class})
RestClient rest;
```

Or you may disable some resources:

```java
@StubRest(disableResources = {MyResource2.class, MyResource3.class})
RestClient rest;
```

## Disabling jersey extensions

Often jersey extensions, required for the final application, make complications for testing.

For example, exception mapper: dropwizard register default exception mapper which
returns only the error message, instead of actual exception (and so sometimes we can't check the real cause).

`disableDropwizardExceptionMappers = true` disables extensions, registered by dropwizard.

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
@TestGuiceyApp
public class Test {

    @StubRest
    RestClient rest;

    public void test() {
        WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class,
                () -> rest.get("/some/error", String.class));

        // exception hidden, only generic error code
        Assertions.assertTrue(ex.getResponse().readEntity(String.class)
                .startsWith("{\"code\":500,\"message\":\"There was an error processing your request. It has been logged"));
    }
}
```

Without dropwizard exception mapper, we can verify exact exception:

```java
public class Test {

    @StubRest(disableDropwizardExceptionMappers = true)
    RestClient rest;

    public void test() {
        ProcessingException ex = Assertions.assertThrows(ProcessingException.class,
                () -> rest.get("/error", String.class));
        // exception available
        Assertions.assertTrue(ex.getCause() instanceof IllegalStateException);
    }
}
```

It might be useful to disable application extensions also with `disableAllJerseyExtensions`:

```java
```java
@StubRest(disableDropwizardExceptionMappers = true,
        disableAllJerseyExtensions = true)
RestClient rest;
```

This way raw resource would be called without any additional logic.

!!! note
    Only extensions, managed by guicey could be disabled: extensions directly registered
    in dropwizard would remain.

Also, you can select exact extensions to use (e.g., to test it):

```java
@StubRest(jerseyExtensions = CustomExceptionMapper.class)
RestClient rest;
```

Or disable only some extensions (for example, disabling extension implementing security):

```java
@StubRest(disableJerseyExtensions = CustomSecurityFilter.class)
RestClient rest;
```

## Debug

Use **debug** output to see what extensions were actually included and what disabled:

```java
@TestGuiceyApp(.., debug = true)
public class Test {
    @StubRest(disableDropwizardExceptionMappers = true,
            disableResources = Resource2.class,
            disableJerseyExtensions = RestFilter2.class)
    RestClient rest;
}
```

```
REST stub (@StubRest) started on DebugReportTest$Test1:

	Jersey test container factory: org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory
	Dropwizard exception mappers: DISABLED

	2 resources (disabled 1):
		ErrorResource                (r.v.d.g.t.j.s.r.support)  
		Resource1                    (r.v.d.g.t.j.s.r.support)  

	2 jersey extensions (disabled 1):
		RestExceptionMapper          (r.v.d.g.t.j.s.r.support)  
		RestFilter1                  (r.v.d.g.t.j.s.r.support)  

	Use .printJerseyConfig() report to see ALL registered jersey extensions (including dropwizard)
```

## Requests logging

By default, rest client would log requests and responses:

```java
@TestGuiceyApp(App.class)
public class Test {

    @StubRest
    RestClient rest;

    @Test
    public void test() {
        String res = rest.get("/foo", String.class);
        Assertions.assertEquals("something", res);
    }
}
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

Logging could be disabled with `logRequests` option: ` @StubRest(logRequests = false)`

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
@StubRest(container = TestContainerPolicy.IN_MEMORY)
```

```java
// throw error if grizzly container not available in classpath
// (use to avoid accidental in-memory use)
@StubRest(container = TestContainerPolicy.GRIZZLY)
```

## Rest client

`RestClient` is the same as [ClientSupport#restClient()](client.md), available for guicey extensions.
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
    Multipart support is enabled automatically when dropwizard-forms available in classpath

    ```java
    rest.buildForm("/some/path")
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

!!! note
    Resource client could be directly injected as a test field
    (instead of calling `rest.resourceClient(MyResource.class)`:
    
    ```java
    @WebResourceClient
    ResourceClient<MyResource> rest;
    ```
