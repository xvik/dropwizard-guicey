# Guicey bundles

By analogy with dropwizard bundles, guicey has it's own `GuiceyBundle`. These bundles contains almost the same options as 
main `GuiceBundle` builder. The main purpose is to group installers, extensions and guice modules related to specific 
feature.

Guicey bundles are initialized during dropwizard `run` phase. All guice modules registered in bundles will also be checked if 
[dropwizard objects autowiring](module-autowiring.md) required.

For example, custom integration with some scheduler framework will require installers to register tasks and guice module
to configure framework. GuiceyBundle will allow reduce integration to just one bundle installation.

```java
public class XLibIntegrationBundle implements GuiceyBundle {

    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        bootstrap.installers(
                XLibFeature1Installer.class,
                XLibFeature2Installer.class,                
        )
        .modules(new XLibGuiceModule());
    }
}

bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
        .bundles(new XLibIntegrationBundle())
        .enableAutoConfig("package.to.scan")
        .build()
);
```

!!! tip
    `GuiceyBootstrap` object used not only for registration, but also provides access to
    `Bootstrap`, `Configuration`, `ConfigurationTree`, `Environment` and `Options` objects.

Bundles may be used to group application features: e.g. ResourcesBundle, TasksBundle (for example, when auto-scan not enabled to decompose configuration).

Bundles are transitive - bundle can install other bundles. 
Duplicate bundles are detected using bundle type, so infinite configuration loops or duplicate configurations are not possible.

!!! warning 
    Be careful if bundle is parameterizable (requires constructor arguments). If two such bundles will be registered, only
    first registration will be actually used and other instance ignored. Note that application configurations (using main GuiceBundle methods) 
    performed before bundles processing and so bundle instance with correct parameters could be registered there.
 
Transitive bundles (or simply a lot of bundles) may cause confusion. Use [diagnostic info](diagnostic.md) to see how guicey was actually configured.  

## Predefined bundles

Guicey ships with few predefined bundles.

### Core installers bundle

Default installers are grouped into `CoreInstallersBundle`. This bundle is always installed implicitly (so you always have default installers).
It may be disabled using `.noDefaultInstallers()` method.

### Web installers bundle

`WebInstallersBundle` provides installers for servlets, filters and listeners installation using servlet api annotations
(@WebServlet, @WebFilter, @WebListener). 

Bundle is not installed by default to avoid confusion. May be enabled using `.useWebInstallers()`. 

NOTE: If web installers used, then you may not need guice ServletModule support. To remove GuiceFilter registrations and ServletModule support use
`.noGuiceFilter()`.

### HK2 debug bundle 

`HK2DebugBundle` is special debug bundle to check that beans properly instantiated by guice or HK2 
(and no beans are instantiated by both).

Only beans installed by installers implementing `JerseyInstaller` (`ResourceInstaller`, `JerseyProviderInstaller`).
All beans must be created by guice and only beans annotated with `@HK2Managed` must be instantiated by HK2.

Bundle may be used in tests. For example using `guicey.bundles` property (see bundles lookup below).

May be enabled by `.strictScopeControl()` shortcut method.

### Diagnostic bundle 

Bundle renders collected guicey [diagnostic information](diagnostic.md).
 
Output is highly configurable, use: `DiagnosticBundle.builder()` to configure reporting (if required).
 
