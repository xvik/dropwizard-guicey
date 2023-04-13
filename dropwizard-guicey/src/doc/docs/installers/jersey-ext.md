# Jersey extension installer

!!! summary ""
    CoreInstallersBundle / [JerseyProviderInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/provider/JerseyProviderInstaller.java)        

Installs various jersey extensions, usually annotated with jersey `#!java @Provider` annotation and installed via `#!java environment.jersey().register()`:

    Supplier, ExceptionMapper, ValueParamProvider, InjectionResolver, 
    ParamConverterProvider, ContextResolver, MessageBodyReader, MessageBodyWriter, 
    ReaderInterceptor, WriterInterceptor, ContainerRequestFilter, 
    ContainerResponseFilter, DynamicFeature, ApplicationEventListener, ModelProcessor

## Recognition

Detects known jersey extension classes and classes annotated with jersey `@jakarta.ws.rs.ext.Provider` annotation and register their instances in jersey.

!!! attention ""
    Extensions registered as **singletons**, when no explicit scope annotation is used.
    Behaviour could be disabled with [option](../guide/options.md):
    ```java
    .option(InstallerOptions.ForceSingletonForJerseyExtensions, false)
    ```

!!! tip ""
    Before guicey 5.7.0 it was required to annotate all extensions with `@Provide`, but now
    it is not required - extension would be recognized by implemented interface.
    But, if you prefer legacy behaviour then it could be reverted with:
    ```java
    .option(InstallersOptions.JerseyExtensionsRecognizedByType, false)
    ```

