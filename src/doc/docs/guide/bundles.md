# Guicey bundles

By analogy with dropwizard bundles, guicey has it's own `GuiceyBundle`. These bundles contains almost the same options as 
main `GuiceBundle` [builder](configuration2.md#main-bundle). The main purpose is the same as dropwizard bundles: incapsulate logic by grouping installers, 
extensions and guice modules related to specific feature.

!!! note
    Guicey bundles are assumed to be used **instead** of dropwizard bundles in guicey-powered application.
    
    It does not mean that drowpizard bundles can't be used - of course they can! There are many
    [existing dropwizard bundles](https://modules.dropwizard.io/thirdparty/) and it would be insance to get rid of them.
    
    It is just not possible to register guice modules and use many guicey features from dropwizard bundles.       

Guicey and dropwizard bundles share **the same lifecycle**:

```java     
public interface ConfiguredBundle<T> {
    default void initialize(Bootstrap<?> bootstrap) {}    
    default void run(T configuration, Environment environment) throws Exception {}
}

public interface GuiceyBundle {
    default void initialize(GuiceyBootstrap bootstrap) {} 
    default void run(GuiceyEnvironment environment) throws Exception {}
}
```

Guicey bundles is an extension to dropwizard bundles (without restrictions), 
so it is extremely simple to switch from dropwizard bundles.

!!! tip
    With guicey bundles it is possible to implement [plug-and-play](#service-loader-lookup) bundles:
    to automatically install bundle when it's jar appear on classpath. 

Example guicey bundle:

```java
public class MyFeatureBundle implements GuiceyBundle {

    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        bootstrap
            .installers(MyFeatureExtensionInstaller.class)
            // dropwizard bundle usage
            .dropwizardBundle(new RequiredDwBundle())
            .modules(new MyFeatureModule());     
                                                                 
        // dropwizard bootstrap access
        bootstrap.bootstrap().addCommand(new MyFeatureCommand());
    }   

    @Override    
    public void run(GuiceyEnvironment environment) throws Exception {
        // configuration access
        environment
            .modules(new SpecialModle(environment.configuration().getSomeValue()))
            .onApplicationStartup(() -> logger.info("Application started!"));
    
        // dropwizard environment access        
        environment.environment().setValidator(new MyCustomValudatior());
    }
}

bootstrap.addBundle(GuiceBundle.builder()                        
        .bundles(new MyFeatureBundle())
        .build()
);
```

Example bundles could by found in guicey itself:

* `CoreInstallersBundle` - all default installers
* `WebInstallersBundle` - servlet and filter annotations support
* `HK2DebugBundle` - guice - HK2 [scope debug tool](hk2.md#hk2-scope-debug)

Even more examples are in [extensions modules](../extras/bom.md)

## Configuration

See all [bundle configuration options](configuration2.md#guicey-bundle)

!!! note
    Almost all configurations appear under initialization phase only. This was done in order
    to follow dropwizard conventions (all configuration under init and all initialization under run).

The only exception is guice modules: it is allowed to register modules in both phases. 
Modules are often require direct configuration values and without this exception it would
be too often required to create wrapper [guicey-aware](guice/module-autowiring.md) modules
for proper registration. Besides, in dropwizard itself HK2 modules could only be registered in run phase.   

## De-duplication

Your bundle may be installed multiple times and you must always think what is **expected behaviour**
in this case.

For example:

```java
.bundles(new MyBundle(), new MuBundle())
```

Bundles are often intended to be used multiple times (for example, [spa bundle](../extras/spa.md)).

But in some cases, only one bundle instance must be installed. For example, [eventbus bundle](../extras/eventbus.md)
must be installed just once. Or it may be some common bundle, installed by multiple other bundles.

In order to solve such cases guicey provides [de-duplication mechanism](deduplication.md).

So when you need to avoid redundant bundle instances, you can:

* extend `UniqueGuiceyBundle` to allow only one bundle instance
* implement `equals` method (where you can implement any deduplication rules (e.g. based on bundles constructor arguments))

!!! note
    Deduplication could also help in case when your bundle is [available through lookup](#bundle-lookup)
    with default configuration, but could be registered with customized configuration.
    
    In this case, you can also use `UniqueGuiceyBundle`: manually registered bundle will 
    always be registered first, and bundle obtained with lookup mechansm would be considered 
    as duplicate and not used (for example, [eventbus bundle](../extras/eventbus.md) use this)

## Bundle technics

### Optional extensions

As all extensions registered under initialization phase, when configuration is not available yet
it is not possible to implement optional extension registration. To workaround it, you can conditionally
disable extensions:

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

### Replaces

As bundle has almost complete access to configuration, it can use [disables](disables.md)
to substitute application functions.

For example, it is known that application use `ServiceX` (from some core module in organization),
but this bundle requires modified service. It can disable core module, installing feature 
and register customized module:

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
    It's not the best example: of course it's simpler to use [binding override](guice/override.md)
    to override single service. But it's just to demonstrate the idea (it could be repalced
    extension or fixed installer). 
    
    Bundles can't disable other bundles (because target bundle could be already processed at this point).   

### Options

Bundle could use guicey [options mechanism](options.md) to access guicey option values:

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

Or it could be some [custom options](options.md#custom-options) usage.

!!! note
    Option values are set only in main `GuiceBundle` and so all bundles see the same option values
    (no one can change values after). 

### Configuration access

Bundle could access not only direct dropwizard `Configuration`, but also individual values  
thanks to [yaml values](yaml-values.md) introspection.

#### Unique sub config

When creating re-usable bundle it is often required to access yaml configuration data. 
Usually this is solved by some "configuration lookups" like in [dropwizard-views](https://www.dropwizard.io/en/stable/manual/views.html) 

Guicey allows you to obtain sub-configuration object directly:

```java
public class XFeatureBundle implements GuiceyBundle {
    @Override
    public void run(GuiceyEnvironment environment) throws Exception {
        XFeatureConfig environment = bootstrap.configuration(XFeatureConfig.class);
        ...
    }
}
```   

Note that this bundle doesn't known exact type of user configuration, it just 
assumes that `XFeatureConfig` is declared somewhere in configuration (on any level)
just once. For example:

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

When you are not sure that configuration is unique, you can rely on exact path definition
(of required sub configuration):

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

Path is declared by bundle user, who knows required configuration location:

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

It wil return both objects: `[xfeature, xfeature2]`

!!! important
    In contrast to unique configurations, this method returns all subclasses too.
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

In this example, bundle search for properties declared directly in `MyConfig` configuration
class with not null value and annotated (classes annotated, not properties!) with custom marker (`@MyMarker`).  

See introspected configuration [structure description](yaml-values.md#introspected-configuration).

### Shared state

Guicey maintains special shared state object useful for storing application-wide data.

!!! warning
    Yes, any shared state is a "hack". Normally, you should avoid using it. 
    Guicey provides this ability to unify all such current and future hacks:
    so if you need to communicate between bundles - you don't need to reinvent the wheel
    and don't have additional problems in tests (due to leaking states).
    

For example, it is used by [spa bundle](../extras/spa.md) to share list of registered
application names between all spa bundle instances and so be able to prevent duplicate name registration.

[Server pages bundle](../extras/gsp.md) use shared state to maintain global configuration
and allow application bundles communication with global views bundle. 

#### Equal communication scenario

Case when multiple (equal) bundles need to communicate. In this case first initialized
bundle init shared state and others simply use it.

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

Case when there is one global bundle, which must initialize some global state and child 
bundles, which use or appends to this global state.

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

## Bundle lookup

Bundle lookup mechanism used to lookup guicey bundles in various sources. It may be used to activate specific bundles
in tests (e.g. [HK2 scope control bundle](hk2.md#hk2-scope-debug)) or to install 3rd party extensions from classpath.

Bundle lookup is equivalent to registering bundle directly using builder `bundles` method.

!!! note
    Bundles from lookup will always be registered after all manually registered bundles
    so you can use [de-cuplication](deduplication.md) to accept manual instance and deny lookup. 

By default, two lookup mechanisms active: [by property](#system-property-lookup) and 
[with service loader](#service-loader-lookup). 

All found bundles are logged into console:

``` 
INFO  [2019-10-17 14:50:14,304] ru.vyarus.dropwizard.guice.bundle.DefaultBundleLookup: guicey bundles lookup =

    ru.vyarus.dropwizard.guice.diagnostic.support.bundle.LookupBundle
```

You can disable default lookups with:

```java
bootstrap.addBundle(GuiceBundle.builder()
        .disableBundleLookup()
        .build()
```

### System property lookup

System property `guicey.bundles` could contain comma separated list of guicey bundle classes. These bundles 
must have no-args constructor.

For example, activate HK2 debug bundle for tests:

```
java ... -Dguicey.bundles=ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle
```

Alternatively, system property may be set in code:

```java
PropertyBundleLookup.enableBundles(HK2DebugBundle.class)
```

### Service loader lookup

Using default java [ServiceLoader](https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html) 
mechanism, loads all `GuiceyBundle` services.

This is useful for automatically install 3rd party extensions (additional installers, extensions, guice modules).

!!! note
    This could be used to install bundle with default configuration: with proper [de-duplication](deduplication.md)
    if user register custom bundle version, it will be used and bundle from lookup will be ignored.
     
    For example, [eventbus](../extras/eventbus.md) bundle works like this

3rd party jar must contain services file:

```
META-INF/services/ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
```

File contain one or more (per line) GuiceyBundle implementations. E.g.

```
com.foo.Bundle1
com.foo.Bundle2
```

Then Bundle1, Bundle2 would be loaded automatically on startup (would appear in logs).

### Customizing lookup mechanism

Custom bundle lookup must implement `GuiceyBundleLookup` interface:

```java
public class CustomBundleLookup implements GuiceyBundleLookup {

    @Override
    public List<GuiceyBundle> lookup() {
        List<GuiceyBundle> bundles = Lists.newArrayList();
        ...
        return bundles;
    }
}
```

Custom lookup implementation may be registered through:

```java
bootstrap.addBundle(GuiceBundle.builder()
        .bundleLookup(new CustomBundleLookup())
        .build()
```

But it's better to register it through default implementation `DefaultBundleLookup`, which performs composition 
of multiple lookup implementations and logs resolved bundles to console.

```java
bootstrap.addBundle(GuiceBundle.builder()
        .bundleLookup(new DefaultBundleLookup().addLookup(new CustomBundleLookup()))
        .build()
```

To override list of default lookups:

```java
bootstrap.addBundle(GuiceBundle.builder()
        .bundleLookup(new DefaultBundleLookup(new ServiceLoaderBundleLookup(), new CustomBundleLookup()))
        .build()
```

Here two lookup mechanisms registered (property lookup is not registered and will not be implicitly added).

## Dropwizard bundles

Dropwizard bundles can be used as before: be registered directly in `Bootstrap`.

Guicey provides direct api for dropwizard bundles registration:

```java
GuiceBundle.builder()
    .dropwizardBundles(new MyDwBundle())
```

and in bundles:

```java
public class MyBundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.dropwizardBundle(new MyDwBundle());
    }
}
```    

!!! note
    The most common case is: extending some existing 3rd party integration (dropwizard bundle)
    with guice bindings (or adding guicey installers for simplified usage). 
    
    ```java
    public class XIntegratuionBundle implements GuiceyBundle {
        @Override
        public void initialize(GuiceyBootstrap bootstrap) {
            bootstrap
                .dropwizardBundle(new DropwizardXBundle());
                .modules(new XBindingsModule())
        }
    }
    ```     
    
    [JDBI bundle](../extras/jdbi3.md) could be king of such example: it does not use dropwizard
    bundle, but it defines additional extension types to simplify configuration. 

When you register dropwizard bundles through guicey api:

* Bundle (and all transitive bundles) appear in [report](diagnostic/configuration-report.md)
* Bundle itself or any transitive bundle could be [disabled](disables.md#disable-dropwizard-bundles)
* [De-duplication mechanism](deduplication.md#dropwizard-bundles) will work for bundle and it's transitive bundles

So, for example, if you have "common bundle" problem (when 2 bundles register some common bundle and so you can use these
bundles together) it could be solved just by registering bundle throug guicey api (and [proper configuration](deduplication.md#unique-items))

### Transitive bundles tracking

Transitive dropwizard bundles are tracked with a `Bootstrap` object proxy 
(so guicey could intercept `addBundle` call).

If you have problems with it, you can switch off transitive bundles tracking:

```java
.option(GuiceyOptions.TrackDropwizardBundles, false)
```

If you don't want to switch off tracking, but still have problems registering some bundle,
you can always register it directly in bootstrap object:

```java
public class MyBundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.bootstrap().addBundle(new MyDwBundle());
    }
}
```

`bootstrap.bootstrap()` - is a raw bootstrap (not a proxy).