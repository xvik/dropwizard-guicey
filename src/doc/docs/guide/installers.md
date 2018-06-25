# Installers

Installer is a core integration concept: every extension point has it's own installer. Installers used for both [auto scan](scan.md) and manual modes
(the only difference is in manual mode classes specified manually).
Installers itself are resolved using classpath scanning, so it's very easy to add custom installers (and possibly override default one by disabling it and registering alternative).

All default installers are registered by [CoreInstallersBundle](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/CoreInstallersBundle.java)

## How it works

When installer recognize class, it binds it into guice `binder.bind(foundClass)` (or bind by installer if it [support binding](#writing-custom-installer)).
But extensions annotated with `@LazyBinding` are not bound to guice context. This may be useful to [delay bean creation](lifecycle.md):
by default, guice production stage will instantiate all registered beans.

On run phase (after injector created) all found or manually provided extensions are installed by type or instantiated (`injector.getInstance(foundClass)`) and passed to installer 
to register extension within dropwizard (installation type is defined by installer).

Installers are [ordered](ordering.md#installers-order).

!!! warning "Each extension is installed by only one installer!"    
    If extension could be recognized by more then one installers, it will be installed only by first 
    matching installer (according to installers order). 

## Writing custom installer

Installer should implement [FeatureInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/FeatureInstaller.java)
interface. It will be automatically registered if auto scan is enabled. To register manually use `.installers()` bundle option.

Installer `matches` method implements feature detection logic. You can use `FeatureUtils` for type checks, because it's denies
abstract classes. Method is called for classes found during scan to detect installable features and for classes directly specified
with `.extensions()` bundle option to detect installer.

Three types of installation supported. Installer should implement one or more of these interfaces:

* `BindingInstaller` allows custom guice bindings. If installer doesn't implement this interface simple `bind(type)` will be called to register in guice.
* `TypeInstaller` used for registration based on type (no instance created during installation).
* `InstanceInstaller` used for instance registration. Instance created using `injector.getInstance(type)`.
* `JerseyInstaller` used for registration of bindings in HK2 context.

Note that extensions may use `@LazyBinding` annotation. In general case such extensions will not be registered in guice.
In case of `BindingInstaller`, special hint will be passed and installer should decide how to handle it (may throw exception as not supported).

`BindingInstaller` called in time of injector creation, whereas `TypeInstaller` and `InstanceInstaller` are called just after injector creation.
`JerseyInstaller` is called on jersey start.

!!! attention ""
    Installers are not guice beans! So injections can't be used inside them. 
    This is because installers also used during initialization phase and instantiated before injector creation.

Example installer:

```java
public class CustomInstaller implements FeatureInstaller<CustomFeature> {
    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, CustomFeature.class);
    }    
}
```

Finds all CustomFeature derived classes and register them in guice (implicit registration - all classes matched by installer are registered in injector). Note that no installer interfaces were used, 
because guice registration is enough.

Now suppose CustomFeature is a base class for our jersey extensions. Then installer will be:

```java
public class CustomInstaller implements FeatureInstaller<CustomFeature>, JerseyInstaller<CustomFeature> {
    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, CustomFeature.class);
    }
    
    @Override
    public void install(final AbstractBinder binder, final Class<CustomFeature> type) {
        JerseyBinding.bindComponent(binder, type, false, false);
    }
    
    @Override
    public void report() {
    }
}
```

!!! tip
    For jersey installers see `AbstractJerseyInstaller` base class, containing common utilities.

### Ordering

In order to support [ordering](ordering.md), installer must implement `Ordered` interface.
If installer doesn't implement it extensions will not be sorted, even if extensions has `@Order` annotations. 

As example, see [ManagedInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)

### Options

Installer could also use [guicey options](options.md): 
* it must implement `WithOptions` marker interface
* or extend form `InstallerOptionsSupport` base class (implemented boilerplate)

### Reporting

Installers `report()` method will be called after it finish installation of all found extensions. Report provides
user visibility of installed extensions. 

To simplify reporting use predefined [Reporter](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/util/Reporter.java) class. 
See example usage in [ManagedInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)

```
INFO  [2016-08-21 23:49:49,534] ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller: managed =

    (ru.vyarus.dropwizard.guice.support.feature.DummyManaged)
```

For complex cases, reporter may be extended to better handle installed extensions. As examples see 
[plugin installer reporter](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/plugin/PluginReporter.java)
and [provider installer reporter](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/provider/ProviderReporter.java)

```
INFO  [2016-08-21 23:49:49,535] ru.vyarus.dropwizard.guice.module.installer.feature.plugin.PluginInstaller: plugins =

    Set<PluginInterface>
        (ru.vyarus.dropwizard.guice.support.feature.DummyPlugin1)
        (ru.vyarus.dropwizard.guice.support.feature.DummyPlugin2)

    Map<DummyPluginKey, PluginInterface>
        ONE        (ru.vyarus.dropwizard.guice.support.feature.DummyNamedPlugin1)
        TWO        (ru.vyarus.dropwizard.guice.support.feature.DummyNamedPlugin2)
```

### Generics

Guicey brings [generics-resolver](https://github.com/xvik/generics-resolver) which you 
can use in installers implementation.

For example, to get extension interface parametrization:

```java
interface Extension<V> {}

class ListExtension implements Extension<List<String>> {}

GenericsResolver.resolve(ListExtension.class)
        .type(Extension.class)
        .genericType("V") == List<String> // (ParameterizedType) 
```

Guicey itself use it for:

* types resolution during configuration introspection (`ConfigTreeBuilder`)
* to introspect type hierarchy and recognize all jersey extensions (`JerseyProviderInstaller`)
* format type for console reporting (`ProviderReporter`) 
* bing jersey extensions to correct types (`JerseyBinding`) 
