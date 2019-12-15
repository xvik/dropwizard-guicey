# Resource installer

!!! summary ""
    CoreInstallersBundle / [ResourceInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/ResourceInstaller.java)

Installs [rest resources](https://www.dropwizard.io/en/release-2.0.x/manual/core.html#resources).

## Recognition

Detects classes annotated with jax-rs `#!java @Path` annotation and register them as rest resources.
Guice will manage resource creation, so you may think of it as usual guice bean.

```java
@Path("/res")
@Produces('application/json')
class SampleResource {
    
    @Inject
    private MyService service;
    
    @GET
    @Path("/sample")
    public Response sample() {
        return Response.ok(service.result()).build();
    }
}
```

!!! attention ""
    Resources registered as **singletons**, when no explicit scope annotation is used.
    Behaviour could be disabled with [option](../guide/configuration.md#options):
    ```java
    .option(InstallerOptions.ForceSingletonForJerseyExtensions, false)
    ``` 

Special `@Protptype` scope annotation may be used to mark resources in prototype scope.
It is useful when [guice servlet support is disabled](../guide/web.md#disable-servletmodule-support) (and so `@RequestScoped` could not be used). 

### Interface recognition

Class will also be recognized if `#!java @Path` annotation found on directly implemented interface.

```java
@Path("/res")
@Produces('application/json')
interface ResourceContract {

    @GET
    @Path("/sample")
    String sample();
}

class SampleResource implements ResourceContract {
    
    @Inject
    private MyService service;
    
    @Override
    public Response sample() {
        return Response.ok(service.result()).build();
    }
}
```

Annotations on interfaces are useful for [jersey client proxies](https://jersey.java.net/apidocs/2.22.1/jersey/org/glassfish/jersey/client/proxy/package-summary.html)  

```java
Client client = ClientBuilder.newClient();
ResourceContract resource = WebResourceFactory
    .newResource(ResourceContract.class, client.target("http://localhost:8080/"));

// call sample method on remote resource http://localhost:8080/res/sample
String result = resource.sample();
```

!!! warning ""
    Jersey client proxies requires extra dependency `org.glassfish.jersey.ext:jersey-proxy-client`

## Request scope bindings

If you need request scoped objects, use `#!java Provider`:

```java
class SampleResource {
    
    @Inject
    private Provider<HttpServletRequest> requestProvider;
    
    @GET
    @Path("/sample")
    public Response sample() {
        HttpServletRequest request = requestProvider.get();
        ...
    }
```

See [jersey objects, available for injection](../guide/guice/bindings.md#jersey-specific-bindings).

## @Context usage

`@Context` annotation usage is a common point of confusion. You can't use it for class fields: 

!!! fail "this will not work"
    ```java
    public class MyResource {
        @Context UriInfo info;
    }
    ```
    
Use provider instead:
    
!!! success "correct way"
    ```java
    public class MyResource {
        @Inject Provider<UriInfo> infoProvider;
    }
    ```

But, you can use `@Context` on method parameters:

```java
public class MyResource {
    @GET
    public Response get(@Context UriInfo info) { ... }
}
```

## Jersey managed resource

If resource class is annotated with `#!java @JerseyManaged` then jersey HK2 container will manage bean creation instead of guice. 
Injection of guice managed beans [could still be possible](../guide/hk2.md#hk2-guice-bridge) via registered [HK2-guice-bridge](https://hk2.java.net/2.4.0-b34/guice-bridge.html),
but guice aop features will not work.

!!! note
    You can manage resources with [HK2 by default](../guide/hk2.md#use-hk2-for-jersey-extensions),
    but this will also affect all [jersey extensions](jersey-ext.md)

```java
@Path("/res")
@Produces('application/json')
@JesreyManaged
class SampleResource {
    ...
}
```

!!! note ""
    `@Context` annotation on field will work on HK2 managed bean:
    ```java
    @Path()
    @JerseyManaged
    public class MyResource {
        @Context UriInfo info;
    }
    ```
