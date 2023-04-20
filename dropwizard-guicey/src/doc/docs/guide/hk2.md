# HK2

!!! danger
    Guicey will get rid of HK2 usage completely in the next version and all
    HK2-related api and features would be removed.
    
    All api supposed to be removed is marked as deprecated now. But there are no
    replacemenets provided. Please try to avoid using HK2 at all.
    
By default, guicey manage all extensions under guice context and only register 
extensions in HK2 by instance. Normally you shouldn't know about HK2 at all.

## Jersey objects

Guice started before HK2 and so all jersey-related beans could be registered in guice context 
only with lazy providers:

```java
binder.bind(jerseyType).toProvider(new JerseyComponentProvider(injectorProvider, jerseyType));
```

This provider will use jersey's `InjectionManager` to lookup bean:
 
```java
injectionManagerInstance.getInstance(type);
```

See more details in [jersey bindings module](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/jersey/hk2/GuiceBindingsModule.java).

### Access HK2 context from guice

Jersey's `InjectionManager` is available for injection inside guice context:

```java
@Inject Provider<InjectionManager> jerseyInjector;
```

Provider is important because jersey starts after guice.

!!! note
    `InjectionManager` is jersey's new DI-agnostic api. You can obtain HK2 
    `ServiceLocator` from it, if required: 
    
    ```java
    ServiceLocator locator = jerseyInjector.getInstance(ServiceLocator.class);
    ``` 

## Jersey extensions

Jersey-related extensions ([resources](../installers/resource.md), [features](../installers/jersey-feature.md) 
and [providers](../installers/jersey-ext.md)) are registered in HK2 with lazy factories
(laziness is important to respect scopes and help with cycled initialization cases (when guice beans depend on HK2 beans)):

```java
binder.bindFactory(new GuiceComponentFactory<>(injector, guiceType)).to(guiceType)
```

And internally this factory will obtain instance from guice injector:

```java
guiceInjector.getInstance(guiceType);
```        

To see more details on how it works look [jersey providers installer](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/provider/JerseyProviderInstaller.java).

## HK2 delegation

You can delegate extension instance management into HK2 with `@JerseyManaged` annotation:

```java
@Provider
@JerseyManaged
public class MapperManagedByHK2 implements ExceptionMapper { ... }
```

Now this extension will not be instantiated by guice - HK2 will instantiate it.

!!! warning
    Delegated beans will not be affected with guice AOP and will not see guice
    beans (if [bridge not activated](#hk2-guice-bridge))

!!! tip
    You can use `.strictScopeControl()` to make sure that beans are nto instantiated
    by both DI containers.
    
## HK2 guice bridge    

HK2 bridge may be required ONLY if you [delegate](#hk2-delegation) some beans creation to HK2 (instead of guice)
but beans still require guice-managed beans injection (HK2 must be able to see guice bindings).

To activate bridge:

* Add dependency: `org.glassfish.hk2:guice-bridge:2.6.1` (version must match HK2 version, used by dropwizard)
* Enable option: `#!java .option(GuiceyOptions.UseHkBridge, true)`

After that, HK2 beans could inject guice beans:

```java
@JerseyManaged
public class HkService {
    
    @Inject
    private GuiceService service;
}
```   

## Use HK2 for jersey extensions 

By default, guice is used to construct all extensions, including jersey related 
([resources](../installers/resource.md), [features](../installers/jersey-feature.md) 
and [providers](../installers/jersey-ext.md)) which are registered in HK2 context as instances.

If you want to delegate all jersey extensions to HK2 then use:

```java
GuiceBundle.builder()
    ...
   .useHK2ForJerseyExtensions()
   .build() 
```

(It is the same as if you annotate all jersey extensions with `@JerseyManaged`)

After enabling, all jersey extensions will be created by HK2. 
Option requires [HK2-guice bridge](#hk2-guice-bridge) (error will be thrown if bridge is not available in classpath)
to use guice services inside HK2 managed beans.

!!! warning
    Guice AOP will work only for instances created by guice, so after enabling this option you will not
    be able to use aop on jersey extensions.

By analogy with `@JerseyManaged`, you can use `@GuiceManaged` to mark exceptional extensions,
which must be still managed by guice.

## HK2 scope debug

Special `HK2DebugBundle` bundle is provided to check that beans properly instantiated by guice or HK2 
(and no beans are instantiated by both).

It could be activated with shortcut:

```java
.strictScopeControl();
```

Affects only beans installed by installers implementing `JerseyInstaller` (`ResourceInstaller`, `JerseyProviderInstaller` etc)
because other extensions does not support delegation to HK2.

Checks that beans annotated with `@GuiceManaged` are instantiated by guice and beans
annotated with `@JerseyManaged` are created by HK2.

In default guice-first mode non annotated beans are assumed to be instantiated in guice (and so error thrown
if bean created in HK2 context). 

In [HK2-first](#use-hk2-for-jersey-extensions) mode, error will be thrown if non annotated jersey extension is created by guice.
