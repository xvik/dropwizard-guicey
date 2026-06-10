# HK2

!!! danger
    Someday Guicey will get rid of HK2 usage completely, which means
    HK2-related API and features would be removed.

    But, as it requires a lot of effort, all HK2-related APIs are **softly deprecated**
    instead — this means there is no direct deprecation and only
    Javadoc mentions "soft deprecation". Please try to avoid using HK2 at all.

    Previous strong deprecation was removed because there are no
    replacements provided for the current API (and it's not clear when complete removal will happen).

By default, Guicey manages all extensions under the Guice context and only registers
extensions in HK2 by instance. Normally you shouldn't know about HK2 at all.

## Jersey objects

Guice starts before HK2, and so all Jersey-related beans can be registered in the Guice context
only with lazy providers:

```java
binder.bind(jerseyType).toProvider(new JerseyComponentProvider(injectorProvider, jerseyType));
```

This provider will use Jersey's `InjectionManager` to look up a bean:

```java
injectionManagerInstance.getInstance(type);
```

See more details in [jersey bindings module](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/jersey/hk2/GuiceBindingsModule.java).

### Access HK2 context from Guice

Jersey's `InjectionManager` is available for injection inside the Guice context:

```java
@Inject Provider<InjectionManager> jerseyInjector;
```

The provider is important because Jersey starts after Guice.

!!! note
    `InjectionManager` is Jersey's new DI-agnostic API. You can obtain HK2
    `ServiceLocator` from it, if required:

    ```java
    ServiceLocator locator = jerseyInjector.getInstance(ServiceLocator.class);
    ```

## Jersey extensions

Jersey-related extensions ([resources](../installers/resource.md), [features](../installers/jersey-feature.md)
and [providers](../installers/jersey-ext.md)) are registered in HK2 with lazy factories
(laziness is important to respect scopes and help with cyclic initialization cases (when Guice beans depend on HK2 beans)):

```java
binder.bindFactory(new GuiceComponentFactory<>(injector, guiceType)).to(guiceType)
```

And internally this factory will obtain an instance from the Guice injector:

```java
guiceInjector.getInstance(guiceType);
```

To see more details on how it works, look at [jersey providers installer](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/provider/JerseyProviderInstaller.java).

## HK2 delegation

You can delegate extension instance management to HK2 with the `@JerseyManaged` annotation:

```java
@Provider
@JerseyManaged
public class MapperManagedByHK2 implements ExceptionMapper { ... }
```

Now this extension will not be instantiated by Guice — HK2 will instantiate it.

!!! warning
    Delegated beans will not be affected by Guice AOP and will not see Guice
    beans (if the [bridge is not activated](#hk2-guice-bridge))

!!! tip
    You can use `.strictScopeControl()` to make sure that beans are not instantiated
    by both DI containers.

## HK2 guice bridge

The HK2 bridge may be required ONLY if you [delegate](#hk2-delegation) some bean creation to HK2 (instead of Guice)
but those beans still require Guice-managed bean injection (HK2 must be able to see Guice bindings).

To activate bridge:

* Add dependency: `org.glassfish.hk2:guice-bridge:2.6.1` (version must match the HK2 version used by Dropwizard)
* Enable option: `#!java .option(GuiceyOptions.UseHkBridge, true)`

After that, HK2 beans can inject Guice beans:

```java
@JerseyManaged
public class HkService {

    @Inject
    private GuiceService service;
}
```

## Use HK2 for jersey extensions

By default, Guice is used to construct all extensions, including Jersey-related
([resources](../installers/resource.md), [features](../installers/jersey-feature.md)
and [providers](../installers/jersey-ext.md)) which are registered in HK2 context as instances.

If you want to delegate all Jersey extensions to HK2, then use:

```java
GuiceBundle.builder()
    ...
   .useHK2ForJerseyExtensions()
   .build()
```

(It is the same as annotating all Jersey extensions with `@JerseyManaged`.)

After enabling it, all Jersey extensions will be created by HK2.
Option requires [HK2-guice bridge](#hk2-guice-bridge) (error will be thrown if bridge is not available in classpath)
to use Guice services inside HK2-managed beans.

!!! warning
    Guice AOP will work only for instances created by Guice, so after enabling this option you will not
    be able to use AOP on Jersey extensions.

By analogy with `@JerseyManaged`, you can use `@GuiceManaged` to mark exceptional extensions,
which must still be managed by Guice.

## HK2 scope debug

A special `HK2DebugBundle` bundle is provided to check that beans are properly instantiated by Guice or HK2
(and no beans are instantiated by both).

It can be activated with the shortcut:

```java
.strictScopeControl();
```

Affects only beans installed by installers implementing `JerseyInstaller` (`ResourceInstaller`, `JerseyProviderInstaller` etc)
because other extensions do not support delegation to HK2.

Checks that beans annotated with `@GuiceManaged` are instantiated by Guice and beans
annotated with `@JerseyManaged` are created by HK2.

In default Guice-first mode, non-annotated beans are assumed to be instantiated in Guice (and so an error is thrown
if a bean is created in the HK2 context).

In [HK2-first](#use-hk2-for-jersey-extensions) mode, an error will be thrown if a non-annotated Jersey extension is created by Guice.
