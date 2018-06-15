# Getting started

!!! note
    Getting started guide briefly covers the most important concepts and commonly used features.
    Advanced description of mentioned topics may be found in user guide.    

## Installation

Available from maven central and [bintray jcenter](https://bintray.com/bintray/jcenter).

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>dropwizard-guicey</artifactId>
  <version>4.1.0</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus:dropwizard-guicey:4.1.0'
```

### BOM

Guicey pom may be also used as maven BOM:

```groovy
plugins {
    id "io.spring.dependency-management" version "1.0.3.RELEASE"
}
dependencyManagement {
    imports {
        mavenBom 'ru.vyarus.guicey:guicey:4.1.0'
    }
}

dependencies {
    compile 'ru.vyarus.guicey:guicey:4.1.0'
   
    // no need to specify versions
    compile 'io.dropwizard:dropwizard-auth'
    compile 'com.google.inject:guice-assistedinject'   
     
    testCompile 'io.dropwizard:dropwizard-test'
    testCompile 'org.spockframework:spock-core'
}
```

Bom includes:

* Dropwizard BOM (io.dropwizard:dropwizard-bom)
* Guice BOM (com.google.inject:guice-bom)
* HK2 bridge (org.glassfish.hk2:guice-bridge) 
* System rules, required for StartupErrorRule (com.github.stefanbirkner:system-rules)
* Spock (org.spockframework:spock-core)

Guicey extensions project provide extended BOM with guicey and all guicey modules included. 
See [extensions project BOM](extras/bom.md) section for more details of BOM usage.

## Usage

!!! note ""
    Full source of example application is [published here](https://github.com/xvik/dropwizard-guicey-examples/tree/master/getting-started)

Register guice bundle:

```java
public class SampleApplication extends Application<Configuration> {

    public static void main(String[] args) throws Exception {
            new SampleApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig(getClass().getPackage().getName())
                .build());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
    }
}
```

!!! tip
    Bundle builder contains shortcuts for all available features, so required function 
    may be found only by looking at available methods (and reading javadoc).

Auto configuration (activated with `enableAutoConfig`) means that guicey will search for extensions in application package and subpackages.

!!! tip
    You can declare multiple packages for classpath scan: 
    ```java
     .enableAutoConfig("com.mycompany.foo", "com.mycompany.bar")
    ```

Application could be launched by simply running main class (assume you will use IDE run command):

```bash
SampleApplication server
```

!!! note
    config.yml is not passed as parameter because we don't need additional configuration now

### Add resource

Creating custom rest resource:

```java
@Path("/sample")
@Produces("application/json")
public class SampleResource {

    @GET
    @Path("/")
    public Response ask() {
        return Response.ok("ok").build();
    }
}
```

Now, when you run application, you can see that resource was installed automatically:

```
INFO  [2017-02-05 11:23:31,188] io.dropwizard.jersey.DropwizardResourceConfig: The following paths were found for the configured resources:

    GET     /sample/ (ru.vyarus.dropwizard.guice.examples.rest.SampleResource)
```

Call `http://localhost:8080/sample/` to make sure it works.

!!! tip
    Rest context is mapped to root by default. To change it use configuration file:
    ```yaml
    server:
        rootPath: '/rest/*'
    ```

Resource is a guice bean, so you can use guice injection here. To accessing request specific
objects like request, response, jersey `javax.ws.rs.core.UriInfo` etc. use `Provider`:

```java
@Path("/sample")
@Produces("application/json")
public class SampleResource {

    @Inject
    private Provider<HttpServletRequest> requestProvider;

    @GET
    @Path("/")
    public Response ask() {
        final String ip = requestProvider.get().getRemoteAddr();
        return Response.ok(ip).build();
    }
}
```

Now resource will return caller IP.

