# Core concepts

Section briefly describes new concepts introduced by guicey and explains why it was done that way. 

## Injector creation phase

Dropwizard declares two phases: 
* initialization (App.initialize method) - when dropwizard app must be configured
* run (App.run method) - when configuration is available and extensions could be registered environment

If we create injector in initialization phase then we will not have access to Configuration (and Environment)
in guice modules. But configuration could be required in modules (especially for 3rd party modules, which
can't be created without known configuration).   

Guicey creates injector at **run phase** to allow using configuration (and environment) in guice modules.

### Guice module

But gucie modules are registered in initialization phase (modules registered in GuiceBundle), when configuration is
no available. To overcome this, guicey provides [marker interfaces](guide/module-autowiring.md) 
like ConfigurationAwareModule to set configuration object into module before injector creation.

!!! tip
    If possible, use `DropwizardAwareModule` as base module class to avoid boilerplate
    ```java
    public class SampleModule extends DropwizardAwareModule<Configuration> {
    
        @Override
        protected void configure() {
            configuration() // access configuration        
            environment() // access environment
            bootstrap()  // access dropwizard bootstrap
            configuratonTree() // configuration as tree of values
            confuguration(Class) // unique sub configuration
            configuration(String) // configuration value by yaml path
            configurations(Class) // sub configuration objects by type (including subtypes)
            options() // access guicey options
        }
    }
    ```  

!!! note
    This works only for top level modules registered in root bundle or guicey bundles. 

### Bindings

Guicey always apply it's own module (`GuiceBootstrapModule`) to injector. This module 
adds all extra bindings (for dropwizard and jersey objects).  

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

!!! note
    This is especially helpful for extensions: if extension can be sure that
    it's configuration object would be used only once in your configuration - it could
    inject it directly, without dealing with your root configuration class.     
    
## Extensions

In raw dropwizard you register extensions like: 

```java
public class App extends Application<Configuration> {
    
    ...
    
    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        // resource registration
        environment.jersey().register(MyResource.class);
        // if extension requires configuration:
        environment.lifecycle().manage(new MyManaged(configuration.getSomething()));
    }    
}
```

Very easy, just need to remember where it should be registered.         

If we want to use guice for extensions management, then all extensions must be 
obtained from guice injector, but it's a boilerplate.

Instead, guicey introduce Extension-Installer concept: you create extension (e.g. MyResource)
and Installer knows how to install it. Guicey only need to know extension class.

If you use classpath scanning, then you don't need to do anything: guicey will recognize extensions and install them.

ResourceInstaller example, for better understanding:
1. recognize MyResource class as rest resource by @Path annotation
2. Installer gets instance from injector (`injector.getInstance(MyResource.class)`) and
performs registration `environment.jersey().register(guiceManagedInstance)`

The same way, `MangedInstaller` recognize `MyManaged` as managed extension (by implemented interface) and installs guice managed instance.

!!! summary
    With classpath scan you dont need to do anything to install extension and in manual mode 
    you only need to specify extension classes. 
    
!!! tip
    Most installer implementations are very simple, so you can easily understand how it works.    

### Custom extensions

Installers are not limited to dropwizard only features: you can use custom installers to write
any 3rd party integration. The only condition is extension classes must have some unique identity (usually annotation).

For example, guice has `.asEeagerSingleton()` configuration option, which declares service as singleton
and grants it's initialization in time of injector creation (even in Development scope).
Yes, in dropwizard case it's better to use `Managed` extensions instead, but sometimes eager singletons
are convenient for quick hacking something (besides, it's just an example).
To simplify eager singleton integrations we create new annotation `@EagerSingleton` and installer
(EagerSingletonInstaller) which recognize annotation and register extensions as eager singletons. Now we need to just
annotate class and (assuming classpath scan) it will be registered automatically.

!!! tip
    Installers are also discovered and registered during classpath scan.   

Another example is `PluginInstaller` which allows you to declare plugins 
(e.g. implementing some interface) and inject all of them at once (as Set<PluginInterface\>).

Extensions project provides special installer to register events in guava eventBus:
`EventBusInstaller` check class methods and if any method is annotated with `@Subscribe` - register extension
as event bus listener.

Not existing, but possible extension for scheduled tasks: we can create `@Schedule` annotation
and write installer to automatically register such classes in scheduler framework.

!!! summary
    In essence, custom installers could help you avoid many boilerplate operations.

!!! warning
    Each extension could be installed only by one installer. It will be the first installer which recognize
    the extension in class (according to installers order).

### Core installers override

It is also possible to replace any core installer, for example, to change it's behaviour:
you just need to disable core installer and install a replacement.

```java
bootstrap.addBundle(GuiceBundle.builder()
                ...
                .disableInstaller(ManagedInstaller.class)
                .installers(MyCustomManagedInstaller.class)       
                .build());
```

!!! tip
    You can disable all installers enabled by default with:
    ```java
    .noDefaultInstallers()
    ```
    In this case, you will have to register some installers manually (even if it would be just a few of 
    guicey's own installers).
    
## Bundles

Dropwizard use bundles for re-usable logic. Bundle also support both dropwizard phases (in essence, it's the same as application)

```java
public interface ConfiguredBundle<T> {
    void initialize(Bootstrap<?> bootstrap);    
    void run(T configuration, Environment environment) throws Exception;
}
```

The concept is great, but, in context of guice, dropwizard bundle did not allow us to
register guice modules (and, of course, guicey installers and extensions).  So there is no way to
elegantly re-use dropwizard bundles mechanism.

Guicey introduce it's own bundles:

```java
public interface GuiceyBundle {
    void initialize(GuiceyBootstrap bootstrap);
}
```

`GuiceyBootstrap` provides almost all the same methods as main `GuiceBundle`, allowing you to register
installers, extensions, modules and other bundles. Also, it provides access to dropwizard objects (bootstrap, configuration, environment)

!!! warning
    Guicey bundles are called under dropwizard **run** phase (method name may be confusing, comparing to dropwizard bundles).
    That means you can't register dropwizard bundles inside guicey bundle (too late for that).
    Run phase used to provide all dropwizard objects (environment and configuration) and only 
    one method used because guicey bundles usually not mixes with normal bundles
    (of course, 3rd party dropwizard bundles are used)
    
!!! tip
    For special cases, there is a way to mix guicey bundle with dropwizard: class must just implement
    both bundle interfaces.

### Bundles usage difference

In dropwizard bundles are helpful not just for extracting re-usable extensions, but for
separation of application logic.

In guicey, you don't need write registration code and, with auto scan enabled,
don't need to configure much at all. So usually, there is no necessity to use guicey bundles
inside project at all.

This makes guicy bundles mostly usable for 3rd party integrations (or core modules extraction for large projects), 
where you can't (and should not) rely on class path scan and must declare all installers and extensions manually.

Guicey itself comes with multiple bundles: 

* [Core installers bundle](guide/bundles.md#core-installers-bundle) - installers, enabled by default
* [Web installers bundle](guide/bundles.md#web-installers-bundle) - web annotations installers for servlets and filters
* [HK2/guice scope diagnostic bundle](guide/bundles.md#hk2-debug-bundle) - enables instantiation tracking to catch extensions instantiation by both (or just not intended) DI
* [Diagnostics bundle](guide/bundles.md#diagnostic-bundle) - configuration diagnostic reporting to look under the hood of configuration process

    
### Lookup

Dropwizard force you to always register bundles manually, and this is good for clarity.
But, sometimes, it is desirable to apply bundles under some condition. For example,
you want custom installers to be registered when 3rd party integrations jar is available
or you may want to enable some bundles under integration tests only.

Guicey provides such ability: 
* add bundles appeared in classpath (with ServiceLoader, not classpath scan)
* declare extra bundles with system property (for example, could be used in tests)
* apply custom lookup implementation

ServiceLookup based lookup is ideal for 3rd party integrations. For example, suppose you have
scheduler framework integration which provides installers for custom annotation (@Job). 
All you need to do to apply extension is to put integration jar into classpath - installers will be 
implicitly registered.

!!! important
    ServiceLookup requires you to prepare extra files so you prepare your bundle for using that way.
    It *does not mean* guicey loads all bundles in classpath!     

!!! note
    ServiceLoader and property based lookups are always enabled, but you can switch them 
    off if required with `.disableBundleLookup()` bundle option.

## Disabling

As you have seen in overriding installers example, you can disable installers. But actually you can disable almost anything:
installers, extensions, guice modules and guicey bundles.

Mostly this is required for testing (to be able to exclude entire application parts and, maybe, replace with something else)

But, in some cases, you may want to change behaviour of 3rd party module: as an example above (with replaced installer),
you can replace extension, guice module (registered by some bundle), or even prevent entire bundle (with transitive bundles)
installation.

Suppose you have some 3rd party bundle:

```java
public class XBundle implements GuiceyBundle {
    public void initialize(GuiceyBootstrap bootstrap) {
        bootstrap
            .extensions(...)
            .modules(new XModule(), new XAddonModule());
    }    
}
```

And, for some reason, you don't need XAddonModule guice module, then you can simply disable it:

```java
bootstrap.addBundle(GuiceBundle.builder()
            .bundles(new XBundle())
            .disableModules(XAddonModule.class)
            .build())
```

!!! tip
    You can even perform mass overrides with some predicate. For example, disable all extensions in package:
    ```java
    .disable(Disables.inPackage("some.package.here"))
    ```

## Guice overrides

Guice allows you to [override any binding](guide/configuration.md#override-guice-bindings) with `Modules.override()`. 
With it you can override any service in context. Guicey provides direct shortcut 
for using this feature. 

Mostly, this is handful for tests, but could be used to override some service, 
registered by 3rd party module (probably registered by some bundle).

  
## Options

Dropwizard configuration covers most configuration cases, except development specific cases.
For example, some trigger may be useful during application testing and be useless on production (so no reason to put it in configuration).
Other example is an ability of low level tuning for 3rd party bundles.

!!! note ""
    [Options](guide/options.md) are developer configurations: either required only for development or triggers set during development 
    and not intended to be changed later.

* Options are declared with enum (each enum represents options group) with value type declaration to grant safety.
* Option could be set only in main bundle (in your application class)
* You can access options anywhere: guice module, guicey bundle, and in any guice service by injecting special service
* Options report is included into diagnostic report so you can see all option values.

For example, guicey use two option groups: `GuiceyOptions` and `InstallersOptions`.
GuiceyOptions used for storing main bundle configurations like packages to scan, injector stage, HK2 bridge usage etc.
That means you have access to all these application configurations from anywhere in your code
(for example, you can know if classpath scan is enabled or not in 3rd party bundle).

Another good example is InstallersOptions.JerseyExtensionsManagedByGuice which changes the way 
jersey extensions are handled: with guice or with HK2. This is developer time solution and must be
selected by developer (because it affects behaviour a lot). Thanks to generic mechanism other bundles could
know what was chosen.

HK2 usage is highly dependent on HK2-guice-bridge presence and with option we can verify it:

```java
Preconditions.checkState(options.get(GuiceyOptions.UseHkBridge), 
                            "HK2 guice bridge is required!")
``` 

### Dynamic options

You can bind option to system property in order to introduce special "hidden" application flag:

```java
GuiceBuilder.builder()
    ...
    .options(new OptionsMapper()
                    .prop("myprop", Myoptions.SomeOption)
                    .map())
    .build()                
```

Now you can run application with `-Dmyprop=value` and this value will be mapped to option (accessible everywhere in the application).

!!! note
    There are built-in basic value conversions from string, but it complex cases you can do manual conversion
    ```java
     .prop("myprop", Myoptions.SomeOption, val -> convertVal(val)) 
    ```   
    
!!! note
    You can map options from sys properties, environment variables or strings (obtained somewhere else).
    You can even allow mass binding to allow external definition of any option `.props("prefix")`.
    See [options lookup](guide/options.md#options-lookup) doc.
    
## You don't need to remember all this

All guicey features could be revealed from main bundle methods. So you don't 
need to remember everything - just look methods.

There is a special group of `print[Something]` methods, which are intended to help
you understand internal state (and help with debugging).
 
As you have seen, real life configuration could be quite complex because you may have many extensions, observed with classpath scan,
bundles, bundles installing other bundles, many gucie modules. Also, some bundles
may disable extensions, installers, guice modules (and come modules could override services).

During startup guicey tracks all performed configurations and you can even access this 
information at runtime using `@Inject GuiceyConfigurationInfo info`.

Out of the box, guicey could print all this into console, you just need to add `.printDiagnosticInfo()`:

```java
bootstrap.addBundle(GuiceBundle.builder()
            .printDiagnosticInfo()
            .build())
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

Other helpful reports:
* `printAvailableInstallers()` - see all registered installers to know what features you can use
* `printConfigurationBindings()` - show available configuration bindings (by path and unique objects)
* `printCustomConfigurationBindings()` - the same as above, but without dropwizard configuration (shorter report)
* `printLifecyclePhases()` - indicate running steps in logs
* `printLifecyclePhasesDetailed()` - very detailed startup reports 

## Not mentioned

* Dropwizard commands support (automatic commands installation with classpath scan)
* Hiding classes from classpath scan
* Integration tests support
* Lifecycle events 
* Special web installers (to use instead of guice ServletModule)
* Admin rest support
* Extensions project with 3rd party integrations
* Examples