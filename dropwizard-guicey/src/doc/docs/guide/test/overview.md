# Testing

Guicey provides test extensions for: 

* [JUnit 5](junit5/setup.md)
* [Spock 2](spock2.md) 
* [Framework-agnostic utilities](general/general.md)
 

!!! note "Spock 2"
    There are no special Spock 2 extensions: junit 5 extensions used directly (with a special library),
    so all junit 5 features are available for spock 2.

Deprecated (because they use
a deprecated [dropwizard rule](https://www.dropwizard.io/en/release-4.0.x/manual/testing.html#junit-4)):

* [Spock 1](spock.md) 
* [JUnit 4](junit4.md)

Almost all extensions implemented with [DropwizardTestSupport](https://www.dropwizard.io/en/latest/manual/testing.html#non-junit).

!!! tip
    [Test framework-agnostic utilities](general/general.md) could be also used with junit 5 or spock extensions in cases when
    assertions required after application shutdown or to test application startup errors.

## Test concepts 

Dropwziard [proposes atomic testing approach](https://www.dropwizard.io/en/stable/manual/testing.html) (separate testing of each element). 

With DI (guice) we have to move towards **integration testing** because:

1. It is now harder to mock classes "manually" (because of DI "black box")
2. We have a core (guice injector, without web services), starting much faster than 
complete application.
   
The following kinds of tests should be used:

1. Unit tests for atomic parts (usually, utility classes)
2. Business logic (core integration tests): lightweight application starts (without web) to test services
   (some services could be mocked or stubbed)
3. Lightweight REST tests: same as 2, but also some rest services simulated (same as 
   [dropwizard resource testing](https://www.dropwizard.io/en/stable/manual/testing.html#testing-resources)) 
4. Web integration tests: full application startup to test web endpoints (full workflow to check transport layer)
5. Custom command tests
6. Application startup fail test (done with command runner) to check self-validations

## Configuration hooks

Guicey provides a [hooks mechanism](../hooks.md) for modifying configuration of the existing application.

Hook receives builder instance used for `GuiceBundle` configuration and so with hook **you can
do everything that could be done is the main bundle** configuration: 

* Register new guice modules
* Register new bundles (dropwizard or guicey)
* Disable extensions/modules/bundles
* Activate guicey reports
* Override guice bindings
* Register some additional extensions (could be useful for validation or to replace existing extension with 
  a stub implementation)
* Change application options

Example hook:

```java
public class MyHook implements GuiceyConfigurationHook {
    
    public void configure(GuiceBundle.Builder builder) {
        builder
            .disableModules(FeatureXModule.class)
            .disable(inPackage("com.foo.feature"))
            .modulesOverride(new MockDaoModule())
            .option(Myoptions.DebugOption, true);
    }
}
```

!!! tip
    You can modify [options](../options.md) in hook and so could enable some custom
    debug/monitoring options specifically for test.

## Disables

Every extension, installed by guicey, **could be disabled**.
When you register extension manually:

```java
GuiceBundle.builder()
    .extensions(MyExceptionMapper.class)
    ...
```

(or extension detected from classpath scan or from guice binding), guicey controls its installation
and so could avoid registering it (disable).

!!! note
    Guicey **can't** disable extensions registered directly:  
    ```java
    environment.jersey().register(MyExceptionMapper.class)    
    ``` 

You can use hooks to disable all unnecessary features in test:

* [installers](../disables.md#disable-installers)
* [extensions](../disables.md#disable-extensions)
* [guice modules](../disables.md#disable-guice-modules)
* [guicey bundles](../disables.md#disable-bundles)
* [dropwizard bundles](../disables.md#disable-dropwizard-bundles)

This way, you can isolate (as much as possible) some feature for testing.

The most helpful should be bundles disable (if you use bundles for features grouping)
and guice modules.

Using [disable predicates](../disables.md#disable-by-predicate) multiple extensions
could be disabled at once (e.g., extensions from some package or only annotated extensions).
But pay attention that predicates applied for all types of extensions!

## Guice bindings override

It is a quite common requirement to replace some service with a stub or mock. 
That's where guice [overriding bindings](../guice/override.md) come into play (`Modules.override()`). 
With it, you don't need to modify existing guice modules to replace any
guice bean for tests (no matter how original binding was declared: it would override
anything, including providers and provider methods).

For example, we have some service binding declared as:
```java
public class MyModule extends AbstractModule {
    
    protected configure() {
        // assumed @Inject ServiceX used everywhere 
        bind(ServiceX.class).to(MyServiceX.class);        
    }
}
```

To replace it with `MyServiceXExt`, preparing overriding module:

```java
public class MyOverridingModule extends AbstractModule {
    
    protected configure() {
        bind(ServiceX.class).to(MyServiceXExt.class);        
    }
}
```  

It could be overridden with a hook:

```java
public class MyHook implements GuiceyConfigurationHook {
    public void configure(GuiceBundle.Builder builder) {
        builder
            // the main module is still registered in application:
            // .modules(new MyModule())        
            .modulesOverride(new MyOverridingModule());
    }
}
```

Now all service injections (`@Inject ServiceX`) would receive `MyServiceXExt` instead of `MyServiceX`

## Debug bundles

You can also use special guicey bundles, which modify application behavior.
Bundles could contain additional listeners or services to gather additional metrics during
tests or validate behavior.

For example, guicey tests use a custom bundle to enable restricted guice options like 
`disableCircularProxies`:

```java
public class GuiceRestrictedConfigBundle implements GuiceyBundle {

    @Override
    void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.modules(new GRestrictModule());
    }

    public static class GRestrictModule extends AbstractModule {
        @Override
        protected void configure() {
            binder().disableCircularProxies();
            binder().requireExactBindingAnnotations();
            binder().requireExplicitBindings();
        }
    }
}

// Here bundle is registered with a system property, but
// also could be registered with a hook
PropertyBundleLookup.enableBundles(GuiceRestrictedConfigBundle.class);
```

Bundles are less powerful than hooks, but in many cases it is enough, for example to:

* disable installers, extensions, guice modules
* register custom extensions, modules or bundles
* override guice bindings

You can also use the [lookup mechanism](../bundles.md#bundle-lookup) to load bundles in tests (like  
[system properties lookup](../bundles.md#system-property-lookup) used above). 

## Overriding overridden beans

Guicey provides [direct support for overriding guice bindings](../guice/override.md),
but this might be already used by application itself (e.g. to "hack" or "patch" some service 
in 3rd party module).

In this case, it would be impossible to override such (already overridden) services and you  
should use the provided custom [injector factory](../guice/injector.md#injector-factory):  

Register factory in guice bundle:

```java
GuiceBundle.builder()
    .injectorFactory(new BindingsOverrideInjectorFactory())
```


After that you can register overriding bindings (which will override even modules registered in `modulesOverride`)
with:

```java
BindingsOverrideInjectorFactory.override(new MyOverridingModule())
```

!!! important
    It is assumed that overriding modules registration and application initialization
    will be at the same thread (`ThreadLocal` used for holding registered modules to allow
    parallel tests usage). 

For example, suppose we have some service `CustomerService` and it's implementation `CustomerServiceImpl`, 
defined in some 3rd party module. For some reason, we need to override this binding in the application:

```java
public class OverridingModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CustomerService.class).to(CustomCustomerServiceImpl.class);
    }
}
```

If we need to override this binding in test (again):


```java
public class TestOverridingModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CustomerService.class).to(CustomServiceStub.class);
    }
}
```

Registration would look like this:

```java
GuiceBundle.builder()
    .injectorFactory(new BindingsOverrideInjectorFactory())
    .modules(new ThirdPatyModule())
    // override binding for application needs
    .modulesOverride(new OverridingModule())
    ...
    .build()

// register overriding somewhere in test
BindingsOverrideInjectorFactory.override(new TestOverridingModule())    
```

!!! tip
    [Configuration hook](#configuration-hooks) may be used for static call (as a good integration point)
    
After test startup, application will use customer service binding from `TestOverridingModule`.

## Test commands

Dropwizard commands could be tested with [commands test support](general/command.md)

For example:

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .run("simple", "-u", "user");

Assertions.assertTrue(result.isSuccessful());
```

There are no special junit 5 extensions for command tests because direct run method is 
already the best way.

!!! warning
    Commands support override `System.in/out/err` to collect all output (and control input)
    which makes such tests impossible to run in parallel.

## Test application startup fail

To verify application self-validation mechanisms (make sure application would fail with incomplete configuration, 
or whatever another reason) use [commands runner](general/startup.md).

For example: 

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .runApp();

Assertions.assertEquals();
```

!!! note "Why not run directly?"
    You can run command directly: `new App().run("simple", "-u", "user")`
    But, if application throws exception in *run* phase, `System.exit(1)` would be called:

    ```java
    public abstract class Application<T extends Configuration> {
        ...
        protected void onFatalError(Throwable t) {
            System.exit(1);
        }
    }
    ```   
    Commands runner runs commands directly so exit would not be called. 

## Configuration

In tests, you can either use a custom configuration file with config overrides
or create configuration instance manually.

For modifying configuration, there are two mechanisms:

* Configuration overrides: dropwizard mechanism works only with file-based configuration.
  Works through system properties and may not work in some cases (for example, for collection
  properties)
* Configuration modifiers: guicey mechanism allowing direct configuration modification before 
  application startup (works with file-based configuration and manually created configuration 
  instance)

## Web client

Guicey provides a [ClientSupport](general/client.md) instance which provides basic methods for calling
web endpoints. The most helpful part of it is that it will self-configure automatically,
according to the provided configuration, so you don't need to modify tests
when rest or admin mapping changes.
