# Concepts overview

!!! abstract ""
    Section briefly describes new concepts introduced by guicey and explains why it was done that way.
    For usage instruction and basic examples see [getting-started section](getting-started.md). 

### HK2

Many people ask why not just use HK2 instead of guice as it's already provided. 
Unfortunately, it's hard to use it in the same elegant way as we can use guice. 
HK2 context is launched too late (after dropwizard run phase) and, for example, it is 
impossible to use HK2 to instantiate dropwizard managed objects because managed 
must be registered before HK2 context starts.

Guicey use lazy factories for integration: it register providers for HK2 objects in 
guice context. Guice-managed objects (extensions) are simply registered as instances.
So most of the time you don't have to know about HK2 at all.

There are additional features allowing you to delegate some extensions management
[completely to HK2](guide/hk2.md#use-hk2-for-jersey-extensions), but it's intended to be used in very rare cases (edge cases!). 
In this case you may require to explicitly register hk2-guice bride so hk2 could 
see guice beans directly. 

!!! danger
    Since jersey 2.26 it is possible to get rid of HK2 completely. Next guicey version
    will ONLY use guice and all current HK2-related features will be removed.   

## Lifecycle

Dropwizard declares two phases: 

* initialization (`App.initialize` method) - when dropwizard app must be configured
* run (`App.run` method) - when configuration is available and extensions could be registered in environment

Guicey follow dropwizard convention: it will configure everything (almost) on initialization phase
and **start injector on run phase**. 

!!! note 
    If we create injector in initialization phase then we will not have access to `Configuration` and `Environment`
    in guice modules, but configuration could be required, especially for 3rd party modules, which
    does not support lazy configuration.   
    
The only exception for configuration under initialization phase is 
guice modules, which can be registered in run phase (simply because modules too often
require configuration values for construction). As a consequence, extensions recognized 
from guice bindings are registered in run phase too.

This separation of initialization and run phases makes configuration more predictable
(especially important when bundles depend on initialization order).      

### Guice module

In the main `GuiceBundle` guice modules registration appears under initialization phase (when
neither `Configuration` nor `Environment` objects are available). If module require these objects 
and it's registration can't be moved to guicey bundle's run method, then use 
[marker interfaces](guide/guice/module-autowiring.md). For example, `ConfigurationAwareModule` will lead 
to configuration object set into module before injector creation.

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

!!! warning
    Marker interfaces work only on modules, directly registered through guicey api.


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

Instead, guicey introduce `Extension-Installer` concept: you create extension (e.g. `MyResource`)
and Installer knows how to install it. Guicey only need to know extension class.

If you use [classpath scanning](guide/scan.md), then you don't need to do anything: guicey will recognize extensions and install them.

For example, [`ResourceInstaller`](installers/resource.md) will:

1. recognize `MyResource` class as rest resource by `@Path` annotation
2. gets instance from injector (`injector.getInstance(MyResource.class)`) and
performs registration `environment.jersey().register(guiceManagedInstance)`

The same way, `MangedInstaller` recognize `MyManaged` as managed extension (by implemented interface) and 
installs guice managed instance (and so all other extensions).

!!! summary
    With classpath scan you don't need to do anything to install extension and in manual mode 
    you only need to specify extension classes. 
    
!!! tip
    Most installer implementations are very simple, so you can easily understand how it works 
    (all core installers are declared in [core installers bundle](guide/bundles.md#core-installers-bundle)).    

### Guice bindings 

Guicey also search extensions in registered guice modules. For example:

```java
public class MyModule extends AbstarctModule {
    @Override
    protected void configure() {
        bind(MyResource.class);    
    }   
}       

GuiceBundle.builder()
    .modules(new MyModule())
    .build()
```

`MyResource` will be recognized as extension and installed.

!!! summary
    So overall there are 3 possible sources for extensions:
    
    * [Classpath scan](guide/scan.md) (mainly used for application extensions)
    * [Manual declaration](guide/configuration.md#configuration-items) (used in bundles to explicitly declare extensions)
    * [Guice bindings](guide/guice/module-analysis.md#extensions-recognition)
    
    In all cases extension is identifyed by it's class, but for extensions
    detected from guice bindings [automatic untargetted binding](guide/guice/bindings.md#extension-bindings) is not performed.  

### Jersey extensions

It is important to note that jersey extensions ([resources](installers/resource.md) and [other](installers/jersey-ext.md))
are **forced to be singletons** (if explicit [scope annotation](guide/guice/scopes.md) is not set).

This force you to always use all request scoped objects through `Provider`. But, from the other side,
this avoids a jvm garbage from creating them for each request and makes everything a bit 
faster (no extra DI work required for each request).

If you think that developer comfort worth more then small performance gain, then:

* You can use explicit scope annotations to change singleton scope (`@RequestScoped`, `@Prototype`)
* Switch off forced singletons (`.option(InstallerOptions.ForceSingletonForJerseyExtensions, false)`)
* Delegate some extensions or resources management to HK2 using `@JerseyManaged`
* Use [HK2 by default](guide/hk2.md#use-hk2-for-jersey-extensions) for jersey extensions

!!! warning 
    Guice AOP will not work on extensions managed by HK2        

### Custom extensions

Installers are not limited to dropwizard only features: you can use custom installers to write
any 3rd party integration. The only condition is extension classes must have some unique identity (usually annotation).

For example,  [`EagerSingletonInstaller`](installers/eager.md) simply binds extensions 
annotated with `@EagerSingleton` with `bind(Ext.class).asEeagerSingleton()`, so we
can simply annotate class and make sure it would be registered in guice context without
additional configurations (thanks to classpath scan). 

!!! tip
    Custom installers are also discovered and registered during [classpath scan](guide/scan.md).   

Another example is [`PluginInstaller`](installers/plugin.md) which allows you to declare plugins 
(e.g. implementing some interface) and inject all of them at once (as `Set<PluginInterface>`).

[Extensions project](https://github.com/xvik/dropwizard-guicey-ext) provides special installer to 
[register events in guava eventBus](extras/eventbus.md):
`EventBusInstaller` check class methods and if any method is annotated with `@Subscribe` - register extension
as event bus listener.

Not existing, but possible extension for scheduled tasks: we can create `@Schedule` annotation
and write installer to automatically register such classes in scheduler framework.

!!! summary
    In essence, custom installers could help you avoid many boilerplate operations.

!!! warning
    Each extension could be installed only by one installer. It will be the first installer which recognize
    the extension in class (according to [installers order](guide/installers.md#ordering)).

### Core installers override

It is also possible to replace any core installer (e.g. to change it's behaviour) -
you just need to disable core installer and install a replacement:

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

Dropwizard use bundles for re-usable logic. Bundle support both dropwizard phases (initialization and run).

```java
public interface ConfiguredBundle<T> {
    default void initialize(Bootstrap<?> bootstrap) {};    
    default void run(T configuration, Environment environment) throws Exception {};
}
```

The concept is great, but, in context of guice, dropwizard bundle did not allow us to
register guice modules (and, of course, guicey installers and extensions).  So there is no way to
elegantly re-use dropwizard bundles mechanism.

Guicey introduce it's own bundles:

```java
public interface GuiceyBundle {
    default void initialize(GuiceyBootstrap bootstrap) {}; 
    default void run(GuiceyEnvironment environment) throws Exception {};
}
```         

As you can see [guicey bundles](guide/bundles.md) are completely equivalent to dropwizard bundles and so
it is very easy to switch from dropwizard bundles into guicey bundles.

`GuiceyBootstrap` provides almost all the same methods as main `GuiceBundle`, allowing you to register
installers, extensions, modules and other bundles. Also, it provides access to dropwizard Bootstrap object

`GuiceyEnvironment` allows to register only guice modules (as all configuration should appear under initialization),
but provide many shortcut methods for simplify manual registrations (or delayed manual logic).
Provides access to dropwizard configuration, environment and introspected configuration tree.
    
!!! tip
    Guicey bundles assume to be used together with dropwizard bundles (because there are already 
    [many](https://modules.dropwizard.io/thirdparty/) ready-to use dropwizard bundles): 
    ```java
    GuiceyBootstrap.builder()
        .dropwizardBundles(..)
    ```

### Bundles usage difference

In dropwizard bundles are helpful not just for extracting re-usable extensions, but for
separation of application logic.

In guicey, you don't need to write registration code and with enabled [classpath scan](guide/scan.md),
don't need to configure much at all. This makes guicy bundles mostly usable for 3rd party integrations (or core modules extraction for large projects), 
where you can't (and should not) rely on class path scan and must declare all installers and extensions manually.

Many bundle examples could be found in [extension modules](guide/modules.md).
    
### Bundles lookup

Dropwizard force you to always register bundles manually, and this is good for clarity.
But, sometimes, it is desirable to apply bundles under some condition. For example,
you want custom installers to be registered when 3rd party integrations jar is available
or you may want to enable some bundles under integration tests only.

Guicey provides such ability: 

* add bundles [appeared in classpath](guide/bundles.md#service-loader-lookup) (with ServiceLoader, not classpath scan)
* declare extra bundles [with system property](guide/bundles.md#system-property-lookup) (for example, could be used in tests)
* apply [custom lookup implementation](guide/bundles.md#customizing-lookup-mechanism)

ServiceLookup based lookup is ideal for 3rd party integrations. For example, suppose you have
scheduler framework integration which provides installers for custom annotation (`@Job`). 
All you need to do to apply extension is to put integration jar into classpath - installers will be 
implicitly registered.

!!! important
    ServiceLookup requires you to [prepare extra files](guide/bundles.md#service-loader-lookup) so you prepare your bundle for using that way.
    It *does not mean* guicey loads all bundles in classpath!     

!!! tip
    ServiceLoader and property based lookups are always enabled, but you can switch them 
    off if required with `.disableBundleLookup()` bundle option.
    

## Disabling items

As you have seen in [overriding installers example](#core-installers-override), you can disable installers. 
But actually you can disable almost anything:
installers, extensions, guice modules, guicey bundles and even dropwizard bundles.

Mostly this is required for testing (to be able to exclude entire application parts and, maybe, replace with something else).

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

And, for some reason, you don't need `XAddonModule` guice module, then you can simply disable it:

```java
bootstrap.addBundle(GuiceBundle.builder()
            .bundles(new XBundle())
            .disableModules(XAddonModule.class)
            .build())
```

!!! tip
    You can even perform [mass disables by predicate](guide/disables.md#disable-by-predicate). 
    For example, disable all installations (extensions, bundles etc) from package:
    ```java
    .disable(Disables.inPackage("some.package.here"))
    ```

!!! warning
    Disabling of guice modules also affect transitive modules! For example,
    
    ```java
    public class MyModule extends AbstractModule {
        @Override
        protected void configure() {
            // transitive
            install(new OtherMyModule());
        }
    }
    
    GuiceBundle.builder()
        .modules(new MyModule())
        .disableModules(OtherMyModule.class)
    ```
    Will disable transitive module!

!!! warning
    Only dropwizard bundles, registered through guicey api are visible!
    For such bundles, guicey will see all transitive bundles too and will be 
    able to disable them:
    
    ```java
    public class MyDwBundle implements ConfiguredBundle {
        public void initialize(Bootstrap bootstrap) {
            // transitive bundle
            bootstrap.addBundle(new OtherMyDwBundle());
        }
    }
    
    GuiceBundle.builder()
            .dropwizardBundles(new MyDwBundle())
            .disableDropwziardBundle(OtherMyDwBundle.class)
    ```
    Will prevent `OtherMyDwBundle` bundle installation.
    
## De-duplication items

Guice modules, guicey bundles and dropwizard bundles are registered by instance.
That means that multiple instances of the same type could be registered, for example:

```java
.bundles(new MyBundle(), new MyBndle())
```

Sometimes it may be desirable, but sometimes not. For example, there may be some common
bundle:

```java
public class Feature1Bundle implements GuiceyBundle {
    public void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.bundles(new CommonBundle()); 
        ...
    }    
}

public class Feature2Bundle implements GuiceyBundle {
    public void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.bundles(new CommonBundle());  
        ...
    }    
}
```

And duplicate initialization of common bundle may lead to error.

In order to detect duplicate instances guicey rely on `equals()` method. So
if two instances of the same type are equal then only one of them will be used
and other considered duplicate.

So if `ComonBundle` implement `equals` everything will work as planned.

For completely unique bundles and modules there are pre-defined base classes
`UniqueGuiceyBundle` and `UniqueModule` (`UniqueDropwizardAwareModule`) accordingly.
(e.g. `CommonBundle extends UniqueGuiceyBundle`)

For cases when it is not possible to change bundle or module class, it could be 
declared as unique:

```java
GuiceBundle.builder()
    .bundles(new Feature1Bundle(), new Feature2Bundle())
    .uniqueItems(ComonBundle.class)
    .build()
```   

!!! warning "Guice modules limitation"
    Transitive guice modules are not counted! That means in case of de-duplication
    transitive modules are not visible.              

Note that "common bundle" problem for dropwizard bundles may be solved by simply 
registering dropwizard bundles through guicey api.

!!! warning "Dropwizrd bundles"
    Only dropwizard bundles, registered through guciey api are visible.
    So if there would be one bundle registered directly in dropwizard and 
    another with guicey api - guicey will not detect duplicate.


## Guice bindings overrides

Guice allows you to override any binding using `Modules.override()`. 
With it you can override any service in context. Guicey provides [direct shortcut for using this feature](guide/guice/override.md). 

Mostly, this is handful for tests, but could be used to override some service, 
registered by 3rd party module (probably registered by some bundle).

  
## Options

Dropwizard configuration covers most configuration cases, except development specific cases.
For example, some trigger may be useful during application testing and be useless on production (so no reason to put it in configuration).
Other example is an ability of low level tuning for 3rd party bundles.

!!! note ""
    [Options](guide/options.md) are developer configurations: either required only for development or triggers set during development 
    and not intended to be changed later.

* Options are [declared with enum](guide/options.md#custom-options) (each enum represents options group) with value type declaration to grant safety.
* Option could be set only [in main bundle](guide/options.md) (in your application class)
* You can access options [anywhere](guide/options.md): guice module, guicey bundle, and in any guice service by injecting special service
* Options report is included into [diagnostic report](guide/diagnostic/configuration-report.md) so you can see all option values.

For example, guicey use two option groups: `GuiceyOptions` and `InstallersOptions`.
`GuiceyOptions` used for storing main bundle configurations like packages to scan, injector stage, HK2 bridge usage etc.
That means you have access to all these application configurations from anywhere in your code
(for example, you can know if classpath scan is enabled or not in 3rd party bundle).

Another good example is `InstallersOptions.JerseyExtensionsManagedByGuice` which changes the way 
jersey extensions are handled: with guice or with HK2. This is developer time decision and must be
selected by developer (because it affects behaviour a lot). Thanks to generic mechanism other bundles could
know what was chosen.

HK2 usage is highly dependent on [HK2-guice-bridge](guide/hk2.md#hk2-guice-bridge) presence and with option we can verify it:

```java
Preconditions.checkState(options.get(GuiceyOptions.UseHkBridge), 
                            "HK2 guice bridge is required!")
``` 

### Options lookup

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
    There are built-in basic value conversions from string, but in complex cases you can do manual conversion
    ```java
     .prop("myprop", Myoptions.SomeOption, val -> convertVal(val)) 
    ```   
    
!!! note
    You can map options from sys properties, environment variables or strings (obtained somewhere else).
    You can even allow mass binding to allow external definition of any option `.props("prefix")`.
    See [options lookup](guide/options.md#options-lookup) doc.

## Configuration hooks

Guicey provides special configuration hooks mechanism which may be used to change application 
configuration. It is useful for tests or to attach various diagnostic tools
for compiled application.

For example, out of the box guicey provides `DiagnosticHook`, which activates
diagnostic reporting (print* methods). It may be enabled even on compiled application
with a system property: 

```
-Dguicey.hooks=diagnostic
```

The same way you can write your hooks and register them with shortcuts 
(`GuiceBundle.builder().aliasHook("tool", ToolHook.class)`).  

    
## You don't need to remember all this

All guicey features could be revealed from main bundle methods. So you don't 
need to remember everything - just look methods.

There is a special group of `print[Something]` methods, which are intended to help
you understand internal state (and help with debugging).
 
As you have seen, real life configuration could be quite complex because you may have many extensions, observed with classpath scan,
bundles, bundles installing other bundles, many gucie modules. Also, some bundles
may disable extensions, installers, guice modules (and some modules could even override bindings).

During startup guicey tracks all performed configurations and you can even access [this 
information](guide/diagnostic/configuration-model.md) at runtime using `@Inject GuiceyConfigurationInfo info`.

Out of the box, guicey could print all this into console, you just need to add:

```java
bootstrap.addBundle(GuiceBundle.builder()
            .printDiagnosticInfo()
            .build())
```

And [other logs](guide/diagnostic/diagnostic-tools.md) giving you inside look on configuration.

### Not mentioned

* [Dropwizard commands support](guide/commands.md) 
* [Integration tests support](guide/test.md)
* [Lifecycle events](guide/events.md)
* [Shared state](guide/shared.md) 
