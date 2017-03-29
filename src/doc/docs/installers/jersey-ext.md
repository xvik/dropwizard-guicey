# Jersey extension installer

!!! summary ""
    CoreInstallersBundle / [JerseyProviderInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/provider/JerseyProviderInstaller.java)        

Installs various jersey extensions, usually annotated with jersey `#!java @Provider` annotation and installed via `#!java environment.jersey().register()`:

    Factory, ExceptionMapper, ValueFactoryProvider, InjectionResolver, 
    ParamConverterProvider, ContextResolver, MessageBodyReader, MessageBodyWriter, 
    ReaderInterceptor, WriterInterceptor, ContainerRequestFilter, 
    ContainerResponseFilter, DynamicFeature, ApplicationEventListener

## Recognition

Detects  classes annotated with jersey `@javax.ws.rs.ext.Provider` annotation and register their instances in jersey.

!!! attention ""
    Extensions registered as **singletons**, even if guice bean scope isn't set.  

Due to specifics of [HK integration](lifecycle.md), you may need to use:

* `#!java @HK2Managed` to delegate bean creation to HK
* `#!java @LazyBinding` to delay bean creation to time when all dependencies will be available 
* `javax.inject.Provider` as universal workaround (to wrap not immediately available dependency).

### Factory

Any class implementing `#!java org.glassfish.hk2.api.Factory` (or extending abstract class implementing it).

```java
@Provider
public class AuthFactory implements Factory<User>{

    @Override
    public User provide() {
        return new User();
    }

    @Override
    public void dispose(User instance) {
    }
}
```

!!! tip ""
    Factories in essence are very like guice (or javax.inject) providers (`#!java Provider`).

Example of using jersey abstract class instead of direct implementation:

```java
@Provider
public class LocaleInjectableProvider extends AbstractContainerRequestValueFactory<Locale> {

    @Inject
    private javax.inject.Provider<HttpHeaders> request;

    @Override
    public Locale provide() {
        final List<Locale> locales = request.get().getAcceptableLanguages();
        return locales.isEmpty() ? Locale.US : locales.get(0);
    }
}
```

### ExceptionMapper

Any class implementing `#!java javax.ws.rs.ext.ExceptionMapper` (or extending abstract class implementing it). 
Useful for [error handling customization](http://www.dropwizard.io/1.0.6/docs/manual/core.html#error-handling).

```java
@Provider
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

### ValueFactoryProvider

Any class implementing `#!java org.glassfish.jersey.server.spi.internal.ValueFactoryProvider` (or extending abstract class implementing it).

```java
@Provider
@LazyBinding 
public class AuthFactoryProvider extends AbstractValueFactoryProvider {

    private final Factory<User> authFactory;

    @Inject
    public AuthFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider,
                               final AuthFactory factory, 
                               final ServiceLocator injector) {
        super(extractorProvider, injector, Parameter.Source.UNKNOWN);
        this.authFactory = factory;
    }

    @Override
    protected Factory<?> createValueFactory(Parameter parameter) {
        final Auth auth = parameter.getAnnotation(Auth.class);
        return auth != null ? authFactory : null;
    }
}
```

!!! note
    `#!java @LazyBinding` was used to delay provider creation because required dependency `#!java MultivaluedParameterExtractorProvider`
    (by super class) will be available only after HK context creation (which is created after guice context). 
    Another option could be using `#!java @HK2Managed` (instead of lazy) which will delegate bean creation to hk.

### InjectionResolver

Any class implementing `#!java org.glassfish.hk2.api.InjectionResolver` (or extending abstract class implementing it).

```java
@Provider
@LazyBinding
public class AuthInjectionResolver extends ParamInjectionResolver<Auth> {
    
    public AuthInjectionResolver() {
        super(AuthFactoryProvider.class);
    }
}
```

!!! note
    `#!java @LazyBinding` was used to delay provider creation because super class will require hk service locator, 
    which is not yet available. `#!java @HK2Managed` could also be used instead.

### ParamConverterProvider

Any class implementing [`#!java javax.ws.rs.ext.ParamConverterProvider`](https://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/ParamConverterProvider.html) (or extending abstract class implementing it).

```java
@Provider
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

Any class implementing [`#!java javax.ws.rs.ext.ContextResolver`](https://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/ContextResolver.html) (or extending abstract class implementing it).

```java
@Provider
public class MyContextResolver implements ContextResolver<Context> {

    @Override
    public Context getContext(Class type) {
        return new Context();
    }

    public static class Context {}
}
```

### MessageBodyReader

Any class implementing [`#!java javax.ws.rs.ext.MessageBodyReader`](https://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/MessageBodyReader.html) (or extending abstract class implementing it).
Useful for [custom representations](http://www.dropwizard.io/1.0.6/docs/manual/core.html#custom-representations).

```java
@Provider
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

Any class implementing [`#!java javax.ws.rs.ext.MessageBodyWriter`](https://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/MessageBodyWriter.html) (or extending abstract class implementing it).
Useful for [custom representations](http://www.dropwizard.io/1.0.6/docs/manual/core.html#custom-representations).

```java
@Provider
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

Any class implementing [`#!java javax.ws.rs.ext.ReaderInterceptor`](https://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/ReaderInterceptor.html) (or extending abstract class implementing it).

```java
@Provider
public class MyReaderInterceptor implements ReaderInterceptor {

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        return null;
    }
}
```

### WriterInterceptor

Any class implementing [`#!java javax.ws.rs.ext.WriterInterceptor`](https://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/WriterInterceptor.html) (or extending abstract class implementing it).

```java
@Provider
public class MyWriterInterceptor implements WriterInterceptor {

    @Override
    void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
    }
}
```

### ContainerRequestFilter

Any class implementing [`#!java javax.ws.rs.container.ContainerRequestFilter`](https://docs.oracle.com/javaee/7/api/javax/ws/rs/container/ContainerRequestFilter.html) (or extending abstract class implementing it).
Useful for [request modifications](http://www.dropwizard.io/1.0.6/docs/manual/core.html#jersey-filters).

```java
@Provider
public class MyContainerRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
    }
}
```

### ContainerResponseFilter

Any class implementing [`#!java javax.ws.rs.container.ContainerResponseFilter`](https://docs.oracle.com/javaee/7/api/javax/ws/rs/container/ContainerResponseFilter.html) (or extending abstract class implementing it).
Useful for [response modifications](http://www.dropwizard.io/1.0.6/docs/manual/core.html#jersey-filters).

```java
@Provider
public class MyContainerResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    }
}
```

### DynamicFeature

Any class implementing [`#!java javax.ws.rs.container.DynamicFeature`](https://docs.oracle.com/javaee/7/api/javax/ws/rs/container/DynamicFeature.html) (or extending abstract class implementing it).
Useful for conditional [activation of filters](http://www.dropwizard.io/1.0.6/docs/manual/core.html#jersey-filters).

```java
@Provider
public class MyDynamicFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    }
}
```

### ApplicationEventListener

Any class implementing [`#!java org.glassfish.jersey.server.monitoring.ApplicationEventListener`](https://jersey.java.net/apidocs/2.9/jersey/org/glassfish/jersey/server/monitoring/ApplicationEventListener.html) (or extending abstract class implementing it).

```java
@Provider
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
