# Testing

Guicey provides test extensions for: 

* [Spock 2](spock2.md)
* [JUnit 5](junit5.md)
* [Framework-agnostic utilities](general.md)

Deprecated:

* [Spock 1](spock.md) 
* [JUnit 4](junit4.md)

All extensions implemented with [DropwizardTestSupport](https://www.dropwizard.io/en/latest/manual/testing.html#non-junit).

!!! note
    There is no special Spock 2 extensions - junit 5 extensions would be used directly so you get the best of both worlds - 
    use junit extensions (and so can always easily migrate to pure junit) and have spock (and groovy) expressiveness.

Additionally, guicey provides several mechanisms at its core for application customization in tests (see below).

## Configuration hooks

Guicey provides [hooks mechanism](../hooks.md) to be able to modify
application configuration in tests.

Using hooks you can disable installers, extensions, guicey bundles  
or override guice bindings.

It may also be useful to register additional extensions (e.g. to validate some internal behaviour).

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

!!! note
    You can modify [options](../options.md) in hook and so could enable some custom
    debug/monitoring options specifically for test.

There are special [spock](spock.md#hook-fields) and [junit](junit5.md#hook-fields) extensions for hooks registrations.

## Disables

You can use hooks to disable all not needed features in test:

* [installers](../disables.md#disable-installers) 
* [extensions](../disables.md#disable-extensions) 
* [guice modules](../disables.md#disable-guice-modules)
* [guicey bundles](../disables.md#disable-bundles)
* [dropwizard bundles](../disables.md#disable-dropwizard-bundles) 

This way you can isolate (as much as possible) some feature for testing. 

The most helpful should be bundles disable (if you use bundles for features grouping)
and guice modules.

Use [predicate disabling](../disables.md#disable-by-predicate).

!!! note
    It is supposed that disabling will be used instead of mocking - you simply remove what
    you don't need and register replacements, if required.

## Guice bindings override

It is quite common requirement to override bindings for testing. For example, 
you may want to mock database access.

Guicey could use guice `Modules.override()` to help you override required bindings.
To use it prepare module only with changed bindings (bindings that must override existing).
For example, you want to replace ServiceX. You have few options:

* If it implements interface, implement your own service and bind as 
`bind(ServiceContract.class).to(MyServiceXImpl.class)`
* If service is a class, you can modify its behaviour with extended class
`bind(ServiceX.class).to(MyServiceXExt.class)`
* Or you can simply register some mock instance
`bind(ServiceX.class).toInstance(myMockInstance)`

```java
public class MyOverridingModule extends AbstractModule {
    
    protected configure() {
        bind(ServiceX.class).to(MyServiceXExt.class);        
    }
}
```  

And register overriding module in hook:

```java
public class MyHook implements GuiceyConfigurationHook {
    public void configure(GuiceBundle.Builder builder) {
        builder
            .modulesOverride(new MyOverridingModule());
    }
}
```

### Debug bundles

You can also use special guicey bundles, which modify application behaviour.
Bundles could contain additional listeners or services to gather additional metrics during
tests or validate behaviour.

For example, guicey tests use bundle to enable restricted guice options like 
`disableCircularProxies`.

Bundles are also able to:

* disable installers, extensions, gucie modules
* override guice bindings

You can also use lookup mechanism to load bundles in tests. For example, 
[system properties lookup](../bundles.md#system-property-lookup). 

## Overriding overridden beans

Guicey provides [direct support for overriding guice bindings](../guice/override.md),
so in most cases you don't need to do anything.

But, if you use this to override application bindings need to override such bindings in test (again), then you
 may use provided custom [injector factory](../guice/injector.md#injector-factory):  

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
    It is assumed that overrding modules registration and application initialization
    will be at the same thread (`ThreadLocal` used for holding registered modules to allow
    parallel tests usage). 

For example, suppose we have some service `CustomerService` and it's implementation `CustomerServiceImpl`, 
defined in some 3rd party module. For some reason we need to override this binding in the application:

```java
public class OverridingModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CustomerService.class).to(CustomCustomerServiceImpl.class);
    }
}
```


If we need to override this binding in test (again):

(Simplified) registration looks like this:

```java
GuiceBundle.builder()
    .injectorFactory(new BindingsOverrideInjectorFactory())
    .modules(new ThirdPatyModule())
    // override binding for application needs
    .modulesOverride(new OverridingModule())
    ...
    .build()

// register overriding somewhere
BindingsOverrideInjectorFactory.override(new TestOverridingModule())    
```

!!! tip
    [Configuration hook](#configuration-hooks) may be used for static call (as a good integration point)
    
After test startup, application will use customer service binding from TestOverridingModule.
