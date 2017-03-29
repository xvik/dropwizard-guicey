
# Configuration

Builder `GuiceBundle.builder()` contains shortcuts for all available features, so you can always find required function 
by looking at available methods and reading javadoc.

!!! note "Configuration subjects (vocabulary)"
    * Installers - used to recognize and install extension (usually encapsulates integration logic: get extension instance from guice injector and register in dropwizard (or jersey, hk, whatever))
    * Extensions - actual application parts, written by you (resources, tasks, health checks, servlets etc)
    * Guice modules
    * Guicey bundles - groups installers, extensions, gucie modules and other guicey bundles (represent reusable logic or 
    3rd party integrations; very similar to dropwizard bundles)
    * Options - general mechanism for low level configurations (development time triggers)
    * Commands - dropwizard commands (mentioned because of ability for automatic registration)

!!! warning
    Configured bundles, modules, installers and extensions are checked for duplicates using class type. Duplicate configurations will be simply ignored.
    For modules and bundles, which configured using instances, duplicates removes means that if two instances of the same type registered, then second instance will
    be ignored.
 
Configuration process is recorded and may be observed with by [diagnostic info](diagnostic.md),
so there is always a way to understand what and how was configured.

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

By default commands scan is disabled because it may confusing. Besides, it's not often needed.


## Extensions

All features installed with guicey installers are called extensions. When auto configuration is enabled, extensions are discovered
automatically. Without auto configuratin (manual mode) all extensions must be specified manually.

```java
.extensions(MyResource1.class, MyHealthCheck.class)
```

Auto configuration may be used together with manual definition (for example, manually registered extension could be unreachable for
classpath scan).

!!! warning ""
    Each extension could be installed only by one installer: if multiple installers could recognize extension, 
    only one of them will install it (first one according to priority).

## Installers

Guicey come with pre-defined set of installers (for common extensions). But you can write your own installers
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

### Default installers

Guicey come with a set of pre defined installers, enabled by default (for common extensions installation like resource, health check, task, managed etc.).
You can disable them with:

```java
.noDefaultInstallers()
```

### Web installers

Guicey has [advanced installers](web.md#web-installers) for standard servlet annotations (`@WebServlet`, `@WebFilter`, `@WebListener`).
They are not enabled by default to avoid confusion: user may not expect guice support for these standard annotations.

To enable web installers:

```java
.useWebInstallers()
```

### Disable installer

You can disable installers (even if it's not registered)

```java
.disableInstallers(ManagedInstaller.class, ResourceInstaller.class)
```

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

## Guicey bundles

In essence, [guicey bundles](bundles.md) are the same as dropwizard bundles: used to install re-usable logic or 
3rd party library integration.

```java
.bundles(new MyBundle(), new ThirdPartyBundle());
```

### Dropwizard bundles unification

Guice bundles must implement interface (`GuiceyBundle`). Dropwizard bundle could inmplement it too. 
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

## Options

[Options](options.md) are used for development time configurations (test specific triggers or low level configurations).
Guicey option enums: `GuiceyOptions` and `InstallersOptions`

```java
.option(GuiceyOptions.InjectorStage, Stage.DEVELOPMENT)
```

!!! tip
    Options look better with static import: `#!java .option(InjectorStage, DEVELOPMENT)`

To provide multiple options at once (batch options load):

```java
.options(new MyOptionsLookup().getOptions())
```

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

It may be useful to bind configuration instance to interface. Suppose some 3rd party requires your configuration
to implement interface:

```java
public class MyConfiguraton extends Configuration implements HasRequiredConfig {...}
```

If binding by interface is enabled:

```java
.bindConfigurationInterfaces()
```

Then configuration could be injected by interface:

```java
@Inject HasRequiredConfig conf;
```

!!! note
    By default, configuration is bound only for [all classes in hierarchy](bindings.md#configuration). In example above it would be 
    `Configuration` and `MyConfiguration`.

### HK bridge

If you need HK services be able to use guice beans, then [hk bridge](https://hk2.java.net/2.4.0-b34/guice-bridge.html) 
must be activated. 
This may be useful when some services are managed by HK (e.g. with [@HK2Managed](lifecycle.md#problematic-cases)).

To activate bridge:

* Add dependency: `org.glassfish.hk2:guice-bridge:2.5.0-b32` (version must match HK version, used by dropwizard)
* Enable option: `#!java .option(GuiceyOptions.UseHkBridge, true)`

After that, HK beans could inject guice beans:

```java
@HK2Managed
public class HkService {
    
    @Inject
    private GuiceService service;
}
```

## Diagnostic

Enable configuration [diagnostic console logs](diagnostic.md) to diagnose configuration problems:

```java
.printDiagnosticInfo()
```

In case of doubts about extension owner (guice or HK) and suspicious for duplicate instantiation,
you can enable [strict control](bundles.md#hk-debug-bundle) which will throw exception in case of wrong owner:

```java
.strictScopeControl()
```