Special `@Prototype` scope annotation may be used to mark resources in prototype scope.
It is useful when [guice servlet support is disabled](../guide/web.md#disable-servletmodule-support) (and so `@RequestScoped` could not be used).

Due to specifics of [HK2 integration](lifecycle.md), you may need to use:

* `#!java @JerseyManaged` to delegate bean creation to HK2
* `#!java @LazyBinding` to delay bean creation to time when all dependencies will be available 
* `jakarta.inject.Provider` as universal workaround (to wrap not immediately available dependency).

Or you can enable [HK2 management for jersey extensions by default](../guide/hk2.md#use-hk2-for-jersey-extensions).
Note that this will affect [resources](resource.md) too and guice aop will not work on jersey extensions.

### Priority

By default, all registered providers are qualified with `@org.glassfish.jersey.internal.inject.Custom` to 
prioritize them (be able to override dropwizard defaults). This *mimics the default behaviour*
of manual registration with `#!java environment.jersey().register(...)`.

For example, when you register your own `ExceptionMapper<Throwable>` it would be used instead
of default dropwizard one (due to prioritized qualification).

For more details see `org.glassfish.jersey.internal.inject.Providers#getAllServiceHolders(
org.glassfish.jersey.internal.inject.InjectionManager, java.lang.Class)` which is used by jersey for providers loading.

!!! tip
    Previously (<= 5.2.0) guicey were not qualifying providers and qualification may (unlikely, but can!)
    introduce behaviour changes on guicey upgrade (due to prioritized custom providers).
    In this case, auto qualification may be disabled with 
    ```java
    .option(InstallerOptions.PrioritizeJerseyExtensions, false) 
    ``` 
    to revert to legacy guicey behaviour.
    `@Custom` may be used directly in this case on some providers for prioritization. 

`@Priority` annotation may be used for ordering providers. Value should be > 0 (but may be negative, just a convention). 
For example, 1000 is prioritized before 2000. See `jakarta.ws.rs.Priorities` for default priority constants.

!!! note
    `@Priority` may work differently on `@Custom` qualified providers (all user providers by default)
    and unqualified (e.g. registered through hk module, like dropwizard defaults). Right now, qualified
    providers sorted ascending while unqualified sorted descending (due to different selection implementations,
    see `getAllServiceHolders` reference above). Probably a jersey bug.

### Supplier

!!! warning
    `Supplier` is used now by hk2 as a replacement to its own `Factory` interface.
    
    If you were using `AbstractContainerRequestValueFactory` then use just `Supplier<T>` instead.

Any class implementing `#!java java.util.function.Supplier` (or extending abstract class implementing it).

```java
public class MySupplier implements Supplier<MyModel> {
    @Override
    public MyModel get() {
       ...    
    }   
}
```

!!! tip ""
    Suppliers in essence are very like guice (or `jakarta.inject`) providers (`#!java Provider`).

!!! warning
    Previously, factories were used as auth objects providers. Now `Function<ContainerRequest, ?>` must be used instead: 
    
    ```java
    @Provider
    class AuthFactory implements Function<ContainerRequest, User> {
    
        @Override
        public User apply(ContainerRequest containerRequest) {
            return new User();
        }
    }
    ```

### ExceptionMapper

Any class implementing `#!java jakarta.ws.rs.ext.ExceptionMapper` (or extending abstract class implementing it). 
Useful for [error handling customization](https://www.dropwizard.io/en/release-3.0.x/manual/core.html#error-handling).

```java
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

!!! tip
    You can also use `ExtendedExceptionMapper` as more flexible alternative. See example usage in
    [dropwizard-views](https://www.dropwizard.io/en/release-3.0.x/manual/views.html#template-errors).
    
!!! tip
    Default exception dropwizard mappers (registered in `io.dropwizard.setup.ExceptionMapperBinder`) could be 
    [overridden](https://www.dropwizard.io/en/release-3.0.x/manual/core.html#overriding-default-exception-mappers)
    (see [priority section](#priority))
    or completely disabled with `server.registerDefaultExceptionMappers` option.    

### ValueParamProvider

Any class implementing `#!java org.glassfish.jersey.server.spi.internal.ValueParamProvider` (or extending abstract class implementing it).

```java
public class AuthFactoryProvider extends AbstractValueParamProvider {

    private final AuthFactory authFactory;

    @Inject
    public AuthFactoryProvider(final jakarta.inject.Provider<MultivaluedParameterExtractorProvider> extractorProvider,
                               final AuthFactory factory) {
        super(extractorProvider, Parameter.Source.UNKNOWN);
        this.authFactory = factory;
    }

    @Override
    protected Function<ContainerRequest, User> createValueProvider(Parameter parameter) {
        final Auth auth = parameter.getAnnotation(Auth.class);
        return auth != null ? authFactory : null;
    }
}
```

### InjectionResolver

Any class implementing `#!java org.glassfish.hk2.api.InjectionResolver` (or extending abstract class implementing it).

```java
class MyObjInjectionResolver implements InjectionResolver<MyObjAnn> {

    @Override
    public Object resolve(Injectee injectee) {
        return new MyObj();
    }

    @Override
    public Class<MyObjAnn> getAnnotation() {
        return MyObjAnn.class;
    }

    @Override
    public boolean isConstructorParameterIndicator() {
        return false;
    }

    @Override
    public boolean isMethodParameterIndicator() {
        return true;
    }
}
```

### ParamConverterProvider

Any class implementing [`#!java jakarta.ws.rs.ext.ParamConverterProvider`](https://jakarta.ee/specifications/platform/9/apidocs/jakarta/ws/rs/ext/paramconverterprovider) (or extending abstract class implementing it).

```java
public class FooParamConverter implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (Foo.class.isAssignableFrom(rawType)) {
            return (ParamConverter<T>) new FooConverter();
        }
        return null;
    }

    private static class FooConverter implements ParamConverter<Foo> {
        @Override
        public Foo fromString(String value) {
            return new Foo(value);
        }

        @Override
        public String toString(Foo value) {
            return value.value;
        }
    }
}
```

### ContextResolver

Any class implementing [`#!java jakarta.ws.rs.ext.ContextResolver`](https://jakarta.ee/specifications/platform/9/apidocs/jakarta/ws/rs/ext/contextresolver) (or extending abstract class implementing it).

```java
public class MyContextResolver implements ContextResolver<Context> {

    @Override
    public Context getContext(Class type) {
        return new Context();
    }

    public static class Context {}
}
```

### MessageBodyReader

Any class implementing [`#!java jakarta.ws.rs.ext.MessageBodyReader`](https://jakarta.ee/specifications/platform/9/apidocs/jakarta/ws/rs/ext/messagebodyreader) (or extending abstract class implementing it).
Useful for [custom representations](https://www.dropwizard.io/en/release-3.0.x/manual/core.html#custom-representations).

```java
public class TypeMessageBodyReader implements MessageBodyReader<Type> {

    @Override
    public boolean isReadable(Class<?> type, java.lang.reflect.Type genericType, Annotation[] annotations, MediaType mediaType) {
        return false;
    }

    @Override
    public Type readFrom(Class<Type> type, java.lang.reflect.Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return null;
    }

    public static class Type {}
}
```

### MessageBodyWriter

Any class implementing [`#!java jakarta.ws.rs.ext.MessageBodyWriter`](https://jakarta.ee/specifications/platform/9/apidocs/jakarta/ws/rs/ext/messagebodywriter) (or extending abstract class implementing it).
Useful for [custom representations](https://www.dropwizard.io/en/release-3.0.x/manual/core.html#custom-representations).

```java
public class TypeMessageBodyWriter implements MessageBodyWriter<Type> {

    @Override
    public boolean isWriteable(Class<?> type, java.lang.reflect.Type genericType, Annotation[] annotations, MediaType mediaType) {
        return false;
    }

    @Override
    public long getSize(Type type, Class<?> type2, java.lang.reflect.Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(Type type, Class<?> type2, java.lang.reflect.Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    }

    public static class Type {}
}
```

### ReaderInterceptor

Any class implementing [`#!java jakarta.ws.rs.ext.ReaderInterceptor`](https://jakarta.ee/specifications/platform/9/apidocs/jakarta/ws/rs/ext/readerinterceptor) (or extending abstract class implementing it).

```java
public class MyReaderInterceptor implements ReaderInterceptor {

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        return null;
    }
}
```

### WriterInterceptor

Any class implementing [`#!java jakarta.ws.rs.ext.WriterInterceptor`](https://jakarta.ee/specifications/platform/9/apidocs/jakarta/ws/rs/ext/writerinterceptor) (or extending abstract class implementing it).

```java
public class MyWriterInterceptor implements WriterInterceptor {

    @Override
    void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
    }
}
```

### ContainerRequestFilter

Any class implementing [`#!java jakarta.ws.rs.container.ContainerRequestFilter`](https://jakarta.ee/specifications/platform/9/apidocs/jakarta/ws/rs/container/containerrequestfilter) (or extending abstract class implementing it).
Useful for [request modifications](https://www.dropwizard.io/en/release-3.0.x/manual/core.html#jersey-filters).

```java
public class MyContainerRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
    }
}
```

### ContainerResponseFilter

Any class implementing [`#!java jakarta.ws.rs.container.ContainerResponseFilter`](https://jakarta.ee/specifications/restful-ws/3.0/apidocs/jakarta/ws/rs/container/containerresponsefilter) (or extending abstract class implementing it).
Useful for [response modifications](https://www.dropwizard.io/en/release-3.0.x/manual/core.html#jersey-filters).

```java
public class MyContainerResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    }
}
```

### DynamicFeature

Any class implementing [`#!java jakarta.ws.rs.container.DynamicFeature`](https://jakarta.ee/specifications/restful-ws/3.0/apidocs/jakarta/ws/rs/container/dynamicfeature) (or extending abstract class implementing it).
Useful for conditional [activation of filters](https://www.dropwizard.io/en/release-3.0.x/manual/core.html#jersey-filters).

```java
public class MyDynamicFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    }
}
```

### ApplicationEventListener

Any class implementing [`#!java org.glassfish.jersey.server.monitoring.ApplicationEventListener`](https://jersey.java.net/apidocs/2.9/jersey/org/glassfish/jersey/server/monitoring/ApplicationEventListener.html) (or extending abstract class implementing it).

```java
public class MyApplicationEventListener implements ApplicationEventListener {

    @Override
    public void onEvent(ApplicationEvent event) {
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return null;
    }
}
```

### ModelProcessor

Any class implementing [`#!java org.glassfish.jersey.server.model.ModelProcessor`](https://eclipse-ee4j.github.io/jersey.github.io/apidocs/2.29.1/jersey/org/glassfish/jersey/server/model/ModelProcessor.html) (or extending abstract class implementing it).

```java
public class MyModelProcessor implements ModelProcessor {

    @Override
    public ResourceModel processResourceModel(ResourceModel resourceModel, 
                                              Configuration configuration) {
        return resourceModel;
    }

    @Override
    public ResourceModel processSubResource(ResourceModel subResourceModel, 
                                            Configuration configuration) {
        return subResourceModel;
    }
}
```
