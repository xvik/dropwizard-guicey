# Setup

!!! note ""
    Getting started guide briefly shows the most commonly used features.
    Advanced descriptions of guicey concepts may be found in [the concepts section](concepts.md).    

## Installation

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>dropwizard-guicey</artifactId>
  <version>{{ gradle.version }}</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus:dropwizard-guicey:{{ gradle.version }}'
```

### BOM

Guicey pom may be also used as maven BOM.

!!! note
    If you use guicey [extensions](guide/modules.md) then use [extensions BOM](extras/bom.md) 
    instead (it already includes guicey BOM).

Gradle:

```groovy
dependencies {
    implementation platform('ru.vyarus:dropwizard-guicey:{{ gradle.version }}')
    // uncomment to override dropwizard and its dependencies versions
//    implementation platform('io.dropwizard:dropwizard-dependencies:{{ gradle.dropwizard }}')

    // no need to specify versions
    implementation 'ru.vyarus:dropwizard-guicey'
       
    implementation 'io.dropwizard:dropwizard-auth'
    implementation 'com.google.inject:guice-assistedinject'   
     
    testImplementation 'io.dropwizard:dropwizard-test'
    testImplementation 'org.spockframework:spock-core'
}
```    

Maven:

```xml      
<dependencyManagement>  
    <dependencies>
        <dependency>
            <groupId>ru.vyarus</groupId>
            <artifactId>dropwizard-guicey</artifactId>
            <version>{{ gradle.version }}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency> 
        <!-- uncomment to override dropwizard and its dependencies versions  
        <dependency>
            <groupId>io.dropwizard/groupId>
            <artifactId>dropwizard-dependencies</artifactId>
            <version>{{ gradle.dropwizard }}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency> -->                 
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>ru.vyarus</groupId>
        <artifactId>dropwizard-guicey</artifactId>
    </dependency>
</dependencies>
```

BOM includes:

BOM           | Artifact
--------------|-------------------------
Guicey itself | `ru.vyarus:dropwizard-guicey`
Dropwizard BOM | `io.dropwizard:dropwizard-bom`
Guice BOM | `com.google.inject:guice-bom`
HK2 bridge | `org.glassfish.hk2:guice-bridge`
Spock-junit5 | `ru.vyarus:spock-junit5` 

## Usage

!!! note ""
    Full source of example application is [published here](https://github.com/xvik/dropwizard-guicey-examples/tree/master/core-getting-started)

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
    may be found only by looking at available methods (and reading the javadoc).

[Auto configuration](guide/scan.md) (activated with `enableAutoConfig`) means that guicey will search for extensions in 
the application package and subpackages. Extension classes are detected by "feature markers": for example, 
resources has `@Path` annotation, tasks extends `Task` etc.


!!! tip
    You can declare multiple packages for classpath scan: 
    ```java
     .enableAutoConfig("com.mycompany.foo", "com.mycompany.bar")
    ```

The application could be launched by running main class (assumes you will use an IDE run command):

```bash
SampleApplication server
```

!!! note
    a config.yml is not passed as a parameter because we don't need additional configuration yet

### Adding a Resource

Create a custom rest resource class:

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

After creating your resource, when you run the application the resource was installed automatically:

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

Resource is a guice bean, so you can use guice injection inside it. To access request scoped objects like `javax.servlet.http.
HttpServletRequest`, `javax.servlet.http.HttpServletResponse`, `javax.ws.rs.core.UriInfo`, `org.glassfish.jersey.server.
ContainerRequest`, etc, you must wrap the desired objects in a `Provider`:

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

The example resource now obtains the caller's remote ip address and returns it in the response body.

!!! warning
    Providers must be used [instead of `@Context` field injections](installers/resource.md#context-usage) 
    But `@Context` can be used for [method parameters](installers/resource.md#context-usage)

!!! note
    By default, resources are **forced to be singletons** (when no scope annotation defined). 

### Adding a Managed Object

[Dropwizard managed objects](https://www.dropwizard.io/en/release-2.0.x/manual/core.html#managed-objects) are extremely useful for managing resources.

Create a simple managed implementation:

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

The managed class will be automatically discovered and installed by Guicey. Guicey always reports installed extensions
when they are not reported by dropwizard itself. In the start-up logs of the application, you can see:

```
INFO  [2017-02-05 11:59:30,750] ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller: managed =

    (ru.vyarus.dropwizard.guice.examples.service.SampleBootstrap)
