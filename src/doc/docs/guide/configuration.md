
# Configuration

Builder `GuiceBundle.builder()` contains shortcuts for all available features, so you can always find required function 
by looking at available methods and reading javadoc.

!!! note "Configuration subjects (vocabulary)"
    * Installers - used to recognize and install extension (usually encapsulates integration logic: get extension instance from guice injector and register in dropwizard (or jersey, HK2, whatever))
    * Extensions - actual application parts, written by you (resources, tasks, health checks, servlets etc)
    * Guice modules
    * Guicey bundles - groups installers, extensions, gucie modules and other guicey bundles (represent reusable logic or 
    3rd party integrations; very similar to dropwizard bundles)
    * Options - general mechanism for low level configurations (development time triggers)
    * Commands - dropwizard commands (mentioned because of ability for automatic registration)

!!! warning
    Configured bundles, modules, installers and extensions are checked for duplicates using *class type*. Duplicate configurations will be simply ignored.
    For modules and bundles, which configured using instances, duplicates removes means that if two instances of the same type registered, then second instance will
    be ignored. For example, in case of `.bundles(new MyBundle("one"), new MyBundle("two))`
    the second bundle will be ignored becuause bundle with the same type is already registered 
    (no matter that constructor parameters are different - only type matters).
 
Configuration process is recorded and may be observed with by [diagnostic info](diagnostic.md),
so there is always a way to understand what and how was configured.

