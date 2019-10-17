# Extensions

!!! summary ""
    Extensions mechanism supposed to be used in guicey for all dropwizard specific features registration
    (instead of manual registrations). 

All extensions are recognized and installed with appropriate [installer](installers.md). This page supposed
to reference declaration examples of most common extensions. See [installers](../installers/resource.md) 
section for details.

!!! warning
    Extension is recognized only by one installer (according to installers order), even if it contains multiple signs!
    See [installers report](diagnostic/installers-report.md) for installers order.

Declaration sources:

* [Classpath scan](scan.md)
* [Manual declaration](configuration2.md#configuration-items)
* [Guice binding](guice/module-analysis.md#extensions-recognition)

!!! tip
    More installers (and so supported extensions types) could be available due to installed [extension bundles](../extras/bom.md).
    Use [installers report](diagnostic/installers-report.md) to see all available installers.
    
    Some extensins support order declaration with `@Order()` - see report.


!!! note
    If you have problems with injection inside extensions (NPE errors) first check that you did not register extension manually!  
    It is a quite often **mistake** (especially with jersey extensions):
    
    ```java
    environment.jersey().register(new MyResource())
    ```
    This way `MyResource` will not be managed by guice and so injections inside it **will not work**
     
    Use constructor injection to prevent such errors (manual places will reveal immediately):
    ```java
    @Path("/")
    public class MyResource {
        private final MyService service;
        
        @Inject
        public MyResource(MyService service) {
            this.service = service;
        }
    }
    ```
   

## Resource

```java
@Path("/res")
@Produces('application/json')   
@Singleton
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

[Recognized](../installers/resource.md) by `@Path` annotation on class or implemented interface.

## Task

```java 
@Singleton
public class MyTask extends Task {

    @Inject
    private MyService service;

    public TruncateDatabaseTask() {
        super("mytask");
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        service.doSomething();
    }
}
``` 

[Recognized](../installers/task.md) by base `Task` class.

## Managed

```java                                     
@Singleton
public class MyService implements Managed {

    @Override
    public void start() throws Exception {
        ...
    }

    @Override
    public void stop() throws Exception {
        ...
    }
}
```   

[Recognized](../installers/managed.md) by `Managed` base class.

## Health check

```java          
@Singleton
public class MyHealthCheck extends NamedHealthCheck {

    @Inject
    private MyService service;

    @Override
    protected Result check() throws Exception {
        if (service.isOk()) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Service is not ok");
        }
    }

    @Override
    public String getName() {
        return "my-service";
    }
}
```  

[Recognized](../installers/healthcheck.md) by base `NamedHealthCheck` class. Custom guicey base class 
used because it would be impossible to automatically register health check without name.  

## Jersey extensions

All jersey extensions are [recognized](../installers/jersey-ext.md) by `javax.ws.rs.ext.Provider` jersey annotation. 
There are [many extensions](../installers/jersey-ext.md) supported.

```java
@Provider
@Singleton
public class DummyExceptionMapper implements ExceptionMapper<RuntimeException> {

    private final Logger logger = LoggerFactory.getLogger(DummyExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException e) {
        logger.debug("Problem while executing", e);
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN)
                .entity(e.getMessage())
                .build();
    }

}
```        

or 

```java
@Provider       
@Singleton
public class MyContainerRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
    }
}
```        
    
## Eager singleton

```java
@EagerSingleton
public class MyService {}
```                          

[Recognized](../installers/eager.md) by `@EagerSingleton` annotation. Replacement
of manual `#!java bind(MyService.class).asEagerSingleton()`.

## More

!!! note
    This was only subset of supported extensions - see [installers](../installers/resource.md) 
    section.
         
    You can add additional extensions support with a [custom installer](installers.md#writing-custom-installer).