# Testing web (HTTP client)

`ClientSupport` is a [JerseyClient](https://eclipse-ee4j.github.io/jersey.github.io/documentation/2.29.1/client.html)
aware of dropwizard configuration, so you can easily call admin/main/rest urls.

Creation:

```java
ClientSupport client = TestSupport.webClient(support);
```

where support is `DropwizardTestSupport` or `GuiceyTestSupport` (in later case it could be used only as generic client for calling external urls).

!!! note
    There are 4 base urls:
    
    * Server root: http://localhost:8080/
    * Application root: http://localhost:8080/app ("/" by default but could be changed: `server.applicationContextPath = 'app'`)
    * Admin root: http://localhost:8081/ or http://localhost:8080/admin (simple server with `server.adminContextPath = 'admin'`)
    * Rest root: http://localhost:8080/rest/ ("/" by default, but could be changed: `server.rootPath = 'rest'`)

    By default, all of them are "/" (root), but could be changed. 

`CientSupport` is a client for server root (everything after port). Usually,
a more specific client is required (app/admin/rest):

```java
TestClient app = client.appClient();
TestClient admin = client.adminClient();
TestClient rest = client.restClient();
```

Specific client usage guarantees url correctness in case of server configuration change.
For example, an integration test may use a random port and clients will use the correct one.  

!!! note
    `ClientSupport` is also extends `TestClient`, so it is 4rth client (for application root)
    and provides the same shortcut methods as other clients.

    Also, `ClientSupport` provide special shortcuts for jersey api:
    
    ```java
    // GET {rest path}/some
    client.targetRest("some").request().buildGet().invoke()
    
    // GET {main context path}/servlet
    client.targetApp("servlet").request().buildGet().invoke()
    
    // GET {admin context path}/adminServlet
    client.targetAdmin("adminServlet").request().buildGet().invoke()
    
    // General external url call
    client.target("https://google.com").request().buildGet().invoke()
    ```

If you want to construct a custom client, `ClientSupport` object can provide all required info:

```groovy
client.getPort()        // app port (8080)
client.getAdminPort()   // app admin port (8081)
client.basePathRoot()   // root server path (http://localhost:8080/)
client.basePathApp()   // main context path (http://localhost:8080/)
client.basePathAdmin()  // admin context path (http://localhost:8081/)
client.basePathRest()   // rest context path (http://localhost:8080/)
```

## Simple shortcuts

`TestClient` provides simple shortcuts for the GET/POST/PUT/PATCH/DELETE methods:

```java
@Test
public void testWeb(ClientSupport client) {
    TestClient rest = client.restClient();
    
    // get with simple result
    Result res = client.get("/sample", Result.class);
    // get with simple result list
    List<Entity> res = client.get("/list", new GenericType<>() {});
    
    // post without result (void)
    client.post("/post", new PostObject());

    // post with result 
    Result res = client.post("rest/action", new PostObject(), Result.class);
}
```

## String.format

String format could be used for all methods:

```java
client.targetRest("some/%s", 12).request().buildGet().invoke()

client.targetRest("some/%s", 12).request().buildGet().invoke(String.class)
    
client.get("/some/%s", User.class, 12)
```

## Defaults

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

Defaults could be cleared at any time with `client.reset()`.

## Sub clients

There is a concept of sub clients. It is used to create a client for a specific sub-url.
For example, suppose all called methods in test have some base path: `/{somehting}/path/to/resource`.
Instead of putting it into each request:

```java
public void testSomething(ClientSupport client) {
    TestClient rest = client.restClient();
    
    rest.get("/%s/path/to/resource/%s", User.class, "path", 12);
    rest.post("/%s/path/to/resource/%s", new User(...), "path", 12);
}
```

A sub client could be created:

```java
public void testSomething(ClientSupport client) {
    TestClient rest = client.restClient().subClient("/{something}/path/to/resource")
            .defaultPathParam("something", "path");
    
    rest.get("/%s", User.class, "path", 12);
    rest.post("/%s", new User(...), "path", 12);
}
```

!!! note
    Sub clients inherit defaults of parent client.

    ```java
    client.defaultQueryParam("q", "v");
    TestClient rest = client.subClient("/path/to/resource");

    // inherited query parameter q=v will be applied to all requests    
    rest.get("/%s", User.class, 12);
    ```

There is a special sub client creation method using jersey `UriBuilder`, required
to properly support matrix parameters in the middle of the path:

```java
TestClient sub = client.subClient(builder -> builder.path("/some/path").matrixParam("p", 1));

// /some/path;p=1/users/12
sub.get("/users/%s", User.class, 12);
```

## Builder API

Request builder API covers all possible configurations
for jersey `WebTarget` and `Invocation.Builder`. The main idea was to simplify
request configuration: to provide all possible methods in one place.

For example:

```java
client.buildGet("/path")
    .queryParam("q", "v")
    .as(User.class)
```

Request specific extensions and properties are also supported:

```java
client.buildGet("/path")
    .queryParam("q", "v")
    .register(VoidBodyReader.class)
    .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
    .asVoid();
```

All builder methods start with a "build" prefix (`buildGet()`, `buildPost()` or generic `build()`).

Builder provides direct value mappings:

* `.as(Class)`
* `.as(GenericType)`
* `.asVoid()`
* `.asString()`

And methods, returning raw (wrapped) response:

* `.invoke()` - response without status checks
* `.expectSuccess()` - fail if not success
* `.expectSuccess(201, 204)` - fail if not success or not expected status
* `.expectRedirect()` - fail if not redirect (method also disabled redirects following)
* `.expectRedirect(301)` - fail if not redirect or not expected status
* `.expectFailure()` - fail if success
* `.expectFailure(400)` - fail success or not expected status

## Debug

Considering the client defaults inheritance (potential decentralized request configuration),
it might be unobvious what was applied to the request.

Request builder provides a `debug()` option, which will print all applied defaults
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

It shows two blocks:

* How request builder was configured (including defaults source)
* How jersey request was configured

The latter is obtained by wrapping jersey `WebTarget` and `Invocation.Builder`
objects to intercept all calls.

Debug could be enabled for all requests: `client.defaultDebug(true)`.

## Request assertions

It would not be very useful for the majority of cases, but as debug api could
aggregate all request configuration data, it is possible to assert on it:

```java
client.buildGet("/some/path")
    .matrixParam("p1", "1")
    .assertRequest(tracker -> assertThat(tracker.getUrl()).endsWith("/some/path;p1=1"))
    .as(SomeEntity.class);
```

or

```java
.assertRequest(tracker -> assertThat(tracker.getQueryParams().get("q")).isEqualTo("1"))
```

## Response assertions

Request builder methods like `.invoke()` or `.expectSuccess()` returns
a special response wrapper object. It provides a lot of useful assertions to simplify
response data testing (avoid boilerplate code).

For example, check a response header, cookie and obtain value

```java
User user = rest.buildGet("/users/123")
        .expectSuccess()
        .assertHeader("Token" , s -> s.startsWith("My-Header;"))
        .assertCookie("MyCookie", "12")
        .as(User.class);
```

Here assertion error will be thrown if header or cookie was not provided or condition does not match.

Even if you need to obtain a header or cookie value from response, you can use assetions to verify
header/cookie presence:

```java
Response response = rest.buildGet("/users/123")
        .expectSuccess()
        .assertHeader("Token" , s -> s.startsWith("My-Header;"))
        .asResponse();

// here you could be sure the header exists        
String token = response.getHeaderString("Token");
```

Redirection correctness could be checked as:

```java
@Path("/resources")
public class Resource {
    
    @Inject
    AppUrlBuilder urlBuilder;
    
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
        // throw error if not 3xx; also, this disables redirects following
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
String value = rest.method(Resource::redirect)
        .expectSuccess()
        .as(res -> res.readEntity(SomeClass.class).getProperty());
```

## Form builder

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

!!! tip
    Compare with raw jersey api usage:

    ```java
    FormDataMultiPart multiPart = new FormDataMultiPart();
    multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file",
            file.toFile(),
            MediaType.APPLICATION_OCTET_STREAM_TYPE);
    multiPart.bodyPart(fileDataBodyPart);

    rest.post(path, Entity.entity(multiPart, multiPart.getMediaType()), Something.class);
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

(java.util and java.time date formatters could be set separately with `dateFormatter()` or `dateTimeFormatter()` methods)

The default format could be changed globally: `client.defaultFormDateFormat("dd/MM/yyyy")`
(or `defaultFormDateFormatter()` with `defaultFormDateTimeFormatter()`).

## Jersey API

It is possible to use `client.target("/path")` to build raw jersey target
(with the correct base path). But without applied defaults.

Direct `Invocation.Builder` could be built with `client.request("/path")`.
Here all defaults would be applied.

Builder API does not hide native jersey API:

* `WebTarget` - could be modified directly with `request.configurePath(target -> target.path("foo"))`
* `Invocation.Builder` - with `request.configureRequest(req -> req.header("foo", "bar"))`

Such modifiers could be applied as client defaults:

* `client.defaultPathConfiguration(...)`
* `client.defaultRequestConfiguration(...)`

Response wrapper also provides direct access to jersey `Response` object:
`response.asResponse()`.

## Resource clients

There is a special type of type-safe clients based on the simple idea:
resource class declaration already provides all required metadata to configure a test request:

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
// essentially, it's a sub client build with the resource path (from @Path annotation)
ResourceClient<UserResource> rest = client.restClient(UserResource.class);

User user = rest.method(r -> r.get(123)).as(User.class);
```

By using a mock object call (`r -> r.get(123)`) we specify a source of metadata and the required values
for request. Using it, a request builder is configured automatically.

It is not required to use all parameters (reverse mapping is not always possible):
use null for not important arguments. All additional configurations could be done manually:

```java
ResourceClient<UserResource> rest = client.restClient(UserResource.class);

User user = rest.method(r -> r.get(null))
        .pathParam("id", 123)
        .as(User.class);
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

!!! note
    `ResourceClient` extends `TestClient`, so all usual method shortcuts are also available for resource client
    (real method calls usage is not mandatory).

### Multipart forms

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

In case of generic multipart object argument:

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

### Sub resources

When a sub resource is declared with an instance:

```java
public class Resource {
    @Path("/sub")
    public SubResource sub() {
        return new SubResource();
    }
}
```

it could be easily called directly:

```java
User user = rest.method(r -> r.sub().get(123)).as(User.class);
```

When a sub resource method is using class:

```java
public class Resource {
    @Path("/sub")
    public Class<SubResource> sub() {
        return SubResource.class;
    }
}
```

you'll have to build a sub-client first:

```java
ResourceClient<SubResource> subRest = rest.subResource(Resource::sub, SubResource.class);
```

!!! important
    Jersey ignores sub-resource `@Path` annotation, so special method for sub resource clients is required.

### Resource typification

It is not always possible to use resource class to buld a sub client
(with `.restClient(Resource.class)`).

In such cases you can build a resource path manually and then "cast" client to the resource type:

```java
ResourceClient<MyResource> rest = client.subClient("/resource/path")
            .asResourceClient(MyResource.class);
```

or just build path manually:

```java
ResourceClient<MyResource> rest = client.subClient(
            builder -> builder.path("/resource").matrixParam("p", 123),
      MyResource.class);
```


## Apache client

By default, the client is based on "url connector", which has a limitation for PATCH
requests: on java > 16 PATCH requests will not work without additional `--add-opens`.
For such requests it is easier to use an apache connector.

It is not possible to use apache connector by default because it
[has problems](https://github.com/eclipse-ee4j/jersey/issues/5528#issuecomment-1934766714)
with multipart requests).

You can switch connector type either by providing different `TestClientFactory`
or by calling `ClientSupport` shortcuts:

* `client.apacheClient()` - `ClientSupport` with apache connector
* `client.urlconnectorClient()` - `ClientSupport` with url connector

With these shortcuts you can use both connectors in the same test.

## Customization

`JerseyClient` used in `ClientSupport` could be customized using `TestClientFactory` implementation.

Simple factory example:

```java
public class SimpleTestClientFactory implements TestClientFactory {

    @Override
    public JerseyClient create(final DropwizardTestSupport<?> support) {
        return new JerseyClientBuilder()
                .register(new JacksonFeature(support.getEnvironment().getObjectMapper()))
                .property(ClientProperties.CONNECT_TIMEOUT, 1000)
                .property(ClientProperties.READ_TIMEOUT, 5000)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .build();
    }
}
```

or using the default implementation as base:

```java
public class SimpleTestClientFactory extends DefaultTestClientFactory {
    
    @Override
    protected void configure(final JerseyClientBuilder builder, final DropwizardTestSupport<?> support) {
        builder.getConfiguration().connectorProvider(new Apache5ConnectorProvider());
    }
}
```

Default implementation (`DefaultTestClientFactory`) applies timeouts and auto-registers multipart support if `dropwizard-forms` module
if available in classpath.

All builders support `.clientFactory()` method for optional customization.


## Default client

`JerseyClient` used inside `ClientSupport` is created by `DefaultTestClientFactory`.

Default implementation:

1. Enables multipart feature if `dropwizard-forms` is in classpath (so the client could be used
   for sending multipart data).
2. Enables request and response logging to simplify writing (and debugging) tests.

By default, all request and response messages are written directly into console to guarantee client
actions visibility (logging might not be configured in tests).

Example output:

```

[Client action]---------------------------------------------{
1 * Sending client request on thread main
1 > GET http://localhost:8080/sample/get

}----------------------------------------------------------


[Client action]---------------------------------------------{
1 * Client response received on thread main
1 < 200
1 < Content-Length: 13
1 < Content-Type: application/json
1 < Date: Mon, 27 Nov 2023 10:00:40 GMT
1 < Vary: Accept-Encoding
{"foo":"get"}

}----------------------------------------------------------
```

Console output might be disabled with a system proprty:

```java
// shortcut sets DefaultTestClientFactory.USE_LOGGER property
DefaultTestClientFactory.disableConsoleLog()
```

With it, everything would be logged into `ClientSupport` logger (java.util) under INFO
(most likely, would be invisible in the most logger configurations, but could be enabled).


To reset property (and get logs back into console) use:

```java
DefaultTestClientFactory.enableConsoleLog()
```

!!! note
    Static methods added not directly into `ClientSupport` because this is
    the default client factory feature. You might use a completely different factory.
