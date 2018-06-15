# Integration lifecycle

Jersey2 guice integration is more complicated than for jersey1, because of [HK2](https://hk2.java.net/2.4.0-b34/introduction.html) container, used by jersey2.

!!! note
    Many people ask why not just use HK2 instead of guice as it's already provided. Unfortunately, it's hard to use it 
    in the same elegant way as we can use guice. HK2 context is launched too late (after dropwizard run phase).
    For example, it is impossible to use HK2 to instantiate dropwizard managed object because managed
    must be registered before HK2 context starts.

Guice integration done in guice exclusive way as much as possible: everything should be managed by guice and invisibly integrated into HK2.
Anyway, it is not always possible to hide integration details, especially if you need to register jersey extensions.

!!! warning ""
    Guice context starts before HK2 context.

## Lifecycle

* Dropwizard configuration phase    
    * Guice bundle registered (in application `initialize` method)
    * Perform [classpath scan for commands](commands.md#automatic-installation) ([optional](configuration.md#commands-search))
* Dropwizard run phase
    * Dropwizard runs bundles (guice bundle is one of them so guice initialization may be performed between other dropwizard bundles)
    * Search guicey bundles in dropwizard bundles ([optional](configuration.md#dropwizard-bundles-unification)) 
    * [Lookup guicey bundles](bundles.md#bundle-lookup)   
    * Apply configuration from guicey bundles
    * **Injector creation** ([using factory](injector.md#injector-factory))
        * [Bind dropwizard objects](bindings.md): Environment, Configuration, Bootstrap
        * Scan for installers (in auto configuration mode)
        * Scan for extensions (in auto configuration mode)
        * Register `GuiceFeature` in environment (jersey `Feature` which will trigger jersey side installations)
        * Apply [lazy jersey bindings](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/jersey/hk2/GuiceBindingsModule.java)
        * Activate [guice servlet support](web.md), register GuiceFilter on admin and main contexts ([could be disabled](configuration.md#servletmodule))
    * Injector created
        * Call installers to register extensions
    * Your application's `run` method executed. Injector is already available, so any guice bean could be [accessed](injector.md)          
* Jersey start
    * [Managed beans](../installers/managed.md) started
    * **HK2 context creation**
        * `GuiceFeature` (registered earlier) called
            * [Optionally](configuration.md#hk2-bridge) register [HK2-guice bridge](https://hk2.java.net/2.4.0-b34/guice-bridge.html) (only guice to hk2 way to let hk2 managed beans inject guice beans)
            * Run jersey specific installers ([resource](../installers/resource.md), [extension](../installers/jersey-ext.md))

!!! note
    Any `EnvironmentCommand` did no start jersey, so managed objects will not be started.
    Also, all jersey related extensions will not be started. Still, core guice context will be completely operable. 

!!! attention ""
    When guice context is created, *jersey context doesn't exist* and when jersey context is created *it doesn't aware of guice existence*.

## Cross context bindings

### Access jersey beans from guice

To access HK2 bindings we need HK2 `ServiceLocator`: it's instance is registered by `GuiceFeature` (in time of HK2 context startup).

Jersey components are bound as providers:

```java
binder.bind(jerseyType).toProvider(new LazyJerseyProvider(jerseyType));
```       

Internally this provider will perform lookup in HK2 service locator:

```java
injector.getInstance(ServiceLocator.class).getService(jerseyType);
```
 
This way jersey beans are "bridged" to guice. They can't be accessed directly in guice beans
at injector creation time (as there is nothing to "bridge" yet). 

`#!java @Inject Provider<JerseyType> provider` must be used to access such beans.     

See more details in [jersey bindings module](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/jersey/hk2/GuiceBindingsModule.java).

### Access guice beans from jersey    

!!! note
    It's almost never required to care about beans visibility from HK2 side because guicey already did all required
    bindings.

HK2 could see all guice beans because of registered guice-bridge. But it doesn't mean HK2 can analyze 
all guice beans to search for extensions (it can resolve only direct injection).
    
Specific jersey installers ([resource](../installers/resource.md), [extension](../installers/jersey-ext.md)) 
create required bindings manually in time of HK2 context creation.

[Jersey extensions installer](../installers/jersey-ext.md) handles most specific installation cases
(where HK2 knowledge is required). It uses the same technic, as the other side binding:

```java
binder.bindFactory(new LazyGuiceProvider(guiceType)).to(type)
```

On request, factory will simply delegate lookup to guice injector:

```java
injector.getInstance(guiceType);
```

!!! tip
    If you just want to add some beans in HK2 context, annotate such beans with `@Provider` and `@HK2Managed` - provider
    will be recognized by installer and HK2 managed annotation will trigger simple registration (overall it's the same
    as write binding manually).
    ```java
    @HK2Managed
    @Provider
    public class MyBeanMangedByHK2 { ... }    
    ```

For more details look [jersey provider installer](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/provider/JerseyProviderInstaller.java)

## Problematic cases    

The problems may appear with binding of jersey extensions.
Good example is [`ValueFactoryProvider`](../installers/jersey-ext.md#valuefactoryprovider). Most likely you will use `AbstractValueFactoryProvider` as base class, but it declares
direct binding for `MultivaluedParameterExtractorProvider`. So such bean would be impossible to create eagerly in guice context.

There are two options to solve this:

* use `@LazyBinding`: bean instance will not be created together with guice context (when `MultivaluedParameterExtractorProvider` is not available),
and creation will be initiated by HK2, when binding could be resolved.
* or use `@HK2Managed` this will delegate instance management to HK2, but still guice services [may be injected](configuration.md#hk2-bridge).

In other cases simply wrap jersey specific bindings into `Provider`.
