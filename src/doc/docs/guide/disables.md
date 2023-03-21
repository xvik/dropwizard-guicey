# Disables

Guicey allows disabling (removing) of any registered configuration items:

* [Extensions](#disable-extensions)
* [Installers](#disable-installers)
* [Guice modules](#disable-guice-modules)
* [Bundles](#disable-bundles)
* [Dropwizard bundles](#disable-dropwizard-bundles)

This is mostly useful in tests, where you can easily modify application context
(replacing entire application parts with [hooks](hooks.md#tests)).

But it also could be used for workarounds: when 3rd party item contains bug or does 
not fit well - it could be always disabled and replaced by different item. For example, 
bundle may register some other optional bundle, which you doesn't need - it could be
simply disabled to avoid installation. 

!!! note
    It doesn't matter if item was already registered or not (in time of disabling). Item 
    may not be registered at all.

Disables are available in [main bundle](configuration.md#main-bundle) and in [guicey bundles](configuration.md#guicey-bundle).

!!! warning
    Disable is performed by class, so disabling modules and bundles disables all instances of type.
    The only way to disable exact instance is to use [disable by predicate](#disable-by-predicate). 

## Disable extensions

```java
.disableExtensions(ExtensionOne.class, ExtensionTwo.class)
```

It doesn't matter if extension was already registered or not (it may be not registered at all)
or what source used (manual, classpath scan or binding).

!!! note
    Extension disable will work for extensions, declared in guice modules! In this
    case guicey will simply remove such binding.

!!! tip
    Extension disable may be also used when classpath scanner detected class you don't need to 
    be installed and you can't use `@InvisibleForScanner` annotation on it.

Generally, all configuration must appear under initialization phase, but it is allowed to disable 
extensions under run phase inside guicey bundle to be able to disable features by 
configuration values (because it's almost never possible to not register, based on configuration values,
so disables is the only way to switch off features, based on configuration).  

## Disable installers

```java
.disableInstallers(ManagedInstaller.class, ResourceInstaller.class)
```

Installer is the core guicey concept because installers implement dropwizard integration -
properly register guice beans in dropwizard. It may appear that existing installer does not
fit your needs or simply contains bug. You can easily remove it and register
replacement (probably fixed version):

```java
GuiceBundle.builder()
    ...
    .disableInstallers(ResourceInstaller.class)
    .installers(CustomizedResourceInstaller.class)
```              

!!! tip
    Custom installers are detected automatically by classpath scan (if enabled).

This could also be used to change installers order (declared with `@Order` annotation on each installer).  

## Disable guice modules

```java
.disableInstallers(ModleOne.class, ModuleTwo.class)
```

Disabling affects both normal (`.modules()`) and overriding (`.modulesOverride()`) modules.

!!! important
    Disabling affect transitive modules!
    
    ```java        
    public class MyModule extends AbstractModule {
        @Override
        public void configure() {   
            install(new TransitiveModule());
        }
    }                           
    
    GuiceBindle.builder()
        .disableModules(TrannsitiveModule.clas)
        ...
    ```
    Will remove `TransitiveModule` (actually, guicey will remove all bindings of this module,
    but result is the same)
    
Modules disable could be used to prevent some additional module 
installation by 3rd party bundle (or to override such module).    

## Disable bundles

Guicey bundles could be disabled only in [main bundle](configuration.md#main-bundle), because 
bundle must be disabled *before* it's execution and transitive bundles are registered during 
execution (so disable may appear too late)  

```java
.disableBundles(MyBundle.class)
```

Could be used to disable some not required transitive bundle, installed by
3rd party bundle.

## Disable dropwizard bundles

```java
.disableDropwizardBundles(MyBundle.class)
```                                      

!!! warning
    Only bundles registered through guicey api could be disabled!
    
    ```java
    bootstrap.addBundle(new MyBundle())
    bootstrap.addBundle(GuiceBindle.builder()
                    .disableDropwizardBundles(MyBundle.class)
                    .build())
    ```
    
    Disable **wIll not work** becuase `MyBundle` is not registered through guicey api.

!!! important
    Disable affects transitive bundles!
    
    ```java   
    public class MyBundle implements ConfiguredBundle {
        @Override
        public void initialize(Bootstrap bootstrap) {   
            bootstrap.addBundle(new TransitiveBundle());
        }
    }                           
    
    GuiceBindle.builder()
        .disableDropwizardBundles(TransitiveBundle.class)
        ...
    ```   
    
    Will remove `TransitiveBundle` (this works due to bootsrap object proxying for 
    bundles registered through guicey api). 

## Disable by predicate

There is also a generic disable method using predicate. With it you can disable
items (bundles, modules, installers, extensions) by package or by installation bundle
or some other custom condition (e.g. introduce your disabling annotation and handle it with predicate).

!!! note
    This is the only way to register exact module or bundle instance (if you have multiple
    items of the same type).

```java
import static ru.vyarus.dropwizard.guice.module.context.Disables.*

.disable(inPackage("com.foo.feature", "com.foo.feature2"));
```

Disable all extensions lying in package (or subpackage). It could be extension, bundle, installer, guice module.
If you use package by feature approach then you can easily switch off entire features in tests.

```java
import static ru.vyarus.dropwizard.guice.module.context.Disables.*

.disable(installer()
         .and(registeredBy(Application.class))
         .and(type(SomeInstallerType.class).negate());
```

Disable all installers, directly registered in main bundle except `SomeInstallerType`

```java
import static ru.vyarus.dropwizard.guice.module.context.Disables.*

.disable(type(MyExtension.class,
         MyInstaller.class,
         MyBundle.class,
         MyModule.class));
```

Simply disable items by type.

The condition is java `Predicate`. Use `Predicate#and(Predicate)`, `Predicate#or(Predicate)`
and `Predicate#negate()` to compose complex conditions from simple ones.

Most common predicates could be build with `ru.vyarus.dropwizard.guice.module.context.Disables`
utility (examples above).

## Reporting

[Configuration diagnostic report](diagnostic/configuration-report.md) (`.printDiagnosticInfo()`) 
shows all [disables and disabled](diagnostic/configuration-report.md#disables) items. 

For example:

```
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)  

    ...

    ├── installer  -LifeCycleInstaller           (r.v.d.g.m.i.feature)         *DISABLED  
```
