# Decomposition

!!! summary ""
    Guide for writing re-usable bundles

In dropwizard there is only one decomposition element: [ConfiguredBundle](https://www.dropwizard.io/en/stable/manual/core.html#bundles).

!!! note
    In pure dropwizard, bundles may also  be used within a single application to separate configuration blocks
    (simplify logic). In guicey, this is not required as [classpath scan](guide/scan.md) may be
    used for extension registration and reduce the amount of required configuration.
    
    This chapter describes only re-usable logic decomposition.
    
In guicey there are three decomposition elements - guicey bundles (`GuiceyBundle`), guice modules (`Module`) and dropwizard bundle (`ConfiguredBundle`).
Having three options could be confusing.

- There are existing [dropwizard modules](https://modules.dropwizard.io/thirdparty/) - `ConfiguredBundle`
- Existing [guice modules](https://github.com/google/guice/wiki/3rdPartyModules) (outdated list, just as an example) - `Module`
- And [guicey extensions](guide/modules.md) - `GuiceyBundle`

All of these modules are supposed to be used together. In some cases, guicey explicitly provides wrapping 
modules (e.g. the [jdbi](extras/jdbi3.md) wrapper around the [dropwizard module](https://www.dropwizard.io/en/latest/manual/jdbi3.html)).
Such wrappers provide guice related features and enhancements, impossible in vanilla dropwizard modules.

## Guicey bundle

As described [here](concepts.md#bundles), guicey introduces its own [bundle](guide/bundles.md) because guicey provides
additional configuration features. Even a simple use case may have a need to configure guice modules from a bundle.

*Prefer `GuiceyBundle` over dropwizard `Bundle`* for developing re-usable modules.
Of course, if module is very generic (does not depend on guice) you can use a pure dropwizard module
(to publish it for wider audience), but almost all bundles rely on guicey features.

Benefits:

- guice support (ability to register guice modules)
- [options](guide/options.md) support
- use [sub-configuration objects](guide/yaml-values.md#unique-sub-configuration) directly (important for writing generic modules)
- define custom [extension types](guide/extensions.md) to simplify usage (e.g. like [jdbi](extras/jdbi3.md))
- [automatic module loading](concepts.md#bundles-lookup) when jar appear in classpath (e.g. like [lifecycle annotations](extras/lifecycle-annotations.md))
- [shared state](guide/shared.md) - advanced techniques for bundle communication (e.g. used by [GSP](extras/gsp.md) and [SPA](extras/spa.md))
- [events](guide/events.md) - internal lifecycle events for fine-tuning lifecycle (again, complex cases only, for example, [GSP](extras/gsp.md) use it to order bundles logic)
- ability to replace functionality (prevent feature x registration by [disabling it](guide/disables.md) and register feature y instead)

## Dropwizard bundle

It is important to note that there is an important difference between registering a dropwizard bundle directly via the 
dropwizard `Bootstrap` and registering a bundle through the guicey api - any bundle registered through the guicey api
[may be disabled](guide/disables.md#disable-dropwizard-bundles), [de-duplicated](guide/deduplication.md#dropwizard-bundles), and/or
tracked for [transitive bundles](guide/bundles.md#transitive-bundles-tracking).

All bundles registered through guicey api will also appear in [configuration report](guide/diagnostic/configuration-report.md).

There is a difference between the order that dropwizard and guicey bundles are initialized:

*Dropwizard bundles* **immediately** initialize a transitive bundle.

```java
public class MyBundle implements ConfiguredBundle {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
            bootstrap.addBundle(new MyOtherBundle());  

            // line executed after MyOtherBundle init
    }
}
```                           

*Guicey bundle* registers a transitive bundle **after** current bundle, but dropwizard bundles are still immediately initialized.

```java
public class MyBundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
            bootstrap
                .dropwizardBundles(new DwBundle())
                .bundles(new MyOtherBundle());
  
            // line executed before MyOtherBundle init, but after DwBundle init
    }
}
```

Guicey bundle de-duplication logic is further explained [here](guide/deduplication.md). In short, registered root bundles 
must be initialized in priority. This avoids situations like:

```java
GuiceBundle.builder()
    .bundles(new Bundle1(), new Bundle2(12))
```

If `Bundle2` is unique and `Bundle1` transitively installs `new Bundle2(1)` (e.g. with different config),
then this transitive bundle would be ignored because the root bundle's init will appear first.
If guicey bundles worked like dropwizard bundles, then the directly registered `Bundle2` would be ignored
and remaining instance would have different configuration, introducing a major point of confusion.  

Normally, this behaviour should not be an issue as you shouldn't rely on bundles initialization order.
But this may be important with [shared state](#shared-state).

## Bundle vs Module

When extracting functionality into re-usable module always start with a bundle.
A guice module will likely be required as well.

Logic should be separated as:

- *Guice module* is responsible for guice bindings and should not be aware of dropwizard. 
- *Bundle* works with dropwizard, extracts required configuration for creating module and do other registrations.

That's an ideal case. But, for example, if you need to apply some bindings based on configuration only 
then you can do it with pure guice module, like:

```java
public class ModuleConfig {  
    @JsonProperty
    private String something; 
}
```

Module knows that target application (where this re-usable module would be used) will declare
this configuration inside its main configuration:

```java
public class AppConfig extends Configuration {
    @JsonProperty    
    private ModuleConfig module;
}
```

Make module [aware of dropwizard stuff](guide/guice/module-autowiring.md#autowiring-base-class):

```java
public class ModuleImpl<C extends Configuration> extends DropwizardAwareModule<C> {

     @Override
     protected void configure() {
        // obtain sub-configuration object 
        ModuleConfig config = Preconditions.checkNotNull(configuration(ModuleConfig.class),
                "ModuleConfig is not found within application config. Please declare it.");
    
        // use it for binding    
        bind(SomeService.class).annotatedWith(Names.named(config.getSomething())).to(SomeServiceImpl.class);                            
     }
}
```

!!! warning
    This is not the recommended way! It was shown just to demonstrate that guice module *could* be used
    without bundle. It's better to use a declarative bundle instead:
    
    ```java
    public class ModuleImpl extends AbstractBundle {

         private ModuleConfig config;
         
         public ModuleImpl(ModuleConfig config) {
            this.config = config;
         }        
    
         @Override
         protected void configure() {                    
            bind(SomeService.class).annotatedWith(Names.named(config.getSomething())).to(SomeServiceImpl.class);                            
         }
    }
    
    public class ModuleBundle extends GuiceyBundle {
        @Override   
        public void run(GuiceyEnvironment environment) throws Exception {
             ModuleConfig config = Preconditions.checkNotNull(environment.configuration(ModuleConfig.class),
                        "ModuleConfig is not found within application config. Please declare it.");
                        
              environment.modules(new ModuleImpl(config));          
        }
    }
    ```
    
## Bundle tips     

These tips show various techniques for developing bundles.  
Mostly, these tips are based on developing [guicey extensions](guide/modules.md).
See [extensions source](https://github.com/xvik/dropwizard-guicey) for examples.

### Uniqueness

For everything that is registered "by instance", the [de-duplication mechanism](guide/deduplication.md) is applied.

You can use it to provide only one instance of a [bundle](guide/bundles.md#bundle-de-duplication) by `extends 
UniqueGuiceyBundle`. If more sophisticated logic is required, a manual [equals and hash code implementation](guide/deduplication.md#equals-method),
may be used. This could be used to de-duplicate only instances with the same constructor arguments.

This is most applicable to [guice modules](guide/deduplication.md#guice-modules) as guice will not start with duplicate bindings
(`MyModule extends UniqueModule`).

### Auto-loaded bundle

Auto-loading is based on the guicey [bundles lookup](guide/bundles.md#bundle-lookup) feature.

Be aware that a user may switch off bundle lookup (with `.disableBundleLookup()`) or apply [custom lookup](guide/bundles.md#customizing-lookup-mechanism)).

#### Auto load override

If your bundle provides configuration, but you still want to load it automatically with the default configuration,
then you can use [bundle uniqueness](guide/bundles.md#bundle-de-duplication):

```java
public class AutoLoadableBundle extends UniqueGuiceyBundle { ... }
```

If this is used, only one bundle instance is allowed. If a user registers another instance of the bundle manually,
the bundle found from a lookup will simply be ignored. The [lifecycle annotations](extras/lifecycle-annotations.md) module uses 
this technique.


### Optional extensions

All extensions must be registered during the initialization phase, when configuration is not yet available
and so it is not possible to implement optional extension registration. 

To work around this, you can conditionally disable extensions:

```java
public class MyFeatureBundle implements GuiceyBundle {

    @Override
    public void initialize(GuiceyBootstrap bootstrap) {   
        // always register extension
        bootstrap.extensions(OptionalExtension.class);     
    }   

    @Override    
    public void run(GuiceyEnvironment environment) throws Exception {
        // disable extension based on configuration value
        if (!environment.configuration().getSomeValue()) {
            environment.disableExtension(OptionalExtension.class);
        }
    }
}
```    

### Replace features

As bundle has almost complete access to configuration, it can use [disables](guide/disables.md)
to substitute application functions.

For example, it is known that an application uses `ServiceX` from some core module provided by the organization. Your module 
requires a modified service. Your bundle may disable the core module, and install a customized module as a replacement:

```java
public class MyFeatureBundle implements GuiceyBundle {

    @Override
    public void initialize(GuiceyBootstrap bootstrap) {           
        bootstrap
            .disableModules(CoreModule.class)
            .modules(new CustomizedCoreModule());     
    }      
}      
```

!!! note
    This is not the best pattern to follow. It is simpler to use [binding override](guide/guice/override.md)
    to override single service. This is an example for demonstration purposes.
    
    Bundles can't disable other bundles (because target bundle could be already processed at this point).   

### Bundle options

Bundles can use the guicey [options mechanism](guide/options.md) to access guicey option values:

```java
public class MyBundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        if (bootstrap.option(GuiceyOptions.UseHkBridge)) {
            // show warning that bridge required
        } 
    }
}
``` 

There is also support for [custom options](guide/options.md#custom-options).

!!! note
    Option values are set only in main `GuiceBundle`. They are immutable, so all bundles receive the same option values.
    
### Configuration access

A bundle may access direct dropwizard `Configuration`, as well as individual values thanks to [yaml values](guide/yaml-values.md)
introspection.

#### Unique sub config

When creating re-usable bundle it is often required to access yaml configuration data. 
Usually this is solved by some "configuration look-ups" like in [dropwizard-views](https://www.dropwizard.io/en/release-4.0.x/manual/views.html) 

Guicey allows you to obtain the sub-configuration object directly:

```java
public class XFeatureBundle implements GuiceyBundle {
    @Override
    public void run(GuiceyEnvironment environment) throws Exception {
        XFeatureConfig environment = bootstrap.configuration(XFeatureConfig.class);
        ...
    }
}
```   

Note that this bundle doesn't know exact type of user configuration, it just 
assumes that `XFeatureConfig` is declared somewhere in configuration at any level
**just once**. For example:

```java
public class MyConfig extends Configuration {
    
    @JsonProperty
    private XFeatureConfig xfeature;
    
    ...
}
```

!!! important
    Your sub configuration object must appear only once within user configuration.
    
    Object uniqueness checked by exact type match, so if configuration also 
    contains some extending class (`#!java XFeatureConfigExt extends XFeatureConfig`) 
    it will be different unique config. 

#### Access by path

When you are not sure that configuration is unique, you can rely on exact path definition of required sub configuration:

```java
public class XFeatureBundle implements GuiceyBundle {
    private String path;
    
    public XFeatureBundle(String path) {
        this.path = path;
    } 
        
    @Override
    public void run(GuiceyEnvironment environment) throws Exception {
        XFeatureConfig conf = environment.configuration(path);
        ...
    }
}
```

The Path may be declared by the bundle user, who knows required configuration location:

```java
GuiceBundle.builder()
    .bundles(new XFeatureBundle("sub.feature"))
    ...
    .build()
``` 

Where 

```java
public class MyConfig extends Configuration {
    
    @JsonProperty
    private SubConfig sub = { // pseudo code to combine class declarations
         @JsonProperty
         private XFeatureConfig feature;   
    }
    
    ...
}
```

#### Multiple configs

In case, when multiple config objects could be declared in user configuration,
you can access all of them: 

```java
public class XFeatureBundle implements GuiceyBundle {
    @Override
    public void run(GuiceyEnvironment environment) throws Exception {
        List<XFeatureConfig> confs = environment.configurations(XFeatureConfig.class);
        ...
    }
}
``` 

For configuration

```java
public class MyConfig extends Configuration {
    
    @JsonProperty
    private XFeatureConfig xfeature;
    @JsonProperty
    private XFeatureConfig xfeature2;
    
    ...
}
```

This `configurations` method will return both objects: `[xfeature, xfeature2]`

!!! important
    In contrast to unique configurations, this method returns all subclasses as well.
    So if there are `#!java XFeatureConfigExt extends XFeatureConfig` declared somewhere it will also be returned.

#### Custom configuration analysis

In all other cases (with more complex requirements) you can use `ConfigurationTree` object which
represents introspected configuration paths.  

```java
public class XFeatureBundle implements GuiceyBundle {
    @Override
    public void run(GuiceyEnvironment environment) throws Exception {
         // get all properties of custom configuration (ignoring properties from base classes)
        List<ConfigPath> paths = environment.configurationTree()
                .findAllRootPathsFrom(MyConfig.class);
    
        // search for not null values of marked (annotated) classes            
        List markedTypes = paths.stream()
            .filter(it -> it.getValue() != null 
                    && it.getType().getValueType().hasAnnotation(MyMarker.class))
            .map(it -> it.getValue())
            .collect(Collectors.toList());
        ...
    }
}
```

In this example, the bundle searches for properties declared directly in the `MyConfig` configuration
class with non-null values and the custom marker (`@MyMarker`) class annotation.  

See introspected configuration [structure description](guide/yaml-values.md#introspected-configuration) for details.

### Shared state

Guicey maintains special shared state object useful for storing application-wide data.

!!! warning
    Yes, any shared state is a "hack". Normally, you should avoid using it. 
    Guicey provides this ability to unify all such current and future hacks:
    so if you need to communicate between bundles - you don't need to reinvent the wheel
    and don't have additional problems in tests (due to leaking states).
    

For example, it is used by [spa bundle](extras/spa.md) to share list of registered
application names between all spa bundle instances and so be able to prevent duplicate name registration.

[Server pages bundle](extras/gsp.md) use shared state to maintain global configuration
and allow application bundles communication with global views bundle. 

#### Equal communication scenario

Use the following in cases when multiple (equal) bundles need to communicate, the first initialized
bundle will initialize the shared state and others simply use it:

```java
public class EqualBundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        // either obtain already shared object or share new object                                                             
        SomeState state = bootstrap.sharedState(EqualBundle, () -> new SomeState());
        ...
    }
}
```     

#### Parent-child scenario

Use the following in cases when there is one global bundle, which must initialize some global state and child 
bundles, which use or append to this global state:

```java
public class GlobalBundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        // share global state object
        bootstrap.shareState(GlobalBundle, new GlobalState());
    }        
}    

public class ChildBundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        // access shared object or fail when not found
        GlobalState state = environment.sharedStateOrFail(GlobalBundle, 
                "Failed to obtain global state - check if global bundle registered");
    }        
}
``` 

### Before/after run logic

If multiple bundles must be synchronized on run phase, use [guicey events](guide/events.md). 

To run code after all guicey bundles have been initialized, but before run is called:

```java
@Override
public void initialize(final GuiceyBootstrap bootstrap) {
    bootstrap.listen(new GuiceyLifecycleAdapter() {
        @Override
        protected void beforeRun(final BeforeRunEvent event)  {
            // do something before bundles run
            // NOTE that environment and configuration already available!
        }
    });
}
```

To run code after all guicey bundles run methods have been called (delayed init):

```java
@Override
public void run(final GuiceyEnvironment environment) {
    environment.listen(new GuiceyLifecycleAdapter() {
        @Override
        protected void bundlesStarted(final BundlesStartedEvent event) {
            // still sdropwizard run phase (anything could be configured)
            // but all guicey bundles aready executed 
        }
    });
}
```    

!!! note
    This will work only for guicey bundles! Registered dropwizard bundles may
    execute before or after these events. Events are broadcast from the main dropwizard 
    `GuiceBundle` run method, so other dropwizard bundles, registered after guice bundle
    will run after it. 
    It is assumed that guicey bundles will be used for most configurations, but especially 
    in complex cases when bundle synchronization is required. 
