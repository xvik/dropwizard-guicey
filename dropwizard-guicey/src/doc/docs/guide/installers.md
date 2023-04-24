# Installers

Installer is a core integration concept: every extension point has its own installer. 
Installers are registered manually or detected by [classpath scan](scan.md).

## Default installers

* [Rest resource](../installers/resource.md) 
* [Dropwizard task](../installers/task.md) 
* [Dropwizard managed object](../installers/managed.md) 
* [Jetty lifecycle](../installers/lifecycle.md)  
* [Health check](../installers/healthcheck.md) 
* [Jersey extensions](../installers/jersey-ext.md)
* [Jersey feature](../installers/jersey-feature.md) 
* [@EagerSingleton](../installers/eager.md)
* [Plugins support](../installers/plugin.md)
* [Http servlet](../installers/servlet.md)
* [Http filter](../installers/filter.md)
* [Servlet context, request, session listener](../installers/listener.md)

!!! tip
    In real application more installers may be available due to 3rd party bundles.
    Use [installers report](diagnostic/installers-report.md) to see all available installers. 

## How it works

All [registered manually extensions](configuration.md#main-bundle), 
classes from [classpath scan](scan.md) and unqualified [guice bindings](guice/module-analysis.md#extensions-recognition)
are recognized by registered installers:

```java
public class FeatureInstaller{
    boolean matches(Class<?> type);
}
```

Detected extensions are bound to guice context either with default `binder.bind(foundClass)` or by installer itself
(default binding is required to support guice `.requireExplicitBindings()` option).

After injector creation, installers register extension in dropwizard (not necessary, but most often). 
For example, installation of extension instance (obtained from injector `#!java injector.getInstance(foundClass)`):

```java
public interface InstanceInstaller<T> {    
    void install(Environment environment, T instance);
}
``` 

Jersey-related extensions are installed later, during jersey context startup:

```java
public interface JerseyInstaller<T> {
    void install(AbstractBinder binder, Injector injector, Class<T> type);
}
```

Installers are [ordered](ordering.md#installers-order).

!!! warning "Each extension is installed by only one installer!"    
    If extension could be recognized by more then one installers, it will be installed only by first 
    matching installer (according to installers order). 

## Writing custom installer

Just for example, suppose we have some scheduling framework and we want to detect extensions,
implementing `ScheduledTask` class.

First of all, installer must implement `FeatureInstaller` interface. Here extension detection must be implemented

```java
public class ScheduledInstaller implements FeatureInstaller {
     @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, ScheduledTask.class);
    }

    // NOTE: report() method will describe later
}  
```

Next, installer must register extension somehow. There may be different options:

* `BindingInstaller` allows custom guice bindings. If installer doesn't implement this interface simple `bind(type)` will be called to register in guice.
* `TypeInstaller` used for registration based on type (no instance created during installation).
* `InstanceInstaller` used for instance registration. Instance created using `injector.getInstance(type)`.
* `JerseyInstaller` used for registration of bindings in HK2 context.

Note that extensions may use `@LazyBinding` annotation. In general case such extensions will not be registered in guice.
In case of `BindingInstaller`, special hint will be passed and installer should decide how to handle it (may throw exception as not supported).

`BindingInstaller` called in time of injector creation, whereas `TypeInstaller` and `InstanceInstaller` are called just after injector creation.
`JerseyInstaller` is called on jersey start.

!!! attention ""
    Installers are not guice beans! So injections can't be used inside them. 
    This is because installers also used during initialization phase and instantiated before injector creation.

For example, our installer would register extension instance into some scheduler framework:

```java
public class ScheduledInstaller implements FeatureInstaller,
                                           InstanceInstaller<ScheduledTask> {
    ...    
    
    @Override
    public void install(Environment environment, ScheduledTask instance) {
        SchedulerFramework.registerTask(instance);
    }   
}
```    

!!! tip
    `TypeInstaller` and `InstanceInstaller` could [access injector](guice/injector.md#access-injector) with
    ```java
    InjectorLookup.getInjector(environment).get();
    ```
    
    And [shared state](shared.md#static-access):
    ```java
    SharedConfigurationState.get(environment).get();
    ```

The last remaining part is reporting - we must see all installed beans in console:

```java
public class ScheduledInstaller implements FeatureInstaller,
                                           InstanceInstaller<ScheduledTask> {
    
    private final Reporter reporter = 
            new Reporter(ScheduledInstaller.class, "scheduled tasks =");
    ...    
    
    @Override
    public void install(Environment environment, ScheduledTask instance) {
        SchedulerFramework.registerTask(instance);
        // register for reporting
        reporter.line("(%s)", FeatureUtils.getInstanceClass(instance).getName());
    }   
                          
    @Override
    public void report() {
        reporter.report();    
    }
}
```

Report method [will be called automatically](#reporting) after all extensions installation.
More complex installers may require special reporter (like jersey extensions installer). 


Another example, suppose `CustomFeature` is a base class for our jersey extensions. 
Then installer will be:

```java
public class CustomInstaller implements FeatureInstaller, JerseyInstaller<CustomFeature> {
    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, CustomFeature.class);
    }
    
    @Override
    public void install(final AbstractBinder binder, final Class<CustomFeature> type) {
        JerseyBinding.bindComponent(binder, type, false, false); 
        ...
    }
    
    @Override
    public void report() { 
        ...
    }
}
``` 

Jersey extensions are more usually complex due to binding aspects (especially for native
jersey extensions). But, hopefully you'll never need to do it yourself. 

!!! tip
    For jersey installers see `AbstractJerseyInstaller` base class, containing common utilities.

### Ordering

In order to support [ordering](ordering.md), installer must implement `Ordered` interface.

!!! important
    If installer doesn't implement `Ordering` extensions will not be sorted, 
    even if extensions has `@Order` annotations. 

As example, see [ManagedInstaller](https://github.com/xvik/dropwizard-guicey/tree/dw-2.1/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)

### Options

Installer could also use [guicey options](options.md): 

* it must implement `WithOptions` marker interface
* or extend form `InstallerOptionsSupport` base class (implemented boilerplate)

### Reporting

Installers `report()` method will be called after it finish installation of all found extensions. Report provides
user visibility of installed extensions. 

To simplify reporting use predefined [Reporter](https://github.com/xvik/dropwizard-guicey/tree/dw-2.1/src/main/java/ru/vyarus/dropwizard/guice/module/installer/util/Reporter.java) class. 
See example usage in [ManagedInstaller](https://github.com/xvik/dropwizard-guicey/tree/dw-2.1/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)

```
INFO  [2016-08-21 23:49:49,534] ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller: managed =

    (ru.vyarus.dropwizard.guice.support.feature.DummyManaged)
```

For complex cases, reporter may be extended to better handle installed extensions. As examples see 
[plugin installer reporter](https://github.com/xvik/dropwizard-guicey/tree/dw-2.1/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/plugin/PluginReporter.java)
and [provider installer reporter](https://github.com/xvik/dropwizard-guicey/tree/dw-2.1/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/provider/ProviderReporter.java)

```
INFO  [2016-08-21 23:49:49,535] ru.vyarus.dropwizard.guice.module.installer.feature.plugin.PluginInstaller: plugins =

    Set<PluginInterface>
        (ru.vyarus.dropwizard.guice.support.feature.DummyPlugin1)
        (ru.vyarus.dropwizard.guice.support.feature.DummyPlugin2)

    Map<DummyPluginKey, PluginInterface>
        ONE        (ru.vyarus.dropwizard.guice.support.feature.DummyNamedPlugin1)
        TWO        (ru.vyarus.dropwizard.guice.support.feature.DummyNamedPlugin2)
```

### Generics

Guicey brings [generics-resolver](https://github.com/xvik/generics-resolver) which you 
can use in installers implementation.

For example, to get extension interface parametrization:

```java
interface Extension<V> {}

class ListExtension implements Extension<List<String>> {}

GenericsResolver.resolve(ListExtension.class)
        .type(Extension.class)
        .genericType("V") == List<String> // (ParameterizedType) 
```

Guicey itself use it for:

* types resolution during configuration introspection (`ConfigTreeBuilder`)
* to introspect type hierarchy and recognize all jersey extensions (`JerseyProviderInstaller`)
* format type for console reporting (`ProviderReporter`) 
* bing jersey extensions to correct types (`JerseyBinding`) 
