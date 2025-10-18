# 8.0.0 Release Notes

* Dropwizard 5 (requires java 17)
* Guice without bundled ASM
* Application fields injection
* Apache test client
* Unify ClientSupport and stubs rest
* Test client fields support
* Application urls builder
* Fix stubs rest early initialization
* Auto close object stored in shared state 

## Guice without bundled ASM

Guicey now uses guice with a [classes](https://repo1.maven.org/maven2/com/google/inject/guice/7.0.0/) classifier without bundled ASM.
ASM is provided as a direct dependency.

Required to support recent java versions (>22). 

## Application fields injection

It is not possible to use injections in the application class. 
Useful for running accessing services in the application run method:

```java
public class App extends Application<Configuration> {
    @Inject Service service;

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder().build());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        service.doSomething();
    }
}
```

## Apache test client

`DefaultTestClientFactory` class was reworked to support extensibility.
`ApacheTestClientFactory` added (extending default factory) to use apache client (`Apache5ConnectorProvider`) 
instead of urlconnection (`HttpUrlConnectorProvider`).

!!! important "Why apache client is not set as default"
    Apache client has [problems with multipart requests](https://github.com/eclipse-ee4j/jersey/issues/5528#issuecomment-1934766714)
    (not a bug, but technical limitation).

    Urlconnection client has problems with PATCH requests on java > 16 
    (requires additional --add-opens).

    Urlconnection remains as default because PATCH requests are less usable 
    than multipart forms. 

There are new shortcuts to switch to apache client in test:

```java
@TestGuiceyApp(value=App.class, apacheClient=true)
```

Also, `ClientSupport` class now provides a shortcut to quickly switch client type
withing a test:

```java
public void testSomething(ClientSupport client) {
    ClientSupport apacheClient = client.apacheClient();
    ClientSupport urlConnectionClient = client.urlConnectionClient();
}
```

## Unify ClientSupport and stubs rest

Guicey provides default (universal) test client `ClientSupport` (used for integration tests) and 
stubs rest (`@StubRest`) `RestClient` (used for lightweight rest tests).

Now base client methods are unified by the `TestClient` class (extended by both):
this means both clients provide exactly the same request-related methods.

Also, as `ClientSupport` represents an application root client, which is not 
very useful in tests, it now provides 3 additinoal clients:

* `.appClient()` - application context client (could not be the same as application root)
* `.adminClient()` - admin context client
* `.restClient()` - rest context client

Plus, there is an ability to create a client for an external api: `.externalClient("http://api.com")`

With it, you can use the same client request methods for different contexts (different base urls).
For example:

```java
public void testSomething(ClientSupport client) {
    User user = client.restClient().get("/users/123", User.class);
}
```

### Client API changes

The main change is `ClientSupport.target()` methods. Which were previously 
supporting sequence of strings: `target(String... pathParts)`, but now
`String.format()` is used: `target(String format, Object... args)`.

!!!! note "Why String.format?"
    Sequence of paths appears to be a bad idea. Compare: 
    `target("a", "12", "c")` and `target("a/%s/c", 12)`.
    The latter is more readable and more flexible.

Moreover, all shortcut methods now support `String.format()` syntax too:
`get("/users/%s", 123)` and `post("/users/%s", new User(), 123)`

There is now not only `Class`-based shortcuts, but also a `GenericType`-based:

```java
    Uset user = client.get("/users/%s", User.class, 123);
    List<User> list = client.get("/users", new GenericType<>() {});
```

As an addition to existing method shortcuts (`get()`, `post()`, etc), 
there is a new `patch()` shortcut: `client.patch("/users/%s", new User(), 123)`

!!! WARNING "Breaking changes"
    * Due to `String.format()`-based syntax, old calls like `target("a", "12", "c")` will work incorrectly now.
    * It was possible to use target method without arguments to get root path: `target()`,
      but now it is not possible. Use `target("/")` instead.
    * Old calls like `client.get("/some/path", null)` used for void calls will also not work 
      (jvm will not know when method to choose: Class or GenericType). Instead, there is a special void shortcut now:
      `client.get("/some/path")` (but `client.get("/some/path", Void.class)` will also work).
    * `basePathMain()` and `targetMain()` methods now deprecated: use `basePathApp()` and `targetApp()` instead.

### Default logger change

By default, all `ClientSupport` (and stubs `RestClient`) requests are logged,
but before it was not logging multipart requests.

Now the logger is configured to log everything by default, including multipart requests.

### Request defaults

The initial concept of request defaults was introduced in stubs rest `RestClient`.
Now it evolved to be a universal concept for all clients.

Each `TestClient` provide "default*" methods to set request defaults:

* `defaultHeader("Name", "value")`
* `defaultQueryParam("Name", "value")`
* `defaultCookie("Name", "value")`
* `defaultAccept("application/json")`
* etc.

The most obvious use case is authorization:

```java
public void testSomething(ClientSupport client) {
    client.defaultHeader("Authorization", "Bearer 123");
    
    User user = client.restClient().get("/users/123", User.class);
}
```

### Sub clients

There is a concept of sub clients. It is used to create a client for a specific sub-url.
For example, suppose all called methods in test have some base path: `/{somehting}/path/to/resource`.
Instead of putting it into eachrequest:

```java
public void testSomething(ClientSupport client) {
    TestClient rest = client.restClient();
    
    rest.get("/%s/path/to/resource/%s", User.class, "path", 12);
    rest.post("/%s/path/to/resource/%12", new User(...), "path", 12);
}
```

A sub client can be created:

```java
public void testSomething(ClientSupport client) {
    TestClient rest = client.restClient().subClient("/{something}/path/to/resource")
            .defaultPathParam("something", "path");
    
    rest.get("/%s", User.class, "path", 12);
    rest.post("/%12", new User(...), "path", 12);
}
```

!!! note
    Sub clients inherit defaults of parent client.
    
    ```java
    client.defaultQueryParam("q", "v");
    TestClient rest = client.subClient("/path/to/resource");

    // inherited query parameter q=v will be applied to all requests    
    rest.get("/%s", User.class, "path", 12);
    ```

Defaults could be cleared at any time with `client.reset()`.

There is a special sub client creation method with jersey `UriBuilder`, required
to properly support matrix parameters in the middle of the path:

```java
TestClient sub = client.subClient(builder -> builder.path("/some/path").matrixParam("p", 1));

// /some/path;p=1/users/12
sub.get("/%s", User.class, 12);
```

### New builder API

There is a new builder API for `TestClient` covering all possible configurations
for jersey `WebTarget` and `Invocation.Builder`. The main idea was to simplify
request configuration: to provide all possible methods in one place.

For example:

```java
client.buildGet("/path")
    .queryParam("q", "v")
    .as(User.class)
```

There are even request specific extensions and properties supported:

```java
client.buildGet("/path")
    .queryParam("q", "v")
    .register(VoidBodyReader.class)
    .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
    .asVoid();
```

All builder methods start with a "build" prefix.

Builder provides direct value mappings:

* `.as(Class)`
* `.as(GenericType)`
* `.asVoid()`
* `.asString()`

And methods, returning raw (wrapped) response:

* `.invoce()` - response without status checks
* `.expectSuccess()` - fail if not success
* `.expectSuccess(201, 204)` - fail if not success or not expected status
* `.expectRedirect()` - fail if not redirect (method also disabled redirects following)
* `.expectRedirect(301)` - fail if not redirect or not expected status 
* `.expectFailure()` - fail if success
* `.expectFailure(400)` - fail success or not expected status

Response wrapper would be described below.

### Debugging

Considering the client defaults inheritance (potential decentralized request configuration)
it might be unobvious what was applied to a request.

Request builder provides a `debug()` options, which will print all applied defaults 
and direct builder configurations to the console:

```java
client.buildGet("/path")
    .queryParam("q", "v")
    .debug()
    .as(User.class)
```

```
Request configuration: 

	Path params:
		p1=1                                      at r.v.d.g.t.c.builder.(RequestBuilderTest.java:61)
		p2=2                                      at r.v.d.g.t.c.builder.(RequestBuilderTest.java:62)
		p3=3                                      at r.v.d.g.t.c.builder.(RequestBuilderTest.java:62)

	Query params:
		q1=1                                      at r.v.d.g.t.c.builder.(RequestBuilderTest.java:57)
		q2=2                                      at r.v.d.g.t.c.builder.(RequestBuilderTest.java:58)
		q3=3                                      at r.v.d.g.t.c.builder.(RequestBuilderTest.java:58)

	Accept:
		application/json                          at r.v.d.g.t.c.builder.(RequestBuilderTest.java:54)

Jersey request configuration: 

	Resolve template                          at r.v.d.g.t.c.builder.(TestRequestConfig.java:869)
		(encodeSlashInPath=false encoded=true)
		p1=1
		p2=2
		p3=3

	Query param                               at r.v.d.g.t.c.b.u.conf.(JerseyRequestConfigurer.java:82)
		q1=1

	Query param                               at r.v.d.g.t.c.b.u.conf.(JerseyRequestConfigurer.java:82)
		q2=2

	Query param                               at r.v.d.g.t.c.b.u.conf.(JerseyRequestConfigurer.java:82)
		q3=3

	Accept                                    at r.v.d.g.t.c.builder.(TestRequestConfig.java:899)
		[application/json]
```

It shows  two blocks:

* How request builder was configured (including defaults source)
* How jersey request was configured

The latter is obtained by wrapping jersey `WebTarget` and `Invocation.Builder`
objects to intercept all calls.

### Request assertions

It would not be very useful for the majority of cases, but as debug api could
aggregate all request configuration data, it is possible to assert on it:

```java
client.buildGet("/some/path")
    .matrixParam("p1", "1")
    .assertRequest(tracker -> assertThat(tracker.getUrl()).endsWith("/some/path;p1=1"))
    .as(SomeEntity.class);
```

Debug could be enabled for all requests: `client.defaultDebug(true)`.

### Form builder

There is a special builder helping build urlencoded and multipart requests (forms):

```java
// urlencoded
client.buildForm("/some/path")
        .param("name", 1)
        .param("date", 2)
        .buildPost()
        .as(String.class);

// multipart
client.buildForm("/some/path")
        .param("foo", "bar")     
        .param("file", new File("src/test/resources/test.txt"))
        .buildPost()
        .asVoid();
```

Also, it could be used to simply create a request entity and use it directly:

```java
Entity entity = client.buildForm(null)
        .param("foo", "bar")     
        .param("file", new File("src/test/resources/test.txt"))
        .buildEntity()

client.post("/some/path", entity); 
```

Builder will serialize all provided (non-multipart) parameters to string.
For dates, it is possible to specify a custom date format:

```java
client.buildForm("/some/path")
        .dateFormat("dd/MM/yyyy")
        .param("date", new Date())
        .param("date2", LocalDate.now())
        .buildPost()
        .asVoid();
```

(java.util and java.time date formatters could be set separately)

Or the default format could be changed globally: `client.defaultFormDateFormat("dd/MM/yyyy")`.`

### Response assertions

As mentioned above, request builder method like `.invoke()` or `.expectSuccess()` returns
a special response wrapper object. It provides a lot of useful assertions to simplify
response data testing (avoid boilerplate code).

For example, check a response header and cookie and obtain value

```java
User user = rest.buildGet("/users/123")
        .expectSuccess()
        .assertHeader("Token" , s -> s.startsWith("My-Header;"))
        .assertCookie("MyCookie", "12")
        .as(User.class);
```

Redirection correctness could be checked as:

```java
@Path("/resources")
public class Resource {
    
    @Path("/list")
    @GET
    public Response get() {
        ...
    }
    
    @Path("/redirect")
    @GET
    public Response redirect() {
        return Response.seeOther(
                urlBuilder.rest(SuccFailRedirectResource.class).method(Resource::get).buildUri()
        ).build();
    }
}
```

```java
rest.method(Resource::redirect)
        .expectRedirect()
        .assertHeader("Location", s -> s.endsWith("/resources/list"));
```

Also, "with*" methods could be used for completely manual assertions:

```java
rest.method(Resource::redirect)
        .expectSuccess(201)
        .withHeader("MyHeader", s -> 
            assertThat(s).startsWith("My-Header;"));
```

Response object could be converted without additional variables:

```java
String value = rest.method(SuccFailRedirectResource::redirect)
        .expectSuccess()
        .as(res -> res.readEntity(SomeClass.class).getProperty());
```

### Jersey API

As before, it is possible to use `client.target("/path")` to build raw jersey target
(with the correct base path). But without applied defaults.

Direct `Invocation.Builder` could be built with `client.request("/path")`.
Here all defaults are applied.

Builders does not hide native jersey API:

* `WebTarget` - could be modified directly with `request.configurePath(target -> target.path("foo"))`
* `Invocation.Builder` - with `request.configureRequest(req -> req.header("foo", "bar"))`

Such modifiers could be applied as defaults:

* `client.defaultPathConfiguration(...)`
* `client.defaultRequestConfiguration(...)`

Response wrapper also provides direct access to jersey `Response` object:
`response.asResponse()`.

### Resource clients

There is a special type of type-safe clients based on the simple idea:
resource class declaration already provides all required metadata to configure test request:

```java
@Path("/users")
public class UserResource {
    
    @Path("/{id}")
    @GET
    public User get(@NotNull @PathParam("id") Integer id) {}
}
```

Resource declares its path in the root `@Path` annotation and method annotations
tell that it's a GET request on path `/users/{id}` with required path parameter.

```java
public void testSomething(ClientSupport client) {
    ResourceClient<UserResource> rest = client.restClient(UserResource.class);
    
    User user = rest.method(r -> r.get(123)).as(User.class);
}
```

By using a mock object call (`r -> r.get(123)`) we specify a source of metadata and the required values
for request. Using it, a request builder is configured automatically.

It is not required to use all parameters (reverse mapping is not always possible): 
use null for not important arguments. All additional configurations could be done manually:

```java
public void testSomething(ClientSupport client) {
    ResourceClient<UserResource> rest = client.restClient(UserResource.class);
    
    User user = rest.method(r -> r.get(null))
            .pathParam("id", 123)
            .as(User.class);
}
```

Almost everything could be recognized:

* All parameter annotations like `@QueryParam`, `@PathParam`, `@HeaderParam`, `@MatrixParam`, `@FormParam`, etc.
* All request methods: `GET`, `POST`, `PUT`, `DELETE`, `PATCH`.
* Request body mapping: `void post(MyEntity entity)`
* And even multipart forms

Not related arguments should be simply ignored:

```java
public void get(@PathParam("id") Integer id, @Context HttpServletRequest request) {}

rest.method(r -> r.get(123, null));
```

#### Multipart forms

Multipart resource methods often use special multipart-related entities, like:

```java
    @Path("/multipart")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipart(
            @NotNull @FormDataParam("file") InputStream uploadedInputStream,
            @NotNull @FormDataParam("file") FormDataContentDisposition fileDetail) 
```

Which is not handy to create manually. To address this, `ResourceClient` provides a
special helper object to build multipart-related values:

```java
rest.multipartMethod((r, multipart) ->
                        r.multipart(multipart.fromClasspath("/sample.txt"),
                        multipart.disposition("file", "sample.txt"))
        .asVoid());
```

Here file stream passed as a first parameter and filename with the second one.

Or

```java
    @Path("/multipart2")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipart2(
            @NotNull @FormDataParam("file") FormDataBodyPart file)
```

```java
    rest.multipartMethod((r, multipart) ->
            r.multipart2(multipart.streamPart("file", "/sample.txt")))
        .asVoid();
```

And in case of generic multipart object argument:

```java
    @Path("/multipartGeneric")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipartGeneric(@NotNull FormDataMultiPart multiPart) 
```

there is a special builder:

```java
rest.multipartMethod((r, multipart) ->
        r.multipartGeneric(multipart.multipart()
              .field("foo", "bar")
              .stream("file", "/sample.txt")
              .build()))
         .as(String.class);
```

!!! note
    Multipart methods require the urlencoded client (default) and, most likely, 
    will fail with the apache client.

#### Sub resources

Sub resource could be declared with an instance:

```java
public class Resource {
    @Path("/sub")
    public SubResource sub() {
        return new SubResource();
    }
}
```

Then sub resource could be easily called directly:

```java
User user = rest.method(r -> r.sub().get(123)).as(User.class);
```

When sub resource method is using class:

```java
public class Resource {
    @Path("/sub")
    public Class<SubResource> sub() {
        return SubResource.class;
    }
}
```

Then you'll have to build a sub-client first: 

```java
ResourceClient<SubResource> subRest = rest.subResource(Resource::sub, SubResource.class);
```

!!! important
    Jersey ignores sub-resource `@Path` annotation.

#### Resource typification

It is not always possible to use resource class to buld a sub client 
(with `.restClient(Resource.class)`).

In such cases you can build a resource path manually and then "cast" client to resource type:

```java
ResourceClient<MyResource> rest = client.subClient("/resource/path")
            .asResourceClient(MyResource.class);
```

or just buid path manually: 

```java
ResourceClient<MyResource> rest = client.subClient(
            builder -> builder.path("/resource").matrixParam("p", 123),
      MyResource.class);
```

## Test client fields support

Before, `ClientSupport` could only be injected as test method (or setup method) parameter.
parameter:

```java
public void testSomething(ClientSupport client)
```

Now it is possible to inject it as a field:

```java
@WebClient
ClientSupport client;
```

It is also possible to inject its sub client:

```java
@WebClient(WebClientType.App)
TestClient app;

@WebClient(WebClientType.Admin)
TestClient admin;

@WebClient(WebClientType.Rest)
TestClient rest;
```
 
Additionally, `ResourceClient` could be injected directly:

```java
@WebResourceClient
ResourceClient<MyResource> rest;
```

!!! important
    Resource client injection works both with integration tests (real client)
    and stub rest:

    ```java
    @TestGuiceyApp(MyApp.class)
    public class MyTest {
        StubRest
        RestClient client;
    
        @WebResourceClient
        ResourceClient<MyResource> rest;
    }
    ```

    Note that resource client could be directly obtained form `RestClient`: 
    `client.restClient(MyResource.class)`


## Application urls builder

Mechanism, used in resource clients, could be also used to build application urls.

A new class `AppUrlBuilder` added to support this. It is not bound by default
in guice context, but could be injected (as jit binding):

```java
@Inject AppUrlBuilder builder;
```

Or it could be created manually: `new AppUrlBuilder(environment)`

There are 3 scenarios:

* Localhost urls: the default mode when all urls contains localhost and applciation port.
* Custom host: `builder.forHost("myhost.com")` when custom host used instead of localhost and application port 
   is applied automatically
* Proxy mode: `builder.forProxy("https://myhost.com")` when application is behind some proxy
  (like apache or nginx) hiding its real port.

Examples:

```java
// http://localhost:8080/
builder.root("/")
// http://localhost:8080/
builder.app("/")
// http://localhost:8081/
builder.admin("/")
// http://localhost:8080/
builder.rest("/")

// http://localhost:8080/users/123     
builde.rest(Resource.class).method(r -> r.get(123)).build()
// http://localhost:8080/users/123     
builde.rest(Resource.class).method(r -> r.get(null)).pathParam("id", 123).build()


// https://some.com:8081/something     
builder.forHost("https://some.com").admin("/something")

// https://some.com/something     
builder.forProxy("https://some.com").admin("/something")
```

Application server configurtion detection logic was only implemented inside `ClientSupport`,
now it was ported to `AppUrlBuilder`, which you can use to obtain:

```java
// 8080
builder.getAppPort();
// 8081
builder.getAdminPort();
// "/" (server.adminContextPath)
builder.getAdminContextPath();
// "/" (server.applicationContextPath)
builder.getAppContextPath();
// "/" (server.rootPath)
builder.getRestContextPath();
```

## Fix stubs rest early initialization

`@StubRest` rest context was starting too early, causing closed configuration error
when `Application#run` method tried to configure it.

Now it is started after the application run.

## Auto close object stored in the shared state

`SharedConfigurationState` values, implementing `AutoCloseable` could be closed
automatically now after application shutdown.