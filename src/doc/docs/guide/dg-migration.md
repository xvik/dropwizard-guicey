# Migration from dropwizard-guice

!!! note "" 
    If you are not migrating from [dropwizard-guice](https://github.com/HubSpot/dropwizard-guice) then
    skip straight to [getting started](../getting-started.md)
    
## Differences

The libraries are different in core guice integration aspects.

### Dropwizard-guice

!!! warning "Note"
    I may not be exactly correct because it's been a long time since I look how all this works. 
    Please correct me if I'm wrong.

Dropwizard-guice rely on [jersey2-guice](https://github.com/Squarespace/jersey2-guice/wiki). 
Injector is created on initialization dropwizard phase.

Pros:
* Resources could be registered directly from gucie bindings
* Manual resource or jersey provider registration is the same as in core dropwizard (`env.jersey().register()`)
* Guice bundles remain the main extension point and guice bindings might be used 
inside them.

Cons:
* jersey2-guice hacks jersey locator lookup process which could potentially break on future versions
* Guice child injector created for each request 
* Not declared services are managed by HK2, but guice AOP will work only for directly bound beans (and managed by guice)
* Environment and Configuration object may be used only as Provider in eager singletons (as injector created in initialization phase)
* Admin context not covered by GuiceFilter (no request scope injections under admin context calls).

Overall, integration feels transparent and super easy to learn (great!), but with a runtime price.

### Guicey

Guicey prefer guice-first approach when everything is created by guice and registered in jersey as instance
(so HK2 does not manage services, resources etc.).

In contrast to dropwizard-guice, guicey brings a lot of new concepts to better integrate guice.
This was necessary due to moving injector creation into dropwizard run phase
(which is conceptually more correct).

Guicey abstracts user of knowing registration details and use Installer - Extension concept.
Installer must recognize extension from class and knows how to properly install it.
Also, installer hides guice usage: it implicitly obtains extension instance from guice context.     

In pure dropwizard you need to: `environment.jersey().register(MyResource.class)` whereas in guicey
you just need to declare extension class: `bundle.extensions(MyResource.class)` and everything else would be automatic.

The same for all other extensions (tasks, health checks, jersey providers etc.): only extenion class is required for 
registration.  
                                       
Pros:
* No hacks, just guice: no problems with aop
* Simple extensions installation (only extension class required) and wider range of supported extensions
* Configuration bindings (by yaml path and internal configuration object)
* Custom options mechanism
* Extensions api (custom installers support)
* GuiceFilter works on both main and admin contexts

Cons:
* Resources must use Provider for all request-scoped injections
* Special bundles (GuiceyBundle) should be used instead of dropwizard bundles (Bundle)
    in order to use guice-related features. But, as dropwizard-guice could discover bundles, 
    guicey could discover its bundles too.
* Extra diagnostic tools (embedded) usage required for debug (to see internal state). 
More a pro, but additional thing to know, which is con.        

Overall, guicey has much more features, cleaner guice integration and very developer friendly (customizable and with extra tooling), 
but with a coast of learning curve (additional concepts to know and use above dropwizard).

### Matrix

!!! note ""
    Only dropwizard-guice features present for comparison

feature    | dropwizard-guice | guicey
-----------|------------------|-------
Auto scan| +      |    +
Auto scan load class to inspect | - | +
Auto scan recognize | Bundle, ConfiguredBundle, HealthCheck, Task, @Provider, resource (@Path), ParamConverterProvider | all exept dropwizard bundles (and more) 
Resources (rest) recognition directly from guice bindings | + | -
Resources (rest) default scope | "request" | singleton 
Dropwizard bundle lookup | + | -
Injections in dropwizard Bundle | +  | - 
Injector creation customization | + | +
Injector creation phase | initialization | run 
Access Bootstrap, Environment, Configuration in guice modules | - | +
GuiceFilter contexts | main | main, admin

## Migration

### Bundle

Bundle registration is almost the same, just more methods available:

```java
bootstrap.addBundle(GuiceBundle.builder()
      .modules(new HelloWorldModule())   // instead of addModule
      .enableAutoConfig(getClass().getPackage().getName())
      // .setConfigClass(HelloWorldConfiguration.class) // not needed
      .build());
``` 

### Auto scan

!!! note
    In guicey, auto scan load all classes in providied package. The logic is - application classes would be loaded in any 
    case (in dropwizard-guice, classes are inspected by reading structure from fs).

Auto scan will find and install extensions, with few exceptions (below)

#### Health checks
 
Use `NamedHealthCheck` instead of `InjectableHealthCheck` as check class.

#### Bundles

Guicey will not find dropwizard bundles: all required bundles must be directly registered in bootstrap.

!!! important
    Guice injections will not work inside dropwizard bundles

But note, that bundles, used for features integrations may not be required as guicey perform automatic installation
(removes redundant code).
For reusable bundles, consider using GuiceyBundle instead. Also, dropwzard bundles, registered in bootsrap could be
recognized as guicey bundles (to provide extra guicey features). 

### Resources (rest)

First of all, note that resource bound only in guice module will not be discovered.
Use either classpath scan to install resources or specify them directly into bundle: `bundle.extensions(Resource1.class, Resoutrce2.class)`.

HK2 managed services (resources, providers) are also supported, but they will lack
guice aop and require extra dependency - HK2-guice-bridge (official HK guice bridge)

!!! important
    Resources are singletons by default and so they will initialize with guice context.
    You may need to wrap some injections with `Provider`. Alternatively, you may use @LazyBinding, @HK2Managed, 
    set Development stage for guice context or use prototype scope on resources (@Prototype or with global option)
    
### Diagnostic

Use `bundle.printDiagnosticInfo()` to see all extensions, installed by classpath scan.

### Injector

Remember that injector is created at runtime phase when Configuration and Environment objects are already present,
so no need to use Provider for them.  

Also, you may use direct access for configuration values. See: `bundle.printConfigurationBindings()`

### Testing

Guicey provides more lightweight alternative to DropwizardAppRule: GuiceyAppRule (starts only guice context without jetty). 
Use it for core business logic integration tests.

Also, note that you may replace any extension or bean in context before test
(GuiceyConfiguratorRule).

## Go on

Read [getting started](../getting-started.md) to get in common with guicey concepts     