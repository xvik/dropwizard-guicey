# Shared configuration state

Sometimes is is required to pass configuration values between different application parts
or implement bundles communication. In this cases usually you have to use `ThreadLocal` (direct static fields can't 
be used because it will make problems for tests).

!!! attention
    This is edge cases - they are really rare and should be avoided if possible.

Guicey adds shared state support in order to replace all current and future hacks (and 
so avoid unexpected side effects for tests).

Shared state is created together with `GuiceBundle` creation and destroyed with application shutdown.
Internally it is implemented as static map with value reference by application instance.

!!! note
    Shared state content is intentionally not logged (there is no report for that) because
    it is an internal state. Don't abuse it! It myst be used only for edge cases.
    
Guicey use it for storing `Injector` object lookup ([InjectorLookup](guice/injector.md#access-injector) is actually a shortcut).

[SPA](../extras/spa.md) and [GSP](../extras/gsp.md) bundles use it for bundles communication.    

## Shared state restrictions

Internally shared state is a `Map<Class, Object>`. Class is used as key because assumed 
usage scope is bundle and it will force you to use bundle class as a key. Moreover non string
key reduce dummy errors: typos (internally, values are stored by string class name to unify keys from different class loaders). 

Other restrictions:

* State value can be **set just once**! This is simply to avoid hard to track problems with overridden state.
* State value can't be null! Again, to avoid problems with NPE errors.

It is assumed that state will be used not for simple values, but for shared configuration objects.

## Main bundle

It is assumed that there should be no need to access shared state from [main bundle](configuration.md#main-bundle).
So [the only state-related method](configuration.md#hooks-related) actually assumed to be used by [hooks](hooks.md):
 
```java
static class XHook implements GuiceyConfigurationHook {
    @Override
    void configure(GuiceBundle.Builder builder) {
        builder.withSharedState(state -> {
            state.put(XHook, new SharedObject());
        });
    }
}
```  

## Guicey bundle

Shared state is assumed to be used by bundles. Bundle provides special [shortcut methods](configuration.md#guicey-bundle) 
for accessing state. It is assumed that state is declared under initialization phase and
could be accessed under both phases.

For usage examples see [bundles section](bundles.md#shared-state).

## Guice modules

Shared state is not intended to be used in guice modules, but it is possible. 
To simplify usage there are shortcuts available in [dropwizard aware module](guice/module-autowiring.md#shared-state) base class. 

## Static access

If required, shared state could be accessed statically everywhere:

```java
SharedConfigurationState.get(application)
```                                      

Or direct value access:

```java
SharedConfigurationState.lookup(application, XBundle.class)
```                    

And it is possible to use `Environment` instance for access:

```java
SharedConfigurationState.get(environment)
SharedConfigurationState.lookup(environment, XBundle.class)
```

Special shorcut methods may be used for "get or fail behaviour":

```java
SharedConfigurationState.lookupOrFail(app, XBundle.class, 
        "Failed to lookup %s service", XBundle.class.getSimpleName())
``` 

It will throw IllegalStateException if shared context is not available or no value.
Note that message is formatted with `String.format`. 

## Tests

Shared state is referenced by application instance, so there should not be any
problems with tests.

The only possible side effect is when you test many application startup error situations, 
when application did not shutdown properly and so some shared contexts may not be removed.
If it (hard to imagine how) will affect your tests, you can forcefully clean all states:

```java
SharedConfigurationState.clear()
```                         

## Default objects

The following objects are available in shared state just in case:

* Bootstrap
* Environment
* Configuration
* [ConfigurationTree](yaml-values.md)

So any of it could be accessed statically with application or environment instance:

```java
Optional<Bootstrap> bootstrap = SharedConfigurationState
            .lookup(environment, Bootstrap.class);
```                

or

```java
Bootstrap bootstrap = SharedConfigurationState
            .lookupOrFail(environment, Bootstrap.class, "No bootstrap available");
``` 