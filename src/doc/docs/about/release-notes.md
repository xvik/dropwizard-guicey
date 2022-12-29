# 5.7.0 Release Notes

!!! summary ""
    [5.6.1 release notes](http://xvik.github.io/dropwizard-guicey/5.6.1/about/release-notes/)

* Update to dropwizard 2.1.4
* Guicey reports use WARN level now instead of INFO
* Classpath scan configuration shortcut
* @Provide not required for jersey extensions
* ModelProcessor jersey extension support
* Extensions help
* Support application instance reuse between tests
* SBOM

## Classpath scan configuration shortcut

Classpath scan (auto configuration) for application package may now be configured with no-args shortcut:

```java
GuiceBundle.builder()
        .enableAutoConfig()
```

This is equivalent to:

```java
GuiceBundle.builder()
        .enableAutoConfig(getClass().getPackage().getName())
```

## @Provide not required for jersey extensions

It is *not required* anymore to put `@Provide` annotation on extensions implementing:

* ExceptionMapper
* ParamConverterProvider
* ContextResolver
* MessageBodyReader
* MessageBodyWriter
* ReaderInterceptor
* WriterInterceptor
* ContainerRequestFilter
* ContainerResponseFilter
* DynamicFeature
* ValueParamProvider
* InjectionResolver
* ApplicationEventListener
* ModelProcessor

!!! note
    Existing extensions with `@Provide` annotation would work as before, annotation is just optional now.

New behaviour may be switched off with a new option:

```java
GuiceBundle.builder()
   .option(InstallersOptions.JerseyExtensionsRecognizedByType, false)
```

Switching off might be required if you want to avoid not annotated extensions resolution by 
classpath scan (edge case).

## ModelProcessor jersey extension support

Extension implementing `org.glassfish.jersey.server.model.ModelProcessor` would be recognized and installed now
(classpath scan or manual installation). Note that `@Provide` annotation is not required for recognition.

```java
public class MyModelProcessor implements ModelProcessor { ... }

```

## Extensions help

New report showing recognized extension signs for registered installers:

```java
GuiceBundle.builder()
        .printExtensionsHelp()
```

```
WARN  [2022-12-28 14:57:01,445] ru.vyarus.dropwizard.guice.debug.ExtensionsHelpDiagnostic: Recognized extension signs

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

## Support application instance reuse between tests

In order to use the same application instance for multiple tests, junit extension must be declared in
BASE test class with `reuseApplication` flag enabled. 

Either with annotation:

```java
@TestGuiceyApp(value = Application.class, reuseApplication = true)
public abstract class BaseTest {}
```

or manually:

```java
public abstract class BaseTest {
    @RegisterExtension
    static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(App.class)
            .reuseApplication()
            .create();
    
}
```

And the same for dropwizard extension (`@TestDropwizardApp` and `TestDropwizardAppExtension`).

!!! important
    Application instance re-use is not enabled by default for backwards compatibility:
    if you already have base class with declared extension, it will work the same as before. 

All tests extending base class would use the same instance:

```java
public class Test1 extends BaseTest { }
```

Such "global" application would be closed after all tests execution (with test engine shutdown).

In essence, reusable application "stick" to declaration in base class, so all tests,
extending base class "inherit" the same declaration and so the same application (when reuse enabled).

You may have multiple reusable applications if you declare multiple base classes:
in this case, tests would use application "attached" to extended base class.

!!! tip
    Reusable applications may be used together with tests, not extending base class
    and using guicey extensions. Such tests would simply start a new application instance.
    Just be sure to avoid port clashes when using reusable dropwizard apps (by using `randomPorts` option). 

`@EnableSetup` and `@EnableHook` fields are also supported for reusable applications.
But declare all such fields on base class level (or below) because otherwise only fields
declared on first started test would be used. Warning would be printed if such fields used
(or ignored because reusable app was already started by different test).

## SBOM

Json and xml SBOMs are now published with `cyclonedx` classifier (same way as dropwizard):

[View published files](https://repo1.maven.org/maven2/ru/vyarus/dropwizard-guicey/5.7.0/)

Also, SBOMs published for all ext modules.