```

### Adding A Filter

!!! note
    Guice [ServletModule](guide/guice/servletmodule.md) may be used for servlets and filters definitions, but most of 
    the time it's more convenient to use simple servlet annotations ([@WebFilter](installers/filter.md), 
    [@WebServlet](installers/servlet.md), [@WebListener](installers/listener.md)). 
    Moreover, guice servlet module is not able to register async [filters](installers/filter.md#async) and [servlets](installers/servlet.md#async).

Add a sample filter around rest methods:

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

The filter will only pass through requests with the `user=me` request parameter. It is used just to show
how to register custom filters with annotations. The implementation itself is not useful.

Upon start-up, new logs will confirm successful filter installation:

```
INFO  [2017-02-11 17:18:16,943] ru.vyarus.dropwizard.guice.module.installer.feature.web.WebFilterInstaller: filters =

    /*                       (ru.vyarus.dropwizard.guice.examples.web.AuthFilter)   .auth
```

Call `http://localhost:8080/sample/` and `http://localhost:8080/sample/?user=me` to make sure filter works.

### Adding a Guice Module

Guice module registration:

```java
bootstrap.addBundle(GuiceBundle.builder()
                ...
                .modules(new SampleModule())
                .build());
```

Multiple modules could be registered at once:
```java
.modules(new SampleModule(), new Some3rdPartyModule())
```

!!! note
    The above registration occurs in dropwizard initialization phase, when neither `Configuration`
    nor `Environment` objects are available. If you need either of them in a module, you may register a module in 
    [guicey bundle's](guide/bundles.md#guicey-bundles) `run` method or use [marker interfaces](guide/guice/module-autowiring.md).
        
## Manual mode

If you don't want to use classpath scanning for extension discovery, then you will have to manually specify all extensions.
Example above would look in manual mode like this:

```java
bootstrap.addBundle(GuiceBundle.builder()
                .modules(new SampleModule())
                .extensions(
                        SampleResource.class,
                        SampleBootstrap.class,
                        CustomHeaderFilter.class
                )
                .build());
```

The only difference is the absence of `.enableAutoConfig(...)` and the explicit declaration of desired extensions.

!!! tip
    Explicit extension declaration could be used together with `enableAutoConfig` (classpath scan). For example,
    a classpath scan may only scan for extensions in your application's package and subpackages, while extensions outside of
    those packages may be specified separately. This avoids large class path scans and improves the startup time of your 
    application.

!!! note
    Only distinct extensions are registered. Duplicates are not registered. If some extension is registered manually and also found 
    with auto config, then only one instance of that extension will be registered. If an extension is registered multiple times 
    manually, the same rules apply and only one extension instance will be registered. 

## Configuration from bindings

Guicey is also able to recognize extensions from declared guice bindings, so manual example above is equal to:

```java
bootstrap.addBundle(GuiceBundle.builder()
                .modules(new SampleModule())
                .build());                 


public class SampleModule extends AbstractModule {
    @Override
    protected void configure() {
         bind(SampleResource.class).in(Singleton.class);
         bind(SampleBootstrap.class);
         bind(CustomHeaderFilter.class);                        
    }   
}
```     

Guicey will recognize all three bindings and register extensions. The difference with classpath scanning
or manual declaration is only that guicey will not declare default bindings for extensions 
(by default, guicey creates untargetted bindings for all extensions: `bind(Extension.class)`).

!!! tip
    An extension may be found three ways: by classpath scan, explicit extension declaration on the GuiceBundle, and by 
    declaring a binding in a guice module. Even if all three were used, the extension would only be registered once.

## Recognized Extensions

Guicey can recognize and install:

* Dropwizard [tasks](installers/task.md)
* Dropwizard [managed objects](installers/managed.md)
* Dropwizard [health checks](installers/healthcheck.md)
* REST [resources](installers/resource.md)
* REST [extensions (exception mappers, message body readers etc.)](installers/jersey-ext.md) 
* Jersey [features](installers/jersey-feature.md)
* [Filters](installers/filter.md), [servlets](installers/servlet.md), [listeners](installers/listener.md)
* [Eager singletons](installers/eager.md), without direct guice registration

It can even simulate simple [plugins](installers/plugin.md).

Other extension types may be recognized with additional installed modules. For example, [jdbi](extras/jdbi3.md) adds 
support for jdbi mappers and repositories. You may add others yourself. Any existing extension integration may be 
replaced, if it doesn't suit your needs.

!!! tip
    If you are unsure or don't understand what guicey is using for its configuration, enable diagnostic logs:
    ```java
    GuiceBundle.builder()        
        .printDiagnosticInfo()
        ...
    ```
    
    To see what extensions are supported you can always use:
    ```java
    GuiceBundle.builder()        
        .printAvailableInstallers()    
    ```
    
    And to see available guice bindings:
    ```java
    GuiceBundle.builder()        
        .printGuiceBindings()    
    ```

## Guicey Bundles 

Guicey Bundles are intended to extend the functionality of Dropwizard Bundles, not limit them. To get access for these extended 
abilities you'll need to use [GuiceyBundle](guide/bundles.md) instead of a dropwizard `ConfiguredBundle`.

The Guicey Bundle **lifecycle and methods are the same** as Dropwizard Bundles. Guicey Bundles simply provide more functionality.


!!! attention
    This does not mean that dropwizard bundles can't be used! An opposite, Guicey provides
    direct shortcuts for them in its bundles:
    
    ```java
    public class MyBundle implements GuiceyBundle {
         default void initialize(GuiceyBootstrap bootstrap) {
             bootstrap.dropwizardBundles(new MyDropeizardBundle());
         }
    }
    ```     
    
    Additional features will be available for Dropwizard Bundles registered through guicey api and
    they also will appear in reports.                          


You can always use vanilla Dropwizard Bundles if you don't need to register guice modules 
or use other guicey features. Usually Dropwizard Bundles used when the required integration has 
already implemented as a 3rd party Dropwizard Bundle.
