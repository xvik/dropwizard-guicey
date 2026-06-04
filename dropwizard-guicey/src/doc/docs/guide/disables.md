# Disables

Guicey allows disabling (removing) any registered configuration items:

* [Extensions](#disable-extensions)
* [Installers](#disable-installers)
* [Guice modules](#disable-guice-modules)
* [Bundles](#disable-bundles)
* [Dropwizard bundles](#disable-dropwizard-bundles)

This is mostly useful in tests, where you can easily modify the application context
(replacing entire application parts with [hooks](hooks.md#tests)).

But it can also be used for workarounds: when a 3rd-party item contains a bug or does
not fit well, it can always be disabled and replaced by a different item. For example,
a bundle may register some other optional bundle that you don't need, and it can be
simply disabled to avoid installation.

!!! note
    It doesn't matter if an item was already registered or not (at the time of disabling). An item
    may not be registered at all.

Disables are available in the [main bundle](configuration.md#main-bundle) and in [Guicey bundles](configuration.md#guicey-bundle).

!!! warning
    Disable is performed by class, so disabling modules and bundles disables all instances of type.
    The only way to disable an exact instance is to use [disable by predicate](#disable-by-predicate).

## Disable extensions

```java
.disableExtensions(ExtensionOne.class, ExtensionTwo.class)
```

It doesn't matter if an extension was already registered or not (it may not be registered at all)
or what source was used (manual, classpath scan, or binding).

!!! note
    Extension disabling will work for extensions declared in Guice modules! In this
    case, Guicey will simply remove such a binding.

!!! tip
    Extension disabling may also be used when the classpath scanner detected a class you don't need to
    be installed and you can't use the `@InvisibleForScanner` annotation on it.

Generally, all configuration must appear under the initialization phase, but it is allowed to disable
extensions under the run phase inside a Guicey bundle in order to disable features by
configuration values (because it's almost never possible not to register based on configuration values,
so disables are the only way to switch off features based on configuration).

## Disable installers

```java
.disableInstallers(ManagedInstaller.class, ResourceInstaller.class)
```

An installer is the core Guicey concept because installers implement Dropwizard integration —
properly registering Guice beans in Dropwizard. It may appear that an existing installer does not
fit your needs or simply contains a bug. You can easily remove it and register a
replacement (probably a fixed version):

```java
GuiceBundle.builder()
    ...
    .disableInstallers(ResourceInstaller.class)
    .installers(CustomizedResourceInstaller.class)
```

!!! tip
    Custom installers are detected automatically by classpath scan (if enabled).

This could also be used to change installer order (declared with the `@Order` annotation on each installer).

## Disable guice modules

```java
.disableInstallers(ModleOne.class, ModuleTwo.class)
```

Disabling affects both normal (`.modules()`) and overriding (`.modulesOverride()`) modules.

!!! important
    Disabling affects transitive modules!

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
    Will remove `TransitiveModule` (actually, Guicey will remove all bindings of this module,
    but result is the same)

Module disabling can be used to prevent some additional module
installation by a 3rd-party bundle (or to override such a module).

## Disable bundles

Guicey bundles can be disabled only in the [main bundle](configuration.md#main-bundle), because
a bundle must be disabled *before* its execution and transitive bundles are registered during
execution (so a disable may appear too late)

```java
.disableBundles(MyBundle.class)
```

Could be used to disable some unneeded transitive bundle installed by
a 3rd-party bundle.

## Disable Dropwizard bundles

```java
.disableDropwizardBundles(MyBundle.class)
```

!!! warning
    Only bundles registered through the Guicey API can be disabled!

    ```java
    bootstrap.addBundle(new MyBundle())
    bootstrap.addBundle(GuiceBindle.builder()
                    .disableDropwizardBundles(MyBundle.class)
                    .build())
    ```

    Disable **will not work** because `MyBundle` is not registered through the Guicey API.

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
    bundles registered through the Guicey API).

## Disable by predicate

There is also a generic disable method using a predicate. With it you can disable
items (bundles, modules, installers, extensions) by package or by installation bundle
or some other custom condition (e.g. introduce your disabling annotation and handle it with a predicate).

!!! note
    This is the only way to disable an exact module or bundle instance (if you have multiple
    items of the same type).

```java
import static ru.vyarus.dropwizard.guice.module.context.Disables.*

.disable(inPackage("com.foo.feature", "com.foo.feature2"));
```

Disable all extensions located in a package (or subpackage). It could be an extension, bundle, installer, or Guice module.
If you use a package-by-feature approach, then you can easily switch off entire features in tests.

```java
import static ru.vyarus.dropwizard.guice.module.context.Disables.*

.disable(installer()
         .and(registeredBy(Application.class))
         .and(type(SomeInstallerType.class).negate());
```

Disable all installers directly registered in the main bundle except `SomeInstallerType`

```java
import static ru.vyarus.dropwizard.guice.module.context.Disables.*

.disable(type(MyExtension.class,
         MyInstaller.class,
         MyBundle.class,
         MyModule.class));
```

Simply disable items by type.

Disable extensions, installed by the exact installer:

```java
@EnableHook
static GuiceyConfigurationHook hook = builder ->
        builder.disable(installedBy(WebFilterInstaller.class));
```

The condition is a Java `Predicate`. Use `Predicate#and(Predicate)`, `Predicate#or(Predicate)`
and `Predicate#negate()` to compose complex conditions from simple ones.

There are disable shortcuts for exact item types (`Disables.module()`, `Disabled.extension()`, etc.) that now return a predicate
type to simplify chained usage:

```java
builder.disable(module().and(ModuleItemInfo mod -> ! mod.isOverriding()));
```

There are special shortcuts for web and jersey extensions (jersey extension is also a web extension):
`Disables.jerseyExtension()` and `Disables.webExtension()` (servlets, filters and jersey).

Most common predicates can be built with the `ru.vyarus.dropwizard.guice.module.context.Disables`
utility (examples above).

## Reporting

[Configuration diagnostic report](diagnostic/configuration-report.md) (`.printDiagnosticInfo()`)
shows all [disables and disabled](diagnostic/configuration-report.md#disables) items.

For example:

```text
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)

    ...

    ├── installer  -LifeCycleInstaller           (r.v.d.g.m.i.feature)         *DISABLED
```
