# Getting started

!!! note ""
    Getting started guide briefly shows the most commonly used features.
    Advanced description of guicey concepts may be found in [the concepts section](concepts.md).    

## Installation

Available from maven central and [bintray jcenter](https://bintray.com/bintray/jcenter).

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>dropwizard-guicey</artifactId>
  <version>5.3.0</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus:dropwizard-guicey:5.3.0'
```

### BOM

Guicey pom may be also used as maven BOM.

!!! note
    If you use guicey [extensions](guide/modules.md) then use [extensions BOM](extras/bom.md) 
    instead (it already includes guicey BOM).

Gradle:

```groovy
dependencies {
    implementation platform('ru.vyarus:dropwizard-guicey:5.3.0')
    // uncomment to override dropwizard and its dependencies versions
//    implementation platform('io.dropwizard:dropwizard-dependencies:2.0.20')

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
            <version>5.3.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency> 
        <!-- uncomment to override dropwizard and its dependencies versions  
        <dependency>
            <groupId>io.dropwizard/groupId>
            <artifactId>dropwizard-dependencies</artifactId>
            <version>2.0.20</version>
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
System rules (required for StartupErrorRule) | `com.github.stefanbirkner:system-rules`
Spock | `org.spockframework:spock-core` 

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
    may be found only by looking at available methods (and reading javadoc).

[Auto configuration](guide/scan.md) (activated with `enableAutoConfig`) means that guicey will search for extensions in 
application package and subpackages. Extension classes are detected by "feature markers": for example, 
resources has `@Path` annotation, tasks extends `Task` etc.


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

Resource is a guice bean, so you can use guice injection inside it. To access request specific
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

!!! warning
    Providers must be used [instead of `@Context` field injections](installers/resource.md#context-usage) 
    But `@Context` can be used for [method parameters](installers/resource.md#context-usage)

!!! note
    By default, resources are **forced to be singletons** (when no scope annotation defined). 

### Add managed

[Dropwizard managed objects](https://www.dropwizard.io/en/release-2.0.x/manual/core.html#managed-objects) are extremely useful for managing resources.

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
    Guice [ServletModule](guide/guice/servletmodule.md) may be used for servlets and filters definitions, but most of 
    the time it's more convenient to use simple servlet annotations ([@WebFilter](installers/filter.md), 
    [@WebServlet](installers/servlet.md), [@WebListener](installers/listener.md)). 
    Moreover, guice servlet module is not able to register async [filters](installers/filter.md#async) and [servlets](installers/servlet.md#async).

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

Guice module registration:

```java
bootstrap.addBundle(GuiceBundle.builder()
                ...
                .modules(new SampleModule())
                .build());
```

Multiple modules could be registered at once:
```java
.modules(new SampleModule(), new Some3rdPatyModule())
```

!!! note
    Registration above occur in dropwizard initialization phase, when neither `Configuration`
    nor `Environment` objects are available, but if you need them in module then either
    register module in [guicey bundle's](guide/bundles.md#guicey-bundles) run method or use [marker interfaces](guide/guice/module-autowiring.md)
        
## Manual mode

If you don't want to use classpath scan, then you will have to manually specify all extensions.
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

The only difference is the absence of classpath scan (but you'll have to manually declare all extensions).

!!! tip
    Explicit extensions declaration could be used together with classpath scan: for example,
    classpath scan could not cover all packages with extensions (e.g. due to too much classes)
    and not covered extensions may be specified manually.    

!!! note
    Duplicate extensions are filtered. If some extension is registered manually and also found with auto scan
    then only one extension instance will be registered. 
    Even if extension registered multiple times manually, only one extension will work. 

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
         bind(CustomHeaderFilter.clas);                        
    }   
}
```     

Guicey will recognize all (3) bindings and register extensions. The difference with classpath scan 
or manual declaration is only that guicey will not declare default bindings for extensions 
(by default, guicey creates untargetted bindings for all extensions: `bind(Extension.class)`).

!!! tip
    One extension may be found by classpath scan, declared manually and in binding,
    but it would still be considered as single registration (with existing binding).

## Possible extensions

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

Other extension types may appear with additional modules (e.g. [jdbi](extras/jdbi3.md) adds 
support for jdbi mappers and repositories) or may be added by yourself. Any existing extension 
integration may be replaced, if it doesn't suite your needs.

!!! tip
    If you'll feel confusing to understand what guicey use for it's configuration, 
    just enable diagnostic logs:
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

## Bundles 

Guicey intended to extend dropwizard abilities (not limit). But to get access for these extended 
abilities you'll need to use [GuiceyBundle](guide/bundles.md) instead of dropwizard `ConfiguredBundle`.

Bundles **lifecycle and methods are the same**, just guicey bundle provide more abilities.


!!! attention
    This does not mean that dropwizard bundles can't be used! An opposite, guicey provides
    direct shortcuts for them in it's bundles:
    
    ```java
    public class MyBundle implements GuiceyBundle {
         default void initialize(GuiceyBootstrap bootstrap) {
             bootstrap.dropwizardBundles(new MyDropeizardBundle());
         }
    }
    ```     
    
    Additional features will be available for dropwizard bundles registered through guicey api and
    they also will appear in reports.                          


You can use dropwizard bundles as before if you don't need to register guice modules 
or use other guicey features from them. Usually dropwizard bundles used when
required integration already implemented as dropwizard bundle (3rd parties).
 