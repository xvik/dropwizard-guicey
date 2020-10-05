# Jersey config report

Report shows all registered jersey extensions, including registered by dropwizard and
all manual registrations.

```java
GuiceBundle.builder()
    ...
    .printJerseyConfig() 
    .build()
```      

Example report:

```
INFO  [2019-10-28 06:16:44,068] ru.vyarus.dropwizard.guice.debug.JerseyConfigDiagnostic: Jersey configuration = 

    Exception mappers
        Throwable                      ExceptionMapperBinder$1      (io.dropwizard.setup)      
        EofException                   EarlyEofExceptionMapper      (i.d.jersey.errors)        
        EmptyOptionalException         EmptyOptionalExceptionMapper (i.d.jersey.optional)      
        IOException                    GuiceExceptionMapper         (r.v.d.g.c.h.support)      
        IOException                    HKExceptionMapper            (r.v.d.g.c.h.s.hk)         *jersey managed
        IllegalStateException          IllegalStateExceptionMapper  (i.d.jersey.errors)        
        JerseyViolationException       JerseyViolationExceptionMapper (i.d.j.validation)         
        JsonProcessingException        JsonProcessingExceptionMapper (i.d.jersey.jackson)       
        ValidationException            ValidationExceptionMapper    (o.g.j.s.v.internal)       

    Param converters
        AbstractParamConverterProvider (i.d.jersey.params)        
        FuzzyEnumParamConverterProvider (i.d.j.validation)         
        GuiceParamConverterProvider  (r.v.d.g.c.h.support)      
        HKParamConverterProvider     (r.v.d.g.c.h.s.hk)         *jersey managed
        RootElementProvider          (o.g.j.j.i.JaxbStringReaderProvider) 
        OptionalDoubleParamConverterProvider (i.d.jersey.optional)      
        OptionalIntParamConverterProvider (i.d.jersey.optional)      
        OptionalLongParamConverterProvider (i.d.jersey.optional)      
        OptionalParamConverterProvider (i.d.jersey.guava)         
        OptionalParamConverterProvider (i.d.jersey.optional)      
        AggregatedProvider           (o.g.j.i.i.ParamConverters) 

    Context resolvers
        Context                        GuiceContextResolver         (r.v.d.g.c.h.support)      
        Context                        HKContextResolver            (r.v.d.g.c.h.s.hk)         *jersey managed

    Message body readers
        Object                         BasicTypesMessageProvider    (o.g.j.m.internal)                       [text/plain]
        byte[]                         ByteArrayProvider            (o.g.j.m.internal)                       [application/octet-stream, */*]
        DataSource                     DataSourceProvider           (o.g.j.m.internal)                       [application/octet-stream, */*]
        Document                       DocumentProvider             (o.g.j.jaxb.internal)                    [application/xml, text/xml, */*]
        File                           FileProvider                 (o.g.j.m.internal)                       [application/octet-stream, */*]
        MultivaluedMap<String, String> FormMultivaluedMapProvider   (o.g.j.m.internal)                       [application/x-www-form-urlencoded]
        Form                           FormProvider                 (o.g.j.m.internal)                       [application/x-www-form-urlencoded, */*]
        Type                           GuiceMessageBodyReader       (r.v.d.g.c.h.support)                    
        Type                           HKMessageBodyReader          (r.v.d.g.c.h.s.hk)         *jersey managed 
        InputStream                    InputStreamProvider          (o.g.j.m.internal)                       [application/octet-stream, */*]
        Object                         JacksonJsonProvider          (c.f.j.jaxrs.json)                       [*/*]
        Object                         JacksonMessageBodyProvider   (i.d.jersey.jackson)                     [*/*]
        Reader                         ReaderProvider               (o.g.j.m.internal)                       [text/plain, */*]
        RenderedImage                  RenderedImageProvider        (o.g.j.m.internal)                       [image/*, application/octet-stream]
        StreamSource                   StreamSourceReader           (o.g.j.m.i.SourceProvider)               [application/xml, text/xml, */*]
        SAXSource                      SaxSourceReader              (o.g.j.m.i.SourceProvider)               [application/xml, text/xml, */*]
        DOMSource                      DomSourceReader              (o.g.j.m.i.SourceProvider)               [application/xml, text/xml, */*]
        String                         StringMessageProvider        (o.g.j.m.internal)                       [text/plain, */*]
        T[], Collection<T>             App                          (o.g.j.j.i.XmlCollectionJaxbProvider)    [application/xml]
        T[], Collection<T>             Text                         (o.g.j.j.i.XmlCollectionJaxbProvider)    [text/xml]
        T[], Collection<T>             General                      (o.g.j.j.i.XmlCollectionJaxbProvider)    [*/*]
        JAXBElement<Object>            App                          (o.g.j.j.i.XmlJaxbElementProvider)       [application/xml]
        JAXBElement<Object>            Text                         (o.g.j.j.i.XmlJaxbElementProvider)       [text/xml]
        JAXBElement<Object>            General                      (o.g.j.j.i.XmlJaxbElementProvider)       [*/*,*/*+xml]
        Object                         App                          (o.g.j.j.i.XmlRootElementJaxbProvider)   [application/xml]
        Object                         Text                         (o.g.j.j.i.XmlRootElementJaxbProvider)   [text/xml]
        Object                         General                      (o.g.j.j.i.XmlRootElementJaxbProvider)   [*/*]
        Object                         App                          (o.g.j.j.i.XmlRootObjectJaxbProvider)    [application/xml]
        Object                         Text                         (o.g.j.j.i.XmlRootObjectJaxbProvider)    [text/xml]
        Object                         General                      (o.g.j.j.i.XmlRootObjectJaxbProvider)    [*/*]

    Message body writers
        Object                         BasicTypesMessageProvider    (o.g.j.m.internal)                       [text/plain]
        byte[]                         ByteArrayProvider            (o.g.j.m.internal)                       [application/octet-stream, */*]
        ChunkedOutput<Object>          ChunkedResponseWriter        (o.g.jersey.server)                      
        DataSource                     DataSourceProvider           (o.g.j.m.internal)                       [application/octet-stream, */*]
        Document                       DocumentProvider             (o.g.j.jaxb.internal)                    [application/xml, text/xml, */*]
        File                           FileProvider                 (o.g.j.m.internal)                       [application/octet-stream, */*]
        MultivaluedMap<String, String> FormMultivaluedMapProvider   (o.g.j.m.internal)                       [application/x-www-form-urlencoded]
        Form                           FormProvider                 (o.g.j.m.internal)                       [application/x-www-form-urlencoded, */*]
        Type                           GuiceMessageBodyWriter       (r.v.d.g.c.h.support)                    
        Type                           HKMessageBodyWriter          (r.v.d.g.c.h.s.hk)         *jersey managed 
        InputStream                    InputStreamProvider          (o.g.j.m.internal)                       [application/octet-stream, */*]
        Object                         JacksonJsonProvider          (c.f.j.jaxrs.json)                       [*/*]
        Object                         JacksonMessageBodyProvider   (i.d.jersey.jackson)                     [*/*]
        OptionalDouble                 OptionalDoubleMessageBodyWriter (i.d.jersey.optional)                 [*/*]
        OptionalInt                    OptionalIntMessageBodyWriter (i.d.jersey.optional)                    [*/*]
        OptionalLong                   OptionalLongMessageBodyWriter (i.d.jersey.optional)                   [*/*]
        Optional<Object>               OptionalMessageBodyWriter    (i.d.jersey.guava)                       [*/*]
        Optional<Object>               OptionalMessageBodyWriter    (i.d.jersey.optional)                    [*/*]
        Reader                         ReaderProvider               (o.g.j.m.internal)                       [text/plain, */*]
        RenderedImage                  RenderedImageProvider        (o.g.j.m.internal)                       [image/*]
        Source                         SourceWriter                 (o.g.j.m.i.SourceProvider)               [application/xml, text/xml, */*]
        StreamingOutput                StreamingOutputProvider      (o.g.j.m.internal)                       [application/octet-stream, */*]
        String                         StringMessageProvider        (o.g.j.m.internal)                       [text/plain, */*]
        Object                         ValidationErrorMessageBodyWriter (o.g.j.s.v.internal)                 
        T[], Collection<T>             App                          (o.g.j.j.i.XmlCollectionJaxbProvider)    [application/xml]
        T[], Collection<T>             Text                         (o.g.j.j.i.XmlCollectionJaxbProvider)    [text/xml]
        T[], Collection<T>             General                      (o.g.j.j.i.XmlCollectionJaxbProvider)    [*/*]
        JAXBElement<Object>            App                          (o.g.j.j.i.XmlJaxbElementProvider)       [application/xml]
        JAXBElement<Object>            Text                         (o.g.j.j.i.XmlJaxbElementProvider)       [text/xml]
        JAXBElement<Object>            General                      (o.g.j.j.i.XmlJaxbElementProvider)       [*/*,*/*+xml]
        Object                         App                          (o.g.j.j.i.XmlRootElementJaxbProvider)   [application/xml]
        Object                         Text                         (o.g.j.j.i.XmlRootElementJaxbProvider)   [text/xml]
        Object                         General                      (o.g.j.j.i.XmlRootElementJaxbProvider)   [*/*]

    Reader interceptors
        MappableExceptionWrapperInterceptor (o.g.j.s.internal)         
        GuiceReaderInterceptor       (r.v.d.g.c.h.support)      
        HKReaderInterceptor          (r.v.d.g.c.h.s.hk)         *jersey managed

    Writer interceptors
        MappableExceptionWrapperInterceptor (o.g.j.s.internal)         
        JsonWithPaddingInterceptor   (o.g.j.s.internal)         
        GuiceWriterInterceptor       (r.v.d.g.c.h.support)      
        HKWriterInterceptor          (r.v.d.g.c.h.s.hk)         *jersey managed

    Container request filters
        GuiceContainerRequestFilter  (r.v.d.g.c.h.support)      
        HKContainerRequestFilter     (r.v.d.g.c.h.s.hk)         *jersey managed

    Container response filters
        GuiceContainerResponseFilter (r.v.d.g.c.h.support)      
        HKContainerResponseFilter    (r.v.d.g.c.h.s.hk)         *jersey managed

    Dynamic features
        CacheControlledResponseFeature (i.d.jersey.caching)       
        GuiceDynamicFeature          (r.v.d.g.c.h.support)      
        HKDynamicFeature             (r.v.d.g.c.h.s.hk)         *jersey managed

    Param value providers
        AsyncResponseValueParamProvider (o.g.j.s.i.inject)         
        BeanParamValueParamProvider  (o.g.j.s.i.inject)         
        CookieParamValueParamProvider (o.g.j.s.i.inject)         
        DelegatedInjectionValueParamProvider (o.g.j.s.i.inject)         
        EntityParamValueParamProvider (o.g.j.s.i.inject)         
        FormParamValueParamProvider  (o.g.j.s.i.inject)         
        GuiceValueParamProvider      (r.v.d.g.c.h.support)      
        HKValueParamProvider         (r.v.d.g.c.h.s.hk)         *jersey managed
        HeaderParamValueParamProvider (o.g.j.s.i.inject)         
        MatrixParamValueParamProvider (o.g.j.s.i.inject)         
        PathParamValueParamProvider  (o.g.j.s.i.inject)         
        QueryParamValueParamProvider (o.g.j.s.i.inject)         
        SessionFactoryProvider       (i.d.jersey.sessions)      
        WebTargetValueParamProvider  (o.g.j.s.i.inject)         

    Injection resolvers
        @Context                        ContextInjectionResolverImpl (o.g.j.inject.hk2)         
        @Ann                            GuiceInjectionResolver       (r.v.d.g.c.h.support)      
        @Ann                            HKInjectionResolver          (r.v.d.g.c.h.s.hk)         *jersey managed
        @Suspended                      ParamInjectionResolver       (o.g.j.s.i.inject)          using AsyncResponseValueParamProvider 
        @CookieParam                    ParamInjectionResolver       (o.g.j.s.i.inject)          using CookieParamValueParamProvider 
        @FormParam                      ParamInjectionResolver       (o.g.j.s.i.inject)          using FormParamValueParamProvider 
        @HeaderParam                    ParamInjectionResolver       (o.g.j.s.i.inject)          using HeaderParamValueParamProvider 
        @MatrixParam                    ParamInjectionResolver       (o.g.j.s.i.inject)          using MatrixParamValueParamProvider 
        @PathParam                      ParamInjectionResolver       (o.g.j.s.i.inject)          using PathParamValueParamProvider 
        @QueryParam                     ParamInjectionResolver       (o.g.j.s.i.inject)          using QueryParamValueParamProvider 
        @Uri                            ParamInjectionResolver       (o.g.j.s.i.inject)          using WebTargetValueParamProvider 
        @BeanParam                      ParamInjectionResolver       (o.g.j.s.i.inject)          using BeanParamValueParamProvider 
```

!!! warning
    Items order does not represent actual priority order!
    Items in each section are sorted according to `@Priority` annotation and then by name,
    but `@Custom` qualification priority is ignored (and so user extensions and default one may be shown in incorrect order).
    Report sorting applied only for consistency (always show the same order) - it's a trade off.

## Common markers

Markers are shown at the end of extension line. The following markers are common for all extension types.

### Filter

Jersey extension scope may be reduced with `@NameBinding` annotation. Custom annotation may 
be created (declared as `@NameBinding`):

```java   
@NameBinding
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterAnn {}
```                                     

Now if extension will be annotated with it:

```java
@Provider
@FilterAnn
public class ExMapper implements ExceptionMapper<IOException> {}
```

Will be applied **only** to resources, annotated with `@FilterAnn`.

Report will identify scoped extensions with marker:

```
IOException                    ExMapper                     (r.v.d.g.d.r.j.FilterMarkerRenderTest) *only @FilterAnn
```        

### Lazy

Extensions annotated with `@LazyBinding` are identified with marker:

```  
IOException                    ExMapper                     (r.v.d.g.d.r.j.LazyRenderTest) *lazy
```   

### Jersey managed

Extensions, [managed by HK2](../hk2.md#hk2-delegation) are also identified with marker:

```
@Ann                            HKInjectionResolver          (r.v.d.g.c.h.s.hk)         *jersey managed
```

## Exception mappers

Extended exception mappers (these mappers may decide if they accept exception handling):

```java
@Provider
public class ExtMapper implements ExtendedExceptionMapper<IOException> {
    @Override
    public boolean isMappable(IOException exception) {
        return false;
    }

    @Override
    public Response toResponse(IOException exception) {
        return null;
    }
}
```    

Are identified in report:

```
OException                    ExtMapper                    (r.v.d.g.d.r.j.ExtendedExceptionMapperRenderTest) *extended
```    

## Report customization

Report is implemented as guicey [event listener](../events.md) and provide additional customization 
options, so if default configuration (from shortcut methods above) does not fit your needs
you can register listener directly with required configuration.

For example, to show only exception mappers:

```java    
listen(new JerseyConfigDiagnostic(new JerseyConfig()
                    .showExceptionMappers()))
```

Report rendering logic may also be used directly as report provide separate renderer object
implementing `ReportRenderer`. Renderer not bound to guice context and assume direct instantiation.