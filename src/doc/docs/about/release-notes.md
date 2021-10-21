# {{ gradle.version }} Release Notes

!!! summary ""
    [5.3.0 release notes](http://xvik.github.io/dropwizard-guicey/5.3.0/about/release-notes/)


* [General](#general)
* [POM](#pom)
* [JUnit 5](#junit-5) 
* [Lambda guice modules](#lambda-guice-modules)
* [JDK16](#jdk16)
* [Shared state](#shared-state)

## General

Dropwizard updated to 2.0.25.

!!! important
    Since dropwizard 2.0.22 [jdk8 compatibility is broken for jdbi3](https://github.com/dropwizard/dropwizard/releases/tag/v2.0.22)
    due to caffeine library upgrade (see [caffeine author explanation](https://github.com/jdbi/jdbi/issues/1853#issuecomment-819101724))
    (actually, new jdbi3 version depends on caffeine 3 while dropwizard itself still depends on caffeine 2).

Special artifact was added: [guicey-jdbi3-jdk8](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-jdbi3-jdk8) fixing caffeine version for you.
Simply use it instead of `guicey-jdbi3` if you need jdk8 support.

## POM

Guicey POM now specifies dependencies versions directly, instead relying on `dependencyManagement` section.
As before, core guicey pom might be used as BOM. This change simplifies dependency resolution for build tools.

Extension modules are also declare versions directly now.

Extensions BOM does not contain properties section anymore (but I doubt anyone ever used it).

## JUnit 5

Fixed support for `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` (the default is `PER_METHOD`).
Now class injections performed in beforeEach instead of instancePostProcessor.

Error message would be thrown now when guicey extensions registered with non-static field (not supported mode of 
starting guicey per test method).
This should prevent incorrect usage confusion: before it was silently not working in this case.  

## Lambda guice modules

Before, modules analysis [was failing for lambda modules](https://github.com/xvik/dropwizard-guicey/issues/160):

```java
GuiceBundle.builder()
        .modules(binder -> binder.bind(SomeService.class))
        .build()
```

Now it is supported. But lambda modules has specifics in guicey reporting:

- Root lambda module class will be shown in the diagnostic report (in a list of root modules)
- Guice bindings report:
    * Will show all root lambda modules bindings below to com.google.inject.Module
      (code links at the end of each binding would lead to correct declaration line)
    * Bindings of lambda modules installed by root (or deeper modules) would be shown
      directly under root module, as if it was declared directly in that module (logically it's correct)

# JDK16

!!! note
    Since JDK 16 `--illegal-access=permit` was changed to `--illegal-access=deny` by default.
    So now, instead of warning, application fails on illegal access.

Guicey configuration analysis was fixed to exclude objects from "sun.*" package from introspection
([170](https://github.com/xvik/dropwizard-guicey/issues/170), [180](https://github.com/xvik/dropwizard-guicey/issues/180))

Guicey-jdbi3 was fixed from [failing due to incorrect javassist usage](https://github.com/xvik/dropwizard-guicey/issues/178)
(new version of [guice-ext-annotations](https://github.com/xvik/guice-ext-annotations/releases/tag/1.4.0) released).

!!! note 
    Dropwizard currently [is not compatible with JDK17](https://github.com/dropwizard/dropwizard/issues/4347).

## Shared state

!!! note
    [Shared state feature](../guide/shared.md) was initially introduces for complex extensions, when multiple bundles must 
    interact with the same state.

The following changes simplifies shared state usage, making it usable for wider range of cases.

`SharedConfigurationState` now could be obtained directly during startup with:

```java
SharedConfigurationState.getStartupInstance() 
```

!!! warning
    During startup, state instance is stored in thread local and so static call would work only from
    application main thread (usually, all configuration appears in one thread so should not be a problem, 
    but just in case).

Direct access is required in cases when there are no way to obtain `Environment` or `Application` objects
(like binding installer, bundle lookup, etc.). Also, now it allows referencing 
main objects everywhere during startup.

Shortcut methods were added to `SharedConfigurationState` instance:

- getBootstrap()
- getApplication()
- getEnvironment()
- getConfiguration()
- getConfigurationTree()

All of them return providers: e.g. `SharedConfigurationState.getStartupInstance().getBootsrap()`
would return `Provider<Bootstrap>`. This is required because target object 
might not be available yet, still there would be a way to initialize some logic with "lazy object" 
(to call it later, when object would be available) at any configuration stage.

Shared state methods were unified in:

- `GuiceyBootstrap` (GuiceyBundle#initialize)
- `GuiceyEnvironment` (GuiceyBundle#run)
- `DropwizardAwareModule`

To simplify all kinds of state manipulations.

!!! tip
    Before, it was assumed that shared state must be initialized only under 
    initialization phase (only in `GuiceyBindle`) and used under runtime phase 
    (it was possible to workaround limitation with manual state resolution).
    
    Now shared state is assumed as more general tool and the same 
    shortcuts for main oprations available everywhere.

    As before, state might be initialized only once.

It was also recommended before to use bundle classes for state keys.
Now it is not strictly recommended (javadoc changed).
