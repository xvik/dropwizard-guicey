# Extensions help

Shows extension signs recognized by registered installers.

```java
GuiceBundle.builder()
    ...
    .printExtensionsHelp()
    .build()
``` 

Example report:

```
INFO  [2022-12-28 14:57:01,445] ru.vyarus.dropwizard.guice.debug.ExtensionsHelpDiagnostic: Recognized extension signs

    lifecycle            (r.v.d.g.m.i.f.LifeCycleInstaller)     
        implements LifeCycle

    managed              (r.v.d.g.m.i.feature.ManagedInstaller) 
        implements Managed

    jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) 
        implements Feature

    jerseyprovider       (r.v.d.g.m.i.f.j.p.JerseyProviderInstaller) 
        @Provider on class
        implements ExceptionMapper
        implements ParamConverterProvider
        implements ContextResolver
        implements MessageBodyReader
        implements MessageBodyWriter
        implements ReaderInterceptor
        implements WriterInterceptor
        implements ContainerRequestFilter
        implements ContainerResponseFilter
        implements DynamicFeature
        implements ValueParamProvider
        implements InjectionResolver
        implements ApplicationEventListener
        implements ModelProcessor

    resource             (r.v.d.g.m.i.f.j.ResourceInstaller)    
        @Path on class
        @Path on implemented interface

    eagersingleton       (r.v.d.g.m.i.f.e.EagerSingletonInstaller) 
        @EagerSingleton on class

    healthcheck          (r.v.d.g.m.i.f.h.HealthCheckInstaller) 
        extends NamedHealthCheck

    task                 (r.v.d.g.m.i.feature.TaskInstaller)    
        extends Task

    plugin               (r.v.d.g.m.i.f.plugin.PluginInstaller) 
        @Plugin on class
        custom annotation on class, annotated with @Plugin

    webservlet           (r.v.d.g.m.i.f.w.WebServletInstaller)  
        extends HttpServlet + @WebServlet

    webfilter            (r.v.d.g.m.i.f.web.WebFilterInstaller) 
        implements Filter + @WebFilter

    weblistener          (r.v.d.g.m.i.f.w.l.WebListenerInstaller) 
        implements EventListener + @WebListener
```

All signs are grouped by installer. Installers listed in processing order, which is important, 
because first installer recognized extension "owns" it (even if extension contains signs, recognizable by other installers).

## Custom installers

Custom installers should implement new method to participate in report (not required!).
Example implementation from singleton installer:

```java
public class EagerSingletonInstaller implements FeatureInstaller {
    ...
    
    @Override
    public List<String> getRecognizableSigns() {
        return Collections.singletonList("@" + EagerSingleton.class.getSimpleName() + " on class");
    }
}
```