# Shared configuration state

Sometimes, it is required to pass configuration values between different application parts
or implement bundles communication. In these cases usually you have to use `ThreadLocal` (direct static fields can't 
be used because it will make problems for tests).

!!! attention
    Use it only when it's not possible to avoid.

Guicey adds shared state support in order to replace all current and future hacks (and 
so avoid unexpected side effects for tests).

Shared state is created together with `GuiceBundle` creation and destroyed with application shutdown.
Internally it is implemented as static map with value reference by application instance.

!!! note
    Don't abuse it! It must be used only for edge cases. To better understand
    shared state usage, there is a [special report](diagnostic/shared-state-report.md).
    
Guicey use it for storing `Injector` object ([InjectorLookup](guice/injector.md#access-injector) is actually a shortcut for shared state access).
Also, all main dropwizard objects are stored there for direct reference.

[SPA](../extras/spa.md) bundle use it to avoid colliding paths:

```java
public class SpaBundle implements GuiceyBundle {
    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        // state shared between all spa bundles
        // NOTE: not a static field for proper parallel tests support
        bootstrap.sharedState(SpaBundleState.class, SpaBundleState::new).checkUnique(assetName);
    }
}

public class SpaBundleState {
    private final List<String> usedAssetNames = new ArrayList<>();

    public void checkUnique(final String assetName) {
        checkArgument(!usedAssetNames.contains(assetName),
                "SPA with name '%s' is already registered", assetName);
        usedAssetNames.add(assetName);
    }
}
```


[GSP](../extras/gsp.md) bundles use it for bundles communication.
Core bundle register global configuration:

```java
public class ServerPagesBundle extends UniqueGuiceyBundle {
    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        loadRenderers();

        // register global config
        bootstrap
                .shareState(ServerPagesGlobalState.class, config)
    }
}
```

Application bundle reference this state:

```java
public class ServerPagesAppBundle implements GuiceyBundle {
    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        this.config = bootstrap.sharedStateOrFail(ServerPagesGlobalState.class,
                "Either server pages support bundle was not installed (use %s.builder() to create bundle) "
                        + " or it was installed after '%s' application bundle",
                ServerPagesBundle.class.getSimpleName(), app.name);
        // register application globally
        config.register(app);
    }
}
```

This way, duplicate registrations could be checked, and global views support could be configured by application bundles. 

## Shared state restrictions

Internally shared state is a `Map<String, Object>`, but state API force you to use Class as key for **type safety**:
it is assumed that **unique class** would be created for shared state (even if you just need to store a simple value)
and the stored object type would be a key.

Other restrictions:

* State value can be **set just once**! This is simply to avoid hard to track problems with overridden state.
* State value can't be null! Again, to avoid problems with NPE errors.

## Auto-closable values

If stored value implements `AutoClosable` - it would be closed
automcaticlly on application shutdown.

## State usage technics

### Parent-child

Value stored in one place (some parent bundle):

```java
bootstrap.shareState(SomeState.class, config);
```

And accessed in some other place (other child bundle or module):

```java
SomeState state = bootstrap
        .sharedStateOrFail(SomeState.class, "State not declared");
```

Here, error would be thrown if state is not initialized.

!!! note
    Guicey bundles registration works the same way as for dropwizard bundles:
    if you register some bundle from the core bundle, it would be initialized immediately.

    ```java
    public class MyBundle implements GuiceyBundle {
        public void initialize(GuiceyBootstrap bootstrap) throws Exception {
            // assume bundle initialize shared state value
            // (this bundle could be a unique bundle so guicey would remove duplicates)
            bootstarp.bundles(new ParentBundle());        
            // shared value could be used here
            SomeState state = bootstrap
                .sharedStateOrFail(SomeState.class, "State not declared");
        }
    }
    ```

### Indirect registration

In some cases, we might have a "race condition" if we're not sure when state value is initialized
(for example, two bundles could be declared in the different order).

For such cases, there is delayed access:

```java
bootstrap.whenSharedStateReady(SomeState.class, (state) -> ...)
```

!!! important
    Listener could be not called at all if target value would not be initialized.
    All not used listeners could be seen in [shared state report](diagnostic/shared-state-report.md)

### First wins

As shown in the SPA example above, shared state may be used by different instances
of the same bundle to perform global validations.

In this case "get or initialize" approach used:

```java
SomeState state = bootstrap.sharedState(SomeState.class, SomeState::new)
```

Here existing state would be requested (if already registered) or new one stored. 

## Utility

During startup shared state could be obtained with a static call:

```java
SharedConfigurationState.getStartupInstance() 
```

!!! note ""
    Static reference is possible only from the main application thread (which is always the case during initialization)

Shared state holds references to the main dropwizard objects, see methods:

- getBootstrap()
- getApplication()
- getEnvironment()
- getConfiguration()
- getConfigurationTree()
- getInjector()
- getOptions()

All of them return providers: e.g. `SharedConfigurationState.getStartupInstance().getBootsrap()`
would return `Provider<Bootstrap>`. This is required because target object
might not be available yet, still there would be a way to initialize some logic with "lazy object"
(to call it later, when object would be available) at any configuration stage.

## Main bundle

It is assumed that there should be no need to access shared state from [main bundle](configuration.md#main-bundle).
So [the only state-related method](configuration.md#hooks-related) actually assumed to be used by [hooks](hooks.md):
 
```java
static class XHook implements GuiceyConfigurationHook {
    @Override
    void configure(GuiceBundle.Builder builder) throws Exception {
        builder.withSharedState(state -> {
            state.put(XHook, new SharedObject());
        });
    }
}
```  

## Guicey bundle

Shared state is assumed to be used by bundles. Bundle provides special [shortcut methods](configuration.md#guicey-bundle) 
for accessing state. It is assumed that state is declared under initialization phase and
could be accessed under both phases (but not restricted, so state could be declared in run phase too).

For usage examples see [decomposition section](../decomposition.md#shared-state).

## Guice modules

Shared state is not intended to be used in guice modules, but it is possible. 
To simplify usage there are shortcuts available in [dropwizard aware module](guice/module-autowiring.md#shared-state) base class. 

## Static access

If required, shared state could be accessed statically everywhere:

```java
SharedConfigurationState.get(application)
```

!!! note ""
    Direct static access (`SharedConfigurationState.getStartupInstance()`) is available only during startup, at runtime you can reference state
    only with Environment or Application objects.

Or direct value access:

```java
SharedConfigurationState.lookup(application, XBundle.class)
```                    

And it is possible to use `Environment` instance for access:

```java
SharedConfigurationState.get(environment)
SharedConfigurationState.lookup(environment, XBundle.class)
```

Special shortcut methods may be used for "get or fail behaviour":

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
* [Options](options.md)
* Injector

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

!!! tip
    During startup these objects might be referenced as lazy objects with [shortcuts](#utility)
