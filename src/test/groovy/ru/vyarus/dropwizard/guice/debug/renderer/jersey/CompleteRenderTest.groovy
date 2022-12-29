package ru.vyarus.dropwizard.guice.debug.renderer.jersey

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.glassfish.jersey.internal.inject.InjectionManager
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.report.jersey.JerseyConfig
import ru.vyarus.dropwizard.guice.debug.report.jersey.JerseyConfigRenderer
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 27.10.2019
 */
@TestDropwizardApp(App)
class CompleteRenderTest extends Specification {
    static {
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    @Inject
    InjectionManager manager
    JerseyConfigRenderer renderer

    void setup() {
        renderer = new JerseyConfigRenderer(manager, true)
    }

    def "Check full jersey report render"() {

        expect:
        render(new JerseyConfig()) == """

    Exception mappers
        Throwable                      ExceptionMapperBinder\$1      (io.dropwizard.setup)
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
        OptionalParamConverterProvider (i.d.jersey.guava)
        AggregatedProvider           (o.g.j.i.i.ParamConverters)

    Context resolvers
        Context                        GuiceContextResolver         (r.v.d.g.c.h.support)
        Context                        HKContextResolver            (r.v.d.g.c.h.s.hk)         *jersey managed

    Message body readers
        Object                         BasicTypesMessageProvider    (o.g.j.m.internal)                       [text/plain]
        byte[]                         ByteArrayProvider            (o.g.j.m.internal)                       [application/octet-stream, */*]
        DataSource                     DataSourceProvider           (o.g.j.m.internal)                       [application/octet-stream, */*]
        Document                       DocumentProvider             (o.g.j.jaxb.internal)                    [application/xml, text/xml, */*]
        Enum                           EnumMessageProvider          (o.g.j.m.internal)                       [text/plain]
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
        DOMSource                      DomSourceReader              (o.g.j.m.i.SourceProvider)               [application/xml, text/xml, */*]
        SAXSource                      SaxSourceReader              (o.g.j.m.i.SourceProvider)               [application/xml, text/xml, */*]
        StreamSource                   StreamSourceReader           (o.g.j.m.i.SourceProvider)               [application/xml, text/xml, */*]
        String                         StringMessageProvider        (o.g.j.m.internal)                       [text/plain, */*]
        T[], Collection<T>             App                          (o.g.j.j.i.XmlCollectionJaxbProvider)    [application/xml]
        T[], Collection<T>             General                      (o.g.j.j.i.XmlCollectionJaxbProvider)    [*/*]
        T[], Collection<T>             Text                         (o.g.j.j.i.XmlCollectionJaxbProvider)    [text/xml]
        JAXBElement<Object>            App                          (o.g.j.j.i.XmlJaxbElementProvider)       [application/xml]
        JAXBElement<Object>            General                      (o.g.j.j.i.XmlJaxbElementProvider)       [*/*,*/*+xml]
        JAXBElement<Object>            Text                         (o.g.j.j.i.XmlJaxbElementProvider)       [text/xml]
        Object                         App                          (o.g.j.j.i.XmlRootElementJaxbProvider)   [application/xml]
        Object                         General                      (o.g.j.j.i.XmlRootElementJaxbProvider)   [*/*]
        Object                         Text                         (o.g.j.j.i.XmlRootElementJaxbProvider)   [text/xml]
        Object                         App                          (o.g.j.j.i.XmlRootObjectJaxbProvider)    [application/xml]
        Object                         General                      (o.g.j.j.i.XmlRootObjectJaxbProvider)    [*/*]
        Object                         Text                         (o.g.j.j.i.XmlRootObjectJaxbProvider)    [text/xml]

    Message body writers
        Object                         BasicTypesMessageProvider    (o.g.j.m.internal)                       [text/plain]
        byte[]                         ByteArrayProvider            (o.g.j.m.internal)                       [application/octet-stream, */*]
        ChunkedOutput<Object>          ChunkedResponseWriter        (o.g.jersey.server)
        DataSource                     DataSourceProvider           (o.g.j.m.internal)                       [application/octet-stream, */*]
        Document                       DocumentProvider             (o.g.j.jaxb.internal)                    [application/xml, text/xml, */*]
        Enum                           EnumMessageProvider          (o.g.j.m.internal)                       [text/plain]
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
        T[], Collection<T>             General                      (o.g.j.j.i.XmlCollectionJaxbProvider)    [*/*]
        T[], Collection<T>             Text                         (o.g.j.j.i.XmlCollectionJaxbProvider)    [text/xml]
        JAXBElement<Object>            App                          (o.g.j.j.i.XmlJaxbElementProvider)       [application/xml]
        JAXBElement<Object>            General                      (o.g.j.j.i.XmlJaxbElementProvider)       [*/*,*/*+xml]
        JAXBElement<Object>            Text                         (o.g.j.j.i.XmlJaxbElementProvider)       [text/xml]
        Object                         App                          (o.g.j.j.i.XmlRootElementJaxbProvider)   [application/xml]
        Object                         General                      (o.g.j.j.i.XmlRootElementJaxbProvider)   [*/*]
        Object                         Text                         (o.g.j.j.i.XmlRootElementJaxbProvider)   [text/xml]

    Reader interceptors
        MappableExceptionWrapperInterceptor (o.g.j.s.internal)
        GuiceReaderInterceptor       (r.v.d.g.c.h.support)
        HKReaderInterceptor          (r.v.d.g.c.h.s.hk)         *jersey managed

    Writer interceptors
        MappableExceptionWrapperInterceptor (o.g.j.s.internal)
        JsonWithPaddingInterceptor   (o.g.j.s.internal)
        GuiceWriterInterceptor       (r.v.d.g.c.h.support)
        HKWriterInterceptor          (r.v.d.g.c.h.s.hk)         *jersey managed
        EofExceptionWriterInterceptor (i.d.jersey.errors)

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
""" as String;
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .enableAutoConfig("ru.vyarus.dropwizard.guice.cases.hkscope.support")
                    .printJerseyConfig()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    String render(JerseyConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }
}
