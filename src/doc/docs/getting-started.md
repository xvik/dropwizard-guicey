# Getting started

!!! note ""
    Getting started guide briefly shows the most commonly used features.
    Advanced description of guicey concepts may be found in [the concepts section](concepts.md).
    If you are migrating from dropwizard-guice, read [migration guide](guide/dg-migration.md).    

## Installation

Available from maven central and [bintray jcenter](https://bintray.com/bintray/jcenter).

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>dropwizard-guicey</artifactId>
  <version>5.0.0.RC1</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus:dropwizard-guicey:5.0.0.RC1'
```

### BOM

Guicey pom may be also used as maven BOM:

```groovy
plugins {
    id "io.spring.dependency-management" version "1.0.8.RELEASE"
}
dependencyManagement {
    imports {
        mavenBom 'ru.vyarus:dropwizard-guicey:5.0.0.RC1'  
        // uncomment to override dropwizard version    
        // mavenBom 'io.dropwizard:dropwizard-bom:1.3.7' 
    }
}

dependencies {
    compile 'ru.vyarus:dropwizard-guicey:4.2.2'
   
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

!!! important
    Providers must be used [instead of `@Context` field injections](installers/resource.md#@context-usage) 

Also, you can inject request specific objects [as method parameter](installers/resource.md#context-usage)

!!! note ""
    Field injection used in examples for simplicity. In real life projects [prefer constructor injection](https://github.com/google/guice/wiki/Injections).    

!!! warning ""
    By default, resources are **forced to be singletons** (when no scope annotation defined). 

### Add managed

[Dropwizard managed objects](http://www.dropwizard.io/1.3.5/docs/manual/core.html#managed-objects) are extremely useful for managing resources.

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
    Except when you need to [access dropwizard objects](guide/module-autowiring.md) in module
    
    
!!! warning
    Guicey removes duplicate registrations by type. For example, in case:
    ```java
    .modules(new SampleModule(), new SampleModule())
    ```
    Only one module will be registered. This is intentional restriction to simplify bundles usage
    (to let you register common modules in different bundles and be sure that only one instance will be used).
    
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

!!! important
    Duplicate extensions are filtered. If some extension is registered manually and also found with auto scan
    then only one extension instance will be registered. 
    Even if extension registered multiple times manually,
    only one extension will work. 
