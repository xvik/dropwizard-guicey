## Sub resources

Default resource installer in guicey registers resource instance, created with guice (resource is a guice bean). 

There are two ways of implementing jersey [sub resources](https://jersey.github.io/documentation/latest/jaxrs-resources.html#d0e2542)

### Guice managed

Sub resource is created as guice bean and it's instance returned from the resource method.

```java
@Path("/root")
public class RootResource {
    @Inject
    private SubResource subResource;
    
    @Path("sub")
    public SubResource sub() {
        return subResource;
    }
}

@Singleton
public class SubResource {

    @GET
    public String handle() {
        return "guice";
    }
}
```

`RootResource` is installed by resource installer. `SubResource` is normal guice bean.

Note that `SubResource` is not annotated with `@Path`. If it will be annotated and classpath scan will be enabled,
then sub resource should be annotated with `@InvisibleForScanner` to avoid sub resource installation as root resource.

In order to access context parameters, jersey service must be used in sub resource:

```java
@Path("/root")
public class RootResource {
    @Inject
    private SubResource subResource;
    
    @Path("{foo}/sub")
    public SubResource sub() {
        return subResource;
    }
}

@Singleton
public class SubResource {

    @Inject
    private Provider<UriInfo> uri;
    
    @GET
    public String handle() {
        String foo = uri.get().getPathParameters().getFirst("foo");
        return "guice " + foo;
    }
}
```

Here sub resource access path parameter, declared on root resource.

### Hk managed

Last example could be more elegant with pure hk sub resource:

```java
@Path("/root")
public class RootResource {
    
    @Path("{foo}/sub")
    public Class<SubResource> sub() {
        return SubResource.class;
    }
}

public class SubResource {

    private String foo;
    
    public HkSubResource(@PathParam("foo") String foo) {
        this.foo = foo;
    }
    
    @GET
    public String handle() {
        return "guice " + foo;
    }
}
``` 

Now sub resource is managed by HK container and so can use all jersey features. But sub resource
instance will be created for each request.

Note that if `SubResource` would be annotated with `@Path` it should be annotated with `@InvisibleForScanner`
to avoid installation (when classpath scan enabled).

If guice dependencies required in sub resource then hk bridge [MUST be enabled](http://xvik.github.io/dropwizard-guicey/4.1.0/guide/configuration/#hk-bridge):

Add dependency

```groovy
implementation 'org.glassfish.hk2:guice-bridge'
``` 

Enable option

```java
GuiceBundle.builder()
        .option(GuiceyOptions.UseHkBridge, true)
```

Now guice services could be injected inside hk managed beans:

```java
public class SubResource {

    private String foo;
    private SomeGuiceService service;
    
    public HkSubResource(@PathParam("foo") String foo, SomeGuiceService service) {
        this.foo = foo;
        this.service = service;
    }
    
    @GET
    public String handle() {
        return "guice " + service.handle(foo);
    }
}
```