!!! tip
    You can see used configuration objects by enabling [detailed lifecycle logs](events.md#debug):
    ```java
    bundle.printLifecyclePhasesDetailed()
    ```

## Auto configuration

Auto configuration enables [classpath scan](scan.md) to search for extensions and custom installers. 
Without auto scan, all extensions (resource, managed, health check etc.) must be registered [manually](#extensions) (manual mode).

```java
.enableAutoConfig("com.mycompany.app")
```

or multiple packages

```java
.enableAutoConfig("com.mycompany.app.resources", "com.mycompany.app.staff")
```

### Commands search

In auto configuration mode guicey could also [search and install](commands.md#automatic-installation) dropwiard commands (register in bootstrap object):

```java
.searchCommands()
```

By default commands scan is disabled because it may be confusing. Besides, it's not often needed.


## Extensions

All features installed with guicey installers are called extensions. When auto configuration is enabled, extensions are discovered
automatically. Without auto configuration (manual mode) all extensions must be specified manually.

```java
.extensions(MyResource1.class, MyHealthCheck.class)
```

Auto configuration may be used together with manual definition (for example, manually registered extension could be unreachable for
classpath scan).

!!! warning ""
    Each extension could be installed only by one installer: if multiple installers could recognize extension, 
    only one of them will install it (first one according to priority).

!!! tip
    Any extension could be disabled with `.disableExtension(Extension.class)` 
    (may be useful to disable not needed extension from 3rd party bundle)

### Disable extensions

You can disable extensions (even if it's not registered)

```java
.disableExtensions(ExtensionOne.class, ExtensionTwo.class)
```

!!! tip
    This may be used when classpath scanner detected class you don't need to be installed
    and you can't use `@InvisibleForScanner` annotation on it.

Mostly useful for tests.

## Installers

Guicey come with pre-defined set of installers (for common extensions). But you can [write your own installers](installers.md#writing-custom-installer)
(or use some 3rd party ones).

!!! note ""
    Most installers implementations are very simple and easy to understand. Look installer source to better understand
    how extensions work. In case when default installer does not fit your needs, it's not hard to replace installer 
    with your custom version.

```java
.installers(MyExtensionInstaller.class, ThirdPartyExtensionInstaller.class)
```

!!! tip
    In auto configuration mode, installers are also detected and installed automatically

### Disable default installers

You can disable all default installers:

```java
.noDefaultInstallers()
```

But note that in this case you must register at least one installer
(it could be one of core installers) because otherwise no extensions could be installed.

### Web installers

Guicey has [advanced installers](web.md#web-installers) for standard servlet annotations (`@WebServlet`, `@WebFilter`, `@WebListener`).
They are not enabled by default to avoid confusion: user may not expect guice support for these standard annotations.

To enable web installers:

```java
.useWebInstallers()
```

### Disable installers

You can disable installers (even if it's not registered)

```java
.disableInstallers(ManagedInstaller.class, ResourceInstaller.class)
```

Mostly useful for tests.

### Debug installers

Special debug option could [print to console all available installers](diagnostic.md#installers-mode) (with registration sources):

```java
.printAvailableInstallers()
```

Use to quickly understand available features.
 
## Guice modules

You can register one or more guice modules (including guice `ServletModule`s):

```java
.modules(new MyModule1(), new MyModule2())
```

If you have many modules try to group their installation inside your custom module in order
to keep guice staff together.

In some cases, it could be desired to use different instances of the same module:
```java
.modules(new ParametrizableModule("mod1"), new ParametrizableModule("mod2"))
```
This will not work (second instance will be dropped). In such cases do registrations in custom
guice module:
```java
install(new ParametrizableModule("mod1"));
install(new ParametrizableModule("mod2"));
```

### Disable guice modules

You can disable guice modules (even if it's not registered)

```java
.disableInstallers(ModleOne.class, ModuleTwo.class)
```

!!! important
    This will affect only modules directly registered in main bundle or guicey bundle.
    (modules installed inside guice module are not affected).
    
This is mostly useful for tests, but could be used to prevent some additional module 
installation by 3rd party bundle (or may be used to override such module).

!!! tip
    Note that you can also override some bindings (see below) instead of entire module override    

### Override guice bindings

Guice allows you to override any binding with `Modules.override()`. 
With it you can override any service in context. Guicey provides direct shortcut 
for using this feature. 

Mostly, this is handful for tests, but could be used to override some service, 
registered by 3rd party module (probably registered by some bundle).

Suppose we have 3rd party service with a bug, registered by 3rd party module:

```java
public class XModule extends AbstractModule {
    protected void configure() {
        bind(XService.class).asEagerSingleton();
        ...
    }
}
```

We can override it with fixed version:

```java
public class FixXServiceModule extends AbstractModule {
    protected void configure() {
        // module with only one binding overriding original definition
        bind(XService.class).to(FixedXService.class);        
    }
}

public class FixedXService extends XService {
    ...
} 
```

```java
bootstrap.addBundle(GuiceBundle.builder()
            .modules(new XModule())
            .modulesOverride(new FixXServiceModule())
            .build())
``` 

Now all guice injector will use your service (`FixedXService`) instead of `XService`.

## Guicey bundles

In essence, [guicey bundles](bundles.md) are the same as dropwizard bundles: used to install re-usable logic or 
3rd party library integration.

```java
.bundles(new MyBundle(), new ThirdPartyBundle());
```

### Dropwizard bundles unification

Guice bundles must implement interface (`GuiceyBundle`). Dropwizard bundle could implement it too. 
This may be useful for [universal bundles](bundles.md#dropwizard-bundles-unification) when all main features are 
activated by dropwizard bundle and guicey features are optional (if guicey present).

When:

```java
.configureFromDropwizardBundles()
```

guicey checks registered dropwizard bundles if they are also `GuiceyBundle` and register them as guicey bundles.

### Bundle lookup

[Bundle lookup](bundles.md#bundle-lookup) mechanism provides support for indirect guicey bundles installation.
Default lookup mechanism allows using service loader (plug-n-play bundles) or system property (test/diagnostic bundles). 

Custom implementation could be specified:

```java
.bundleLookup(new MyBundleLookupImpl())
```

Shortcut to disable default bundle lookup:

```java
.disableBundleLookup()
```

### Disable bundles

Guicey bundles could be disabled only in root bundle. Bundles can't disable other bundles.  

```java
.disableBundles(MyBundle.class)
```

This is mostly useful for tests, but could also be used to disable some not required transitive bundle, installed by
3rd party bundle.

## Options

[Options](options.md) are used for development time configurations (test specific triggers or low level configurations).
Guicey option enums: `GuiceyOptions` and `InstallersOptions`

```java
.option(GuiceyOptions.InjectorStage, Stage.DEVELOPMENT)
```

!!! tip
    Options look better with static import: `#!java .option(InjectorStage, DEVELOPMENT)`

[Options mapper](options.md#options-lookup) could be used to map option value 
from system properties, environment variables or simple strings (basic type conversions supported):

```java
.options(new OptionsMapper()
                .prop("myprop", Myoptions.SomeOption)
                .env("STAGE", GuiceyOptions.InjectorStage)
                .string("property value", Myoptions.SomeOtherOption)
                .map())                
```  

## Disable by predicate

There is also a generic disable method using predicate. With it you can disable
items (bundles, modules, installers, extensions) by package or by installation bundle
or some other custom condition (e.g. introduce your disabling annotation and handle it with predicate).

Supposed to be used in integration tests, but could be used directly too in specific cases.

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

## Guice

### Stage

Guice stage cold be provided in:

```java
.build(Stage.DEVELOPMENT)
```

By default, PRODUCTION stage used.

### Injector 

[Custom guice injector factory](injector.md#injector-factory) may be registered to customize injector creation 
(e.g. required for [governator](../examples/governator.md)):

```java
.injectorFactory(new GovernatorInjectorFactory())
```

### ServletModule

By default, guicey [registers](web.md#guice-servletmodule-support) `GuiceFilter` for both main and admin contexts to provide request scopes for both contexts and
ability to use guice `ServletModule`s on main context.

`GuiceFilter` is registered with REQUEST dispatcher type. If you need to use other types:

```java
.option(GuiceFilterRegistration, EnumSet.of(REQUEST, FORWARD))
```

If you [don't need servlet module support](web.md#disable-servletmodule-support) (and request scopes), guice filter installation could be disabled:

```java
.noGuiceFilter()
```

Servlet modules will be rejected in this case. Intended to be used when [web installers](web.md#web-installers) enabled.

!!! note 
    `HttpServletRequest` and `HttpServletResponse` objects will be available for injection only in scope of jersey resources call.

### Configuration binding

!!! warning "Deprecated"
    Option remain for compatibility and will be eventually removed.
    You can always bind configuration by implemented interface [using qualifier](bindings.md#configuration):
    ```java
    @Inject @Config HasRequiredConfig config; 
    ``` 
    Also, unique configuration sub objects are also [available for injection](bindings.md#unique-sub-configuration) 
    (much better option rather then marker interface):
    ```java
    @Inject @Config RequiredConfig config; 
    ``` 

It may be useful to bind configuration instance to interface. Suppose some 3rd party requires your configuration
to implement interface:

```java
public class MyConfiguration extends Configuration implements HasRequiredConfig {...}
```

If binding by interface is enabled:

```java
.bindConfigurationInterfaces()
```

Then configuration could be injected by interface:

```java
@Inject HasRequiredConfig conf;
```

### HK2 bridge

If you need HK2 services be able to use guice beans, then [HK2 bridge](https://hk2.java.net/2.4.0-b34/guice-bridge.html) 
must be activated. 
This may be useful when some services are managed by HK2 (e.g. with [@HK2Managed](lifecycle.md#problematic-cases)).

To activate bridge:

* Add dependency: `org.glassfish.hk2:guice-bridge:2.5.0-b32` (version must match HK2 version, used by dropwizard)
* Enable option: `#!java .option(GuiceyOptions.UseHkBridge, true)`

After that, HK2 beans could inject guice beans:

```java
@HK2Managed
public class HkService {
    
    @Inject
    private GuiceService service;
}
```

### Use HK2 for jersey extensions 

By default, guice is used to construct all extensions, including jersey related (resources, providers)
which are registered in HK2 context as instances.

If you want to use HK2 for jersey extensions management then use:

```java
.useHK2ForJerseyExtensions()
```

(It is the same effect as if you will annotate all jersey extensions with `@HK2Managed`)

After enabling, all jersey extensions will be created by HK2. 
Option requires HK2-guice bridge (error will be thrown if bridge is not available in classpath)
to use guice services inside HK2 managed beans.

!!! warning
    Guice AOP will work only for instances created by guice, so after enabling this option you will not
    be able to use aop on jersey extensions.

By analogy with `@HK2Managed`, you can use `@GuiceManaged` to mark exceptional extensions,
which must be still managed by guice.

## Lifecycle events

Guicey broadcast events on all important configuration phases. These events contain
references to all available environment objects and current context configuration.
For example, after event with all resolved extension or event with all processed bundles.

Events are used to print lifecycle phases report (see below), but you may use it 
to modify installers, bundles, extensions (post process instances, but not affect quantity).  

Events are registered with:

```java
.listen(new MyEventListener())
```

Read more in [events documentation](events.md).

## Diagnostic

Startup errors could be debugged with lifecycle logs:

```java
.printLifecyclePhasesDetailed()
```

!!! tip
    Especially helpful when classpath scanner accept classes you don't need because
    it will prints all resolved extension before injector creation.

!!! tip
    Report shows disabled items. Use [diagnostic logs](diagnostic.md) to find the disabler.  

!!! note
    `.printLifecyclePhases()` could be used to just indicate phases in logs without additional details
    (useful when need to understand initialization order)

If you have problems with [configuration bindings](bindings.md#configuration-tree) (or just need to see available bindings) use:

```java
.printConfigurationBindings()
```

!!! note
    Bindings report is printed before injector creation (in case if startup fails due to missed binding) 

Enable configuration [diagnostic console logs](diagnostic.md) to diagnose configuration problems:

```java
.printDiagnosticInfo()
```


In case of doubts about extension owner (guice or HK2) and suspicious for duplicate instantiation,
you can enable [strict control](bundles.md#hk2-debug-bundle) which will throw exception in case of wrong owner:

```java
.strictScopeControl()
```

!!! note
    When you have duplicate initialization (most likely for jersey related extensions)
    first check that you are not register extension manually! 
    Using constructor injection helps preventing such errors (manual places will immediately reveal).
    
## Guicey configuration hooks  

There is an external configuration mechanism. It could be used to modify 
application configuration externally without application modification:

```java
public interface GuiceyConfigurationHook {
    void configure(GuiceBundle.Builder builder);    
}
```

Hook implementation will receive the same builder instance as used in `GuiceBundle` 
and so it is able to change anything (for example, `GuiceyBundle` abilities are limited).

If hook is a class then it could be registered directly:

```java
new MyHook().register()
```

Otherwise lambda may be used:

```java
ConfigurationHooksSupport.register(builder -> { 
    // do modifications 
})
```

All hooks are executed just before guice bundle builder finalization (when you call last `.build()` method).
Hooks registered after this moment will simply be never used.

!!! note
    This functionality is intended to be used for integration tests and there is
    a [special test support](test.md) for it.  
    
In hook you can do all the same as in main application configuration. In context of tests,
 the most important is:
* Change options
* Disable any bundle, installer, extension, module
* Register disable predicate (to disable features by package, registration source etc.)
* Override guice bindings
* Register additional bundles, extensions, modules (usually test-specific, for example guicey tests register 
additional guice module with restricted guice options (disableCircularProxies, requireExplicitBindings, requireExactBindingAnnotations))
       