Bundle may be registered with [bundle lookup mechanism](bundles.md#bundle-lookup). For example:

```java
PropertyBundleLookup.enableBundles(DiagnosticBundle.class);
``` 
 
May be enabled by `.printDiagnosticInfo()` shortcut method.

Special shortcut `.printAvailableInstallers()` register diagnostic bundle configured for [showing only installers](diagnostic.md#installers-mode). Useful when you looking for available features.

!!! attention ""
    Only one bundle instance accepted, both options can't be enabled at the same time.

## Dropwizard bundles unification

Guicey bundles and dropwizard bundles may be unified providing single (standard) extension point for both 
dropwizard and guicey features:

```java
public class MixedBundle implements ConfiguredBundle, GuiceyBundle {
    
    public void initialize(Bootstrap<?> bootstrap) {
        // do something in init phase
    }   
                  
    public void initialize(GuiceyBootstrap bootstrap) {
        // apply guicey configurations
    } 
    
    public void run(T configuration, Environment environment) throws Exception {
        // not needed because everything could be done in guicey bundle's method
    } 
} 
``` 

Feature is disabled by default, to enable it use `.configureFromDropwizardBundles()` method.

```java
bootstrap.addBundle(new MixedBundle());
bootstrap.addBundle(GuiceBundle.builder()
        .configureFromDropwizardBundles(true)
        .build()
);
```

When active, all registered dropwizard bundles are checked if they implement `GuiceyBundle`.
Works with both `Bundle` and `ConfiguredBundle` dropwizard bundle types. 

!!! warning 
    Don't assume if guicey bundle's `initialize` method will be called before/after dropwizard bundle's `run` method. 
    Both are possible (it depends if bundle registered before or after GuiceBundle).

## Bundle lookup

Bundle lookup mechanism used to lookup guicey bundles in various sources. It may be used to activate specific bundles
in tests (e.g. HK2DebugBundle) or to install 3rd party extensions from classpath.

Bundle lookup is equivalent to registering bundle directly using builder `bundles` method.

By default, 2 lookup mechanisms active. All found bundles are logged into console.
Duplicate bundles are removed (using bundle class to detect duplicate).

To disable default lookups use `disableBundleLookup`:

```java
bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
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
mechanism, loads all GuiceyBundle services.

This is useful for automatically install 3rd party extensions (additional installers, extensions, guice modules).

3rd party jar must contain services file:

```
META-INF/services/ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
```

File contain one or more (per line) GuiceyBundle implementations. E.g.

```
com.foo.Bundle1
com.foo.Bundle2
```

Then Bundle1, Bundle2 would be loaded automatically on startup.

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
bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
        .bundleLookup(new CustomBundleLookup())
        .build()
```

But it's better to register it through default implementation `DefaultBundleLookup`, which performs composition 
of multiple lookup implementations and logs resolved bundles to console.

```java
bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
        .bundleLookup(new DefaultBundleLookup().addLookup(new CustomBundleLookup()))
        .build()
```

To override list of default lookups:

```java
bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
        .bundleLookup(new DefaultBundleLookup(new ServiceLoaderBundleLookup(), new CustomBundleLookup()))
        .build()
```

Here two lookup mechanisms registered (property lookup is not registered and will not be implicitly added).

## Options

[Options](options.md) could be used in guice module to access guicey configurations:

```java
public class MyModule extends DropwizardAwareModule<MyConfiguration> {
    @Override
    protected void configure() {
        // empty when guicey servlet support is dasabled
        if (options.<EnumSet>get(GuiceyOptions.GuiceFilterRegistration).isEmtpy()) {
            // do nothing
        } else {
            // register servlet module
        }
    }
}
``` 

Or it could be some [custom options](options.md#custom-options) usage.

!!! note
    If you are going to register module inside guicey bundle, you can simply resolve
    option value inside guicey bundle and pass it to module directly.

## Configuration

### Unique feature config

When working with re-usable bundles, it could be handy to rely on unique configuration 
object:

```java
public class XFeatureBundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        XFeatureConfig conf = bootstrap.configuration(XFeatureConfig.class);
        ...
    }
}
``` 

Note that this bundle doesn't known exact type of user configuration, it just 
assumes that XFeatureConfig is declared somewhere in configuration (on any level)
just once. For example:

```java
public class MyConfig extends Configuration {
    
    @JsonProperty
    private XFeatureConfig xfeature;
    
    ...
}
```

!!! important
    Object uniqueness checked by exact type match, so if configuration also 
    contains some extending class (`XFeatureConfigExt`) it will be different unique config. 

### Access by path

When you are not sure that configuration is unique, you can rely on exact path definition:

```java
public class XFeatureBundle implements GuiceyBundle {
    private String path;
    
    public XFeatureBundle(String path) {
        this.path = path;
    } 
        
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        XFeatureConfig conf = bootstrap.configuration(path);
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

!!! warning
    Remember that you can't register 2 bundles with the same class

### Multiple configs

In case, when multiple config objects could be declared in user configuration,
you can access all of them: 

```java
public class XFeatureBundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        List<XFeatureConfig> confs = configurations(XFeatureConfig.class);
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
    So if there are `XFeatureConfigExt` declared somewhere it will also be returned.

### Custom configuration analysis

In all other cases (with more complex requirements) you can use `ConfigurationTree` object which
represents introspected configuration paths.  

```java
public class XFeatureBundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
         // get all properties of custom configuration (ignoring properties from base classes)
        List<ConfigPath> paths = bootstrap.configurationTree()
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

In this example, bundle search for properties declared directly in MyConfig configuration
class with not null value and annotated (classes annotated, not properties!) with custom marker (`@MyMarker`).  

See introspected configuration [structure description](bindings.md#introspected-configuration)
