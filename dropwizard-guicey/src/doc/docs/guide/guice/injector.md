# Injector instance

## Restrictive options

Guicey is compatible with the following guice restrictive options:

```java
public class MyModule extends AbstractModule {
        @Override
        protected void configure() {
            binder().disableCircularProxies();
            binder().requireExactBindingAnnotations();
            binder().requireExplicitBindings();
        }
    }
```

## Access injector

In some cases it may be important to get injector instance outside of guice context.

!!! warning
    Injector is created on dropwizard run phase. Attempt to obtain injector before it
    will lead to exception.

Injector instance could be resolved with:

* `#!java GuiceBundle#getInjector()` method on instance (exception thrown if not yet started)
* `#!java InjectorLookup.getInjector(app).get()` static call using application instance (lookup returns `Optional` for null safety).

If you need lazy injector reference, you can use `InjectorProvider` class (`#!java Provider<Injector>`):

```java
Provider<Injector> provider = new InjectorProvider(app);
// somewhere after run phase
Injector injector = provider.get();
```

Bean instance may be obtained with `getInstance` shortcut:

```java
public class MyApplication extends Application<Configuration> {
    
    @Override
    public void run(TestConfiguration configuration, Environment environment) throws Exception {
        InjectorLookup.getInstance(this, SomeService.class).get().doSomething();
    }
}
```       

Injector could also be referenced by `Environment` object:

```java
InjectorLookup.getInstance(environment, SomeService.class).get().doSomething();
```

!!! tip
    Most likely, requirement for injector instance means integration with some third party library.
    Consider [writing custom installer](../installers.md#writing-custom-installer) in such cases (it will eliminate need for injector instance).
    
Inside guice context you can simply inject Injector instance:

```java
@Inject Injector injector;
```    

## Injector stage

By default injector is created at `PRODICTION` stage, which means that all registered
singletons are instantiated in time of injector craetion.

You can change stage at [main bundle](../configuration.md#injector):

```java
GuiceBundle.builder()
    ...
    .build(Stage.DEVELOPMENT)
```

## Injector factory
  
You can control guice injector creation through `ru.vyarus.dropwizard.guice.injector.InjectorFactory`. 

Default implementation is very simple:

```java
public class DefaultInjectorFactory implements InjectorFactory {

    @Override
    public Injector createInjector(final Stage stage, final Iterable<? extends Module> modules) {
        return Guice.createInjector(stage, modules);
    }
}
```

Injector creation customization may be required by some 3rd party library.
For example, [netflix governator](https://github.com/Netflix/governator) 
owns injector creation ([see example](../../examples/governator.md)).

Custom injector factory could be registered in guice bundle builder:

```java
bootstrap.addBundle(GuiceBundle.builder()
            .injectorFactory(new CustomInjectorFactory())
            ...
```