Also you can inject request specific objects with [as method parameter](installers/resource.md#context-usage)

!!! note
    Field injection used in examples for simplicity. In real life projects [prefer constructor injection](https://github.com/google/guice/wiki/Injections).    

### Add managed

[Dropwizard managed objects](http://www.dropwizard.io/1.1.0/docs/manual/core.html#managed-objects) are extremely useful for managing resources.

Create simple managed implementation:

```java
@Singleton
public class SampleBootstrap implements Managed {
    private final Logger logger = LoggerFactory.getLogger(SampleBootstrap.class);

    @Override
    public void start() throws Exception {
        logger.info("Starting some resource");
    }

    @Override
    public void stop() throws Exception {
        logger.info("Shutting down some resource");
    }
}
```

It will be automatically discovered and installed. Guicey always reports installed extensions
(when they are not reported by dropwizard itself). So you can see in startup logs now:

```
INFO  [2017-02-05 11:59:30,750] ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller: managed =

    (ru.vyarus.dropwizard.guice.examples.service.SampleBootstrap)
```

### Add filter

!!! note
    Guice `ServletModule` may be used for servlets and filters definitions, but most of the time it's more convenient
    to use simple servlet annotations (`@WebFilter`, `@WebServlet`, `@WebListener`). 
    Moreover, guice servlet module is not able to register async filters and servlets.

To use `@WebFilter` annotation for filter installation web installers must be activated with shortcut method:

```java
bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig(getClass().getPackage().getName())
                .useWebInstallers()
                .build());
```

Add sample filter around rest methods:

```java
@WebFilter(urlPatterns = "/*")
public class CustomHeaderFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if ("me".equals(request.getParameter("user"))) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response)
                    .sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authorized");
        }
    }

    @Override
    public void destroy() {
    }
}
```

Filter will pass through only requests with `user=me` request parameter. It is used just to show
how to register custom filters with annotations (implementation itself is not useful).

New lines in log will appear confirming filter installation:

```
INFO  [2017-02-11 17:18:16,943] ru.vyarus.dropwizard.guice.module.installer.feature.web.WebFilterInstaller: filters =

    /*                       (ru.vyarus.dropwizard.guice.examples.web.AuthFilter)   .auth
```

Call `http://localhost:8080/sample/` and `http://localhost:8080/sample/?user=me` to make sure filter works.

### Add guice module

If you need to register guice module in injector:

```java
bootstrap.addBundle(GuiceBundle.builder()
                ...
                .modules(new SampleModule())
                .build());
```

Multiple modules could be registered:
```java
.modules(new SampleModule(), new Some3rdPatyModule())
```

!!! note ""
    Guice `ServletModule` could be used for filters and servlets registration.
        
!!! tip ""
    If you have at least one module of your own then it's recommended to move 
    all guice modules registration there to encapsulate guice staff:
    ```java
    .modules(new SampleModule())        
    ```
    ```java
    public class SampleModule extends AbstractModule {
        
        @Override
        protected void configure() {
            install(new Some3rdPatyModule());
            
            // some custom bindings there
        }
    }
    ```
    
!!! warning
    Guicey removes duplicate registrations by type. For example, in case:
    ```java
    .modules(new SampleModule(), new SampleModule())
    ```
    Only one module will be registered. This is intentional restriction to simplify bundles usage.
    
In some cases, it could be desired to use different instances of the same module:
```java
.modules(new ParametrizableModule("mod1"), new ParametrizableModule("mod2"))
```
This will not work (second instance will be dropped). In such cases do registrations in custom
guice module:
```java
install(new ParametrizableModule("mod1"));
install(new ParametrizableModule("mod2"));
```

#### Access dropwizard objects

You may need dropwizard `Configuration` or `Environment` inside module.
For example, to access configuration or to tune dropwizard environment.

In most cases simply extend `DropwizardAwareModule`:

```java
public class SampleModule extends DropwizardAwareModule<Configuration> {

    @Override
    protected void configure() {
        configuration()... // access configuration
        environment()... // access environment
        bootstrap()...  // access dropwizard bootstrap
    }
}
``` 

!!! note
    There are also [interfaces available](guide/module-autowiring.md), which may be implemented directly instead of extending class.
    Base class simply reduce boilerplate.

### Available bindings

* Configuration as `io.dropwizard.Configuration`, your configuration class and [any class between them](guide/bindings.md#configuration) 
(and, optionally, [interfaces implemented by your configuration class](guide/configuration.md#configuration-binding))
* `io.dropwizard.setup.Environment`

These bindings are not immediately available as HK2 context [starts after guice](guide/lifecycle.md):

* `javax.ws.rs.core.Application`
* `javax.ws.rs.ext.Providers`
* `org.glassfish.hk2.api.ServiceLocator`
* `org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider`

Request-scoped bindings:

* `javax.ws.rs.core.UriInfo`
* `javax.ws.rs.container.ResourceInfo`
* `javax.ws.rs.core.HttpHeaders`
* `javax.ws.rs.core.SecurityContext`
* `javax.ws.rs.core.Request`
* `org.glassfish.jersey.server.ContainerRequest`
* `org.glassfish.jersey.server.internal.process.AsyncContext`
* `javax.servlet.http.HttpServletRequest`
* `javax.servlet.http.HttpServletResponse`

!!! note ""
    Request scoped objects must be used through provider:
    ```java
    @Inject Provider<HttpServletRequest> requestProvider;
    ```

## Manual mode

If you don't want to use auto configuration, then you will have to manually specify all extensions.
Example above would look in manual mode like this:

```java
bootstrap.addBundle(GuiceBundle.builder()
                .useWebInstallers()
                .modules(new SampleModule())
                .extensions(
                        SampleResource.class,
                        SampleBootstrap.class,
                        CustomHeaderFilter.class
                )
                .build());
```

As you can see the actual difference is only the absence of classpath scan, so you have to manually
specify all extensions.

!!! tip
    Explicit extensions declaration could be used in auto configuration mode too: for example,
    classpath scan could not cover all packages with extensions (e.g. due to too much classes)
    and not covered extensions may be specified manually.    

!!! warning
    Duplicate extensions are filtered. If some extension is registered manually and also found with auto scan
    then only one extension instance will be registered. 
    Even if extension registered multiple times manually,
    only one extension will work. 

## Installers    

Installer is the core concept in guicey: 

* it detects extension in class (during classpath scan or manual extension specification)
* it installs extension (in most cases, request instance from guice and install in in dropwizard environment) 

Most installer implementations are very simple, so you can easily understand how it works.
 
If installer does not work as you need (not quite or has a bug), you can always replace it:

```java
bootstrap.addBundle(GuiceBundle.builder()
                ...
                .disableInstaller(ManagedInstaller.class)
                .installers(MyCustomManagedInstaller.class)       
                .build());
```

In this example, managed objects installer was disabled and custom implementation registered. 
Of course, installer could be simply disabled without replacement.

!!! warning
    Each extension could be installed only by one installer. It will be the first installer which recognize
    the extension in class (according to installers order).
    
!!! tip
    You can disable all installers enabled by default with:
    ```java
    .noDefaultInstallers()
    ```
    In this case, you will have to register some installers manually (even if it would be just a few of 
    guicey's own installers).
    
Installers are perfect extension points: consider writing custom installers for custom integrations 
(for example, to automatically register scheduler jobs using some annotation).

!!! tip ""
    Custom installers are also detected during classpath scan and could be registered automatically

## Bundles

From dropwizard you know that bundles are useful for re-using logic. 

Guicey has it's own bundles (`GuiceyBundle`) for the same reasons. Bundles allow grouping
guice modules, guicey installers and extensions (and even other bundles transitively).

!!! tip
    Guicey can check registered dropwizard bundles if they implement `GuiceyBunlde` and register them as guicey bundles too.
    It may be useful if guicey extensions are available in dropwizard bundle as additional (extra extension). 
    To enable it use:
    ```java
    .configureFromDropwizardBundles()
    ```

Guicey itself comes with multiple bundles: 

* [Core installers bundle](guide/bundles.md#core-installers-bundle) - installers, enabled by default
* [Web installers bundle](guide/bundles.md#web-installers-bundle) - web annotations installers for servlets and filters
* [HK2/guice scope diagnostic bundle](guide/bundles.md#hk2-debug-bundle) - enables instantiation tracking to catch extensions instantiation by both (or just not intended) DI
* [Diagnostics bundle](guide/bundles.md#diagnostic-bundle) - configuration diagnostic reporting to look under the hood of configuration process

[Extensions project](https://github.com/xvik/dropwizard-guicey-ext) contains even more bundles with 3rd party integrations (guava event bus, jdbi, etc.).

#### Bundles loading

Out of the box guicey provides multiple mechanisms for bundles loading.

For example, bundle could be [loaded automatically](guide/bundles.md#service-loader-lookup) if it's present in classpath (plug-n-play modules).
Or bundle could be loaded by [system property](guide/bundles.md#system-property-lookup), which is useful for testing.

Custom bundle loading mechanism [could be registered](guide/bundles.md#customizing-lookup-mechanism).

## Options

Dropwizard configuration covers most configuration cases, except development specific cases.
For example, some trigger may be useful during application testing and be useless on production (so no reason to put it in configuration).
Other example is an ability of low level tuning for 3rd party bundles.

!!! note ""
    [Options](guide/options.md) are developer configurations: either required only for development or triggers set during development 
    and not intended to be changed later.
 
Guicey itself use options for:

* Internal configurations (`GuiceOptions` enum, mostly configurable through main bundle builder methods): classpath scan packages, 
boolean flags, injector stage. Using options instead of internal bundle state allows any 3rd party
bundle/installer/extension to access these values.
* Installers options (`InstallerOptions` enum): mostly, fail or "just print error" behaviour trigger. Default values are permissive,
but developer could enforce failure.

## Configuration diagnostic

Guicey always logs installed extensions in console (as shown above), so you can be sure
if exact extension is installed or not.

Still, some configuration aspects could be not obvious (especially when project gets bigger):

* Where did it get this extension from
* How long did it take to perform classpath scan
* etc.

During startup, guicey records configuration process. After enabling `printDiagnosticInfo`:

```java
bootstrap.addBundle(GuiceBundle.builder()
                ...
                .printDiagnosticInfo()
                .build());
```

You can see additional logs in console like:

```
    GUICEY started in 453.3 ms
    │   
    ├── [0,88%] CLASSPATH scanned in 4.282 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │   
    ├── [4,2%] COMMANDS processed in 19.10 ms
    │   └── registered 2 commands
    │   
    ├── [6,4%] BUNDLES processed in 29.72 ms
    │   ├── 2 resolved in 8.149 ms
    │   └── 6 processed
    ...
    
    
    APPLICATION
    ├── extension  FooBundleResource            (r.v.d.g.d.s.bundle)       
    ├── module     FooModule                    (r.v.d.g.d.s.features)     
    ├── module     GuiceBootstrapModule           (r.v.d.guice.module)       
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)      
    │   
    ├── Foo2Bundle                   (r.v.d.g.d.s.bundle)       
    │   ├── extension  FooBundleResource            (r.v.d.g.d.s.bundle)       *IGNORED
    │   ├── module     FooBundleModule              (r.v.d.g.d.s.bundle)       
    │   ├── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)      
    ...
```

And [other logs](guide/diagnostic.md) giving you inside look on configuration.

!!! tip
    It may be even used in educational purposes to better understand how guicey work.

