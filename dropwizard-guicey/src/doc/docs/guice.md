# Guice

!!! note
    It is important to say that guicey **did not apply any "guice magic"**. Guicey just register
    additional bindings, which you can use in your beans.  
    
Or you can simply enable full guice report ([.printAllGuiceBindings()](http://xvik.github.io/dropwizard-guicey/5.0.0/guide/diagnostic/guice-report/))
and see all added bindings under `GuiceBootsrapModule`:

```
 7 MODULES with 106 bindings
    │   
    └── GuiceBootstrapModule         (r.v.d.guice.module)       
        ├── <scope>              [@Prototype]     -                                               at ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule.configure(GuiceBootstrapModule.java:51)
        ├── instance             [@Singleton]     Options                                         at ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule.configure(GuiceBootstrapModule.java:57)
        ├── instance             [@Singleton]     ConfigurationInfo                               at ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule.configure(GuiceBootstrapModule.java:60)
        ...
```

## Added bindings

All applied bindings are described in [the user guide](guide/guice/bindings.md).

Main objects:

* `io.dropwizard.setup.Bootstrap` 
* `io.dropwizard.Configuration`
* `io.dropwizard.setup.Environment`

Bindings below are not immediately available as HK2 context [starts after guice](guide/lifecycle.md):

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

!!! important ""
    Request scoped objects must be used through provider:
    ```java
    @Inject Provider<HttpServletRequest> requestProvider;
    ```    

!!! warning
    Pay attention that inside rest resources `@Context` injection on fields [will not work on fields](http://xvik.github.io/dropwizard-guicey/5.0.0/installers/resource/#context-usage), 
    but **will** for method arguments.

### Configuration bindings

It is quite common need to access configuration value by path, instead of using
entire configuration object. Often this removes boilerplate when one option is used in multiple places, compare:

```java
@Inject MyConfiguration config
...

// in each usage
config.getSub().getFoo()
```

and 

```java
@Inject @Config("sub.foo") String foo;

// and use direct value in all places
``` 

Also, often you have some unique configuration sub object, e.g. 

```java
public class MyConfig extends Configuration {
    @JsonProperty
    AuthConfig auth;
}
```

It may be more convenient to bind it directly, instead of full configuration:

```java
@Inject @Config AuthConfig auth;
```

See complete description in [the user guide](http://xvik.github.io/dropwizard-guicey/5.0.0/guide/guice/bindings/#yaml-config-introspection)

!!! warning
    If [not disabled](guide/yaml-values.md#disable-configuration-introspection), guicey will always bind all configuration values
    (including values from base `Configuration` class). Don't be confused when use [custom config report](guide/diagnostic/yaml-values-report.md) -
    it just not shows common bindings for simplicity, but they are still applied.     

!!! note
    Use [configuration bindings report](guide/diagnostic/yaml-values-report.md) to see available configuration
    bindings. It is executed *before* injector creation and so could be used for problems diagnosis. 
    Bindings [may change](guide/guice/bindings.md#value-by-path) with configuration values changes (e.g. `server` section depends on server implementation used).    

You can also annotate any configuration property (or getter) with qualifier annotation
and property value [would be bound with this qualifier directly](guide/yaml-values.md#qualified-bindings):

```java
public class MyConfig extends Configuration {
    
    @Named("custom")
    private String prop1;
    
    @CustomQualifier
    private SubObj obj1 = new SubObj();
    
    ...

@Singleton
public class MyService { 
        
    @Inject @Named("custom") String prop;   
    @Inject @CustomQualifier SubObj obj;
}
```


## Extensions and AOP   

As it [was mentioned](concepts.md#extensions) guice knows about extensions either by 
classpath scan search, manual declaration or guice bindings.

Recognition [from guice binding](guide/guice/module-analysis.md#extensions-recognition) is not interesting
as you bind it manually.

Auto scan and manual declaration are essentially the same: guicey have extension class, which [must be bound to guice context](http://xvik.github.io/dropwizard-guicey/5.0.0/guide/guice/bindings/#extension-bindings).
In most cases it would be just `bind(Extension.class)` (but some installers can do more sophisticated bindings, 
like [plugins installer](installers/plugin.md)).

As you can see, in all cases extension is constructed by guice and so AOP features will work.

!!! note
    While HK2 is still used, instance management may be [delegated to HK2](http://xvik.github.io/dropwizard-guicey/5.0.0/guide/hk2/#hk2-delegation)
    but it is not used in core guicey (just an ability; this is almost never required) 

All extensions recognized from guice bindings are clearly visible in the [configuration report](guide/diagnostic/configuration-report.md).

## Servlets and filters

[GuiceFilter](https://github.com/google/guice/wiki/Servlets) is registered on both main and admin contexts.
Guice servlets and filters (registered through [ServletModule](http://xvik.github.io/dropwizard-guicey/5.0.0/guide/guice/servletmodule/)) may co-exist with pure servlets and filters:
as guice filter intercept both contexts, it would be able to manage request scope for all calls (even rest).

When you register [servlets](installers/servlet.md) and [filters](installers/filter.md) directly,
their instances will be managed by guice (because they are [extensions](#extensions-and-aop)), just
dispatching will work a bit differently, which is almost never important.

As you can see, in case of servlets, AOP features will also be always available. Moreover,
[scopes](http://xvik.github.io/dropwizard-guicey/5.0.0/guide/guice/scopes/) will work as expected. 

!!! note 
    [Web report](guide/diagnostic/web-report.md) could show actual mapping difference between
    pure servlets and `GuiceFilter`-managed servlets. 
 
## Startup

The only not intuitive step, performed by guicey, is [modules analysis](guide/guice/module-analysis.md):
just before injector creation guicey parse all registered modules (using [guice SPI](https://github.com/google/guice/wiki/ExtensionSPI)):

```java
List<Element> elements = Elements.getElements(modules)
```

!!! note
    Pay attention that guicey looks only actual bindings **before** injector creation. And that's why
    it would not "see" [JIT bindings](https://github.com/google/guice/wiki/JustInTimeBindings) (bindings that was not declared
    and created just because guice found an injection point). 
    This is intentional to force *declaration* of all important bindings.  

To avoid re-parsing elements during injector creation, guicey pack all parsed elements as module with:

```java
Module module = Elements.getModule(elements)
```

And so guicey [injector factory](guide/guice/injector.md#injector-factory) will receive this 
synthetic module. So if you need access to raw module, you can either do it with [event](guide/events.md)
or [disable modules analysis](guide/guice/module-analysis.md#disabling-analysis) (but in this case some features would not work)

!!! note
    Guice bindings override (`Modules.override()`), available through guicey api [modulesOverride()](guide/guice/override.md),
    will also cause synthetic module (because overrides are applied before calling injector factory).
    But this supposed to be used for tests only (just to mention).
    
## AOP

Not guicey-related, but still, as it's not always obvious how AOP is applied on beans
use [AOP report](guide/diagnostic/aop-report.md) - it shows all affected beans and
(more importantly) applied aop handlers order.
          