# Module autowiring   

Because guice modules are registered in dropwizard init section only `Bootstrap` instance is available.
Often `Environment` and `Configuration` objects are also required.

## Autowiring interfaces 

Guicey can automatically inject environment objects into your module if 
it implements any of (or all of them): 

* `BootstrapAwareModule` - access bootstrap instance 
* `EnvironmentAwareModule` - access environment instance 
* `ConfigurationAwareModule` - access configuration instance
* `ConfigurationTreeAwareModule` - access to configuration values by path
* `OptionsAwareModule` - access guicey options

Reference object will be set to module just before injector creation, so you can use it inside your 
module logic (`configuration` method).

!!! warning
    Module autowiring will only work for modules directly set to `modules()` (of main bundle or any guicey bundle).

```java
public class MyModule implements EnvironmentAwareModule {
    private Environemnt environment;
    
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    
    @Override
    protected void configure() {
        // use environment here
    }
}
```

## Autowiring base class

To avoid manually implementing interfaces (avoid boilerplate) you can use `DropwizardAwareModule` as 
base class which already implements all autowiring interfaces:

```java
public class MyModule extends DropwizardAwareModule<MyConfiguration> {
    @Override
    protected void configure() {
        bootstrap()     // Bootstrap instance
        environment()   // Environment instance
        configuration() // MyConfiguration instance
        appPackage()    // application class package 
        configuratonTree() // configuration as tree of values
        confuguration(Class) // unique sub configuration
        configuration(String) // configuration value by yaml path
        configurations(Class) // sub configuration objects by type (including subtypes)
        options() // access guicey options
    }
} 
```

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

!!! tip
    If you are going to register module inside guicey bundle, you can simply resolve
    option value inside guicey bundle and pass it to module directly.

## Configuration access

!!! tip
    If you are going to register module inside guicey bundle, you can simply resolve
    configuration object inside guicey bundle and pass it to module directly (bundle has 
    absolutely the same configuration access methods)

### Unique feature config

When working with re-usable modules, it could be handy to rely on unique configuration 
object:

```java
public class XFeatureModule extends DropwizardAwareModule<Configuration> {
    @Override
    protected void configure() {
        XFeatureConfig conf = configuration(XFeatureConfig.class);
        ...
    }
}
``` 

Note that this module doesn't known exact type of user configuration, it just 
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
public class XFeatureModule extends DropwizardAwareModule<Configuration> {
    
    private String path;
    
    public XFeatureModule(String path) {
        this.path = path;
    } 
    
    @Override
    protected void configure() {
        XFeatureConfig conf = configuration(path);
        ...
    }
}
```

Path is declared by module user, who knows required configuration location:

```java
GuiceBundle.builder()
    .modules(new XFeatureModule("sub.feature"))
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

### Multiple configs

In case, when multiple config objects could be declared in user configuration,
you can access all of them: 

```java
public class XFeatureModule extends DropwizardAwareModule<Configuration> {
    @Override
    protected void configure() {
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
public class XFeatureModule extends DropwizardAwareModule<Configuration> {
    @Override
    protected void configure() {
        // get all properties of custom configuration (ignoring properties from base classes)
        List<ConfigPath> paths = configurationTree().findAllRootPathsFrom(MyConfig.class);
        
        List markedTypes = paths.stream()
            .filter(it -> it.getValue() != null 
                    && it.getType().getValueType().hasAnnotation(MyMarker.class))
            .map(it -> it.getValue())
            .collect(Collectors.toList());
        ...
    }
}
```

In this example, module search for properties declared directly in MyConfig configuration
class with not null value and annotated (classes annotated, not properties!) with custom marker (`@MyMarker`).  

See introspected configuration [structure description](bindings.md#introspected-configuration)
