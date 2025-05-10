# Guicey bundles

Analogous to Dropwizard Bundles, Guicey has its own `GuiceyBundle`. These bundles contain many of the same options as the
main `GuiceBundle` [builder](configuration.md#main-bundle). The purpose of Guicey Bundles is the same as Dropwizard Bundles: 
encapsulate logic by grouping installers, extensions and guice modules related to specific features and libraries.

!!! note
    Guicey bundles are assumed to be used **instead** of dropwizard bundles in guicey-powered application.
    
    It does not mean that drowpizard bundles can't be used - of course they can! There are many
    [existing dropwizard bundles](https://modules.dropwizard.io/thirdparty/) and it would be insane to get rid of them.
    
    It is not possible to register guice modules and use many guicey features from dropwizard bundles.       

Guicey and Dropwizard Bundles share **the same lifecycle**:

```java     
public interface ConfiguredBundle<T> {
    default void initialize(Bootstrap<?> bootstrap) {}    
    default void run(T configuration, Environment environment) throws Exception {}
}

public interface GuiceyBundle {
    default void initialize(GuiceyBootstrap bootstrap) throws Exception {} 
    default void run(GuiceyEnvironment environment) throws Exception {}
}
```

Guicey Bundles are an extension to dropwizard bundles (without restrictions), 
so it is extremely simple to switch from dropwizard bundles.

!!! tip
    With Guicey Bundles, it is possible to implement [plug-and-play](#service-loader-lookup) bundles
    to automatically install a bundle when its jar appears on the classpath. 

Example Guicey bundle:

```java
public class MyFeatureBundle implements GuiceyBundle {

    @Override
    public void initialize(GuiceyBootstrap bootstrap) throws Exception {
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

Example bundles can be found in Guicey itself:

* `CoreInstallersBundle` - all default installers
* `WebInstallersBundle` - servlet and filter annotations support
* `HK2DebugBundle` - guice - HK2 [scope debug tool](hk2.md#hk2-scope-debug)

Even more examples are in [extensions modules](../extras/bom.md)

## Configuration

See all [bundle configuration options](configuration.md#guicey-bundle)

!!! note
    Most configurations appear during the initialization phase.

On run phase it is possible to register guice modules, often requiring direct configuration values.
Also, some extension registration may depend on configuration value.

## Bundle De-duplication

Your bundles may be installed multiple times, and you must always think of what should be **expected behaviour** in these cases.

For example:

```java
.bundles(new MyBundle(), new MyBundle())
```

Bundles are often intended to be used multiple times (for example, [spa bundle](../extras/spa.md)).

But in some cases, only one bundle instance must be installed. For example, [eventbus bundle](../extras/eventbus.md)
must be installed just once. The opposite may be true: a common bundle could be installed by multiple other bundles.

In order to manage these cases guicey provides a [de-duplication mechanism](deduplication.md).

To avoid redundant bundle instances, you can:

* extend `UniqueGuiceyBundle` to allow only one bundle instance
* implement `equals` method (where you can implement any deduplication rules (e.g. based on bundles constructor arguments))

!!! note
    Deduplication could also help in case when your bundle is [available through lookup](#bundle-lookup)
    with default configuration, but could be registered with customized configuration.
    
    In this case, you can also use `UniqueGuiceyBundle`: manually registered bundle will 
    always be registered first, and bundle obtained with lookup mechansm would be considered 
    as duplicate and not used (for example, [eventbus bundle](../extras/eventbus.md) use this)

## Bundle Lookup

The bundle lookup mechanism is used to lookup guicey bundles in various sources. It may be used to activate specific bundles
in tests (e.g. [HK2 scope control bundle](hk2.md#hk2-scope-debug)) or to install 3rd party extensions from classpath.

Bundle lookup is equivalent to registering bundle directly using builder `bundles` method.

!!! note
    Bundles from lookup will always be registered after all manually registered bundles,
    so you can use [de-duplication](deduplication.md) to accept manual instance and deny lookup. 

By default, two lookup mechanisms active: [by property](#system-property-lookup) and 
[with service loader](#service-loader-lookup). 

All found bundles are logged into console:

``` 
INFO  [2019-10-17 14:50:14,304] ru.vyarus.dropwizard.guice.bundle.DefaultBundleLookup: guicey bundles lookup =

    ru.vyarus.dropwizard.guice.diagnostic.support.bundle.LookupBundle
```

You can disable default look-ups with:

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

This is useful for automatically installing 3rd party extensions (additional installers, extensions, guice modules).

!!! note
    This could be used to install bundles with default configuration and proper [de-duplication](deduplication.md).
    If a user register custom bundle version, it will be used and bundle from lookup will be ignored.
     
    For example, [eventbus](../extras/eventbus.md) bundle works like this

3rd party jar must contain services file:

```
META-INF/services/ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
```

The file must contain one GuiceyBundle implementation per line. 

For example:
```
com.foo.Bundle1
com.foo.Bundle2
```

Then Bundle1, Bundle2 would be loaded automatically on startup and appear in logs.

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

To override the list of default look-ups:

```java
bootstrap.addBundle(GuiceBundle.builder()
        .bundleLookup(new DefaultBundleLookup(new ServiceLoaderBundleLookup(), new CustomBundleLookup()))
        .build()
```

Here, only two lookup mechanisms registered (property lookup is not registered and will **not** be implicitly added).

## Dropwizard bundles

Dropwizard bundles can be used as before, registered directly in `Bootstrap`.

Guicey provides direct api for dropwizard bundles registration:

```java
GuiceBundle.builder()
    .dropwizardBundles(new MyDwBundle())
```

and in bundles:

```java
public class MyBundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) throws Exception {
        bootstrap.dropwizardBundle(new MyDwBundle());
    }
}
```    

!!! note
    The most common case is extending some existing 3rd party integration (Dropwizard Bundle)
    with guice bindings (or adding guicey installers for simplified usage). 
    
    ```java
    public class XIntegratuionBundle implements GuiceyBundle {
        @Override
        public void initialize(GuiceyBootstrap bootstrap) throws Exception {
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
* [De-duplication mechanism](deduplication.md#dropwizard-bundles) will work for a bundle, and its transitive bundles

So, if you have a "common bundle" problem (when 2 bundles register some common bundle and so you can use these
bundles together) it could be solved just by registering bundle through the guicey api with 
[proper configuration](deduplication.md#unique-items).

### Transitive bundles tracking

Transitive dropwizard bundles are tracked with a `Bootstrap` object proxy so guicey could intercept the `addBundle` call.

If you have problems with the proxy, you can switch off transitive bundles tracking:

```java
.option(GuiceyOptions.TrackDropwizardBundles, false)
```

If you don't want to switch off tracking, but still have problems registering some bundle, you can always register it directly in 
bootstrap object:

```java
public class MyBundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) throws Exception {
        bootstrap.bootstrap().addBundle(new MyDwBundle());
    }
}
```

`bootstrap.bootstrap()` - is a raw bootstrap (not a proxy).
