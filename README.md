#Dropwizard guice integration
[![License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)
[![Build Status](http://img.shields.io/travis/xvik/dropwizard-guicey.svg?style=flat&branch=master)](https://travis-ci.org/xvik/dropwizard-guicey)
[![Coverage Status](https://img.shields.io/coveralls/xvik/dropwizard-guicey.svg?style=flat)](https://coveralls.io/r/xvik/dropwizard-guicey?branch=master)

### About

[Dropwizard](http://dropwizard.io/) integration based on ideas from [dropwizard-guice](https://github.com/HubSpot/dropwizard-guice) and 
[dropwizardy-guice](https://github.com/jclawson/dropwizardry/tree/master/dropwizardry-guice) 
(which was derived from first one).

Features:
* Guice injector created on run phase (now both dropwizard-guice and dropwizardry-guice do the same)
* Jersey integration through [jersey-guice](https://jersey.java.net/documentation/1.18/chapter_deps.html#d4e1876)
* No base classes for application or guice module (only bundle registration required)
* Configurable installers mechanism: each supported feature (task install, health check install, etc) has it's own installer and may be disabled
* Custom feature installers could be added
* Optional classpath scan to search features: resources, tasks, commands, health checks etc (without dependency on reflections library)
* Injections works in commands (environment commands)
* Support injection of Bootstrap, Environment and Configuration objects into guice modules before injector creation 
* Guice ServletModule can be used to bind servlets and filters (for main context)
* Servlets and filters could be installed into admin context (using annotations)
* Extensions ordering supported (for some extension types, where it might be useful)
* Dropwizard style reporting of installed extensions

### Setup

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) (package appear immediately after release) 
and then to maven central (require few days after release to be published). 

[![Download](https://api.bintray.com/packages/vyarus/xvik/dropwizard-guicey/images/download.svg) ](https://bintray.com/vyarus/xvik/dropwizard-guicey/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/dropwizard-guicey/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/dropwizard-guicey)

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>dropwizard-guicey</artifactId>
  <version>1.0.0</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus:dropwizard-guicey:1.0.0'
```

### Usage

You can use classpath scanning or configure everything manually (or combine both).
Auto scan configuration example:

```java
@Override
public void initialize(Bootstrap<TestConfiguration> bootstrap) {
    bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
            .enableAutoConfig("package.to.scan")
            .searchCommands(true)            
            .build()
    );
}
```

Auto scan will resolve installers and using installers find features in classpath and install them. 
Commands will also be searched in classpath, instantiated and set into bootstrap object.

Manual configuration example:

```java
@Override
void initialize(Bootstrap<TestConfiguration> bootstrap) {
    bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
            .installers(ResourceInstaller.class, TaskInstaller.class, ManagedInstaller.class)
            .extensions(MyTask.class, MyResource.class, MyManaged.class)
            .modules(new MyModule())
            .build()
    );
    bootstrap.addCommand(new MyCommand())
}
```

Installers defined manually. They will be used to detect provided bean classes and properly install them.

Look [tests](https://github.com/xvik/dropwizard-guicey/tree/master/src/test/groovy/ru/vyarus/dropwizard/guice/support) 
for configuration examples.

After application start look application log for dropwizard style extension installation reports.

Bundle options:
* `enableAutoConfig` enables auto scan on one or more packages to scan. If not set - no auto scan will be performed and default installers will not be available.
* `searchCommands` if true, command classes will be searched in classpath and registered in bootstrap object. 
Auto scan must be enabled. By default commands scan is disabled (false), because it may be not obvious.
* `modules` one or more guice modules to start. Not required: context could start even without custom modules.
* `disableInstallers` disables installers, found with auto scan. May be used to override default installer or disable it.
Note: when auto scan not enabled no installers will be registered automatically.
* `installers` registers feature installers. Used either to add installers from packages not visible by auto scan or to
configure installers when auto scan not used.
* `extensions` manually register classes (for example, when auto scan disabled). Classes will be installed using configured installers.
* `build` allows specifying guice injector stage (production, development). By default, PRODUCTION stage used.

### Classpath scan

Classpath scanning is activated by specifying packages to scan in bundle `.enableAutoConfig("package.to.scan")`.

When auto scan enabled:
* Feature installers searched in classpath (including default installers): classes implementing `FeatureInstaller`.
Without auto scan default installers not registered.
* Search for features in classpath using `FeatureInstaller#matches` method.
* If commands search enabled `.searchCommands(true)`, performs search for all classes extending `Command` and install them into
bootstrap.

`@InvisibleForScanner` annotation hides class from scanner (for example, to install it manually or to avoid installation at all)

### Module autowiring

Because guice modules are registered in init section, it's not possible to get reference for environment and configuration objects.
To overcome this limitation, you can implement `BootstrapAwareModule`, `EnvironmentAwareModule` or `ConfigurationAwareModule`
interfaces and reference object will be set to module just before injector creation (allowing you to use it during module configuration).

This will work only for modules set to `modules()` bundle option.

[Example](https://github.com/xvik/dropwizard-guicey/blob/master/src/test/groovy/ru/vyarus/dropwizard/guice/support/AutowiredModule.groovy)

### Extension ordering

Some installers support extensions ordering (managed, lifecycle and admin servlet and filters).
To define extensions order use `@Order` annotation. Extensions sorted naturally (e.g. `@Order(1)` before `@Order(2)`).
Extensions without annotation goes last.

### Installers

Installer is a core integration concept: every extension point has it's own installer. Installers used for both auto scan and manual modes
(the only difference is in manual mode classes specified manually).
Installers itself are resolved using classpath scanning, so it's very easy to add custom installers (and possibly override default one by disabling it and registering alternative).

All default installers could be found [here](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature)

When installer recognize class, it binds it into guice `binder.bind(foundClass)` (or bind by installer if it support binding).
On run phase (after injector created) all found or manually provided extensions are installed by type or instantiated (`injector.getInstance(foundClass)`) and passed to installer 
to register extension within dropwizard (installation type is defined by installer).

[ResourceInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ResourceInstaller.java) 
finds classes annotated with `@Path` and register their instance as resources. Resources registered as singletons, even if guice bean scope isn't set. If you need prototype
resources, install them manually or write custom installer.

[TaskInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/TaskInstaller.java)
finds classes extending `Task` class and register their instance in environment.

[ManagedInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)
finds classes implementing `Managed` and register their instance in environment. Support ordering.

[LifeCycleInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/LifeCycleInstaller.java)
finds classes implementing jetty `LifeCycle` interface and register their instance in environment. Support ordering.

[HealthCheckInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/health/HealthCheckInstaller.java)
finds classes extending `NamedHealthCheck` class and register their instance in environment.

Custom base class is required, because default `HealthCheck` did not provide check name, which is required for registration.

[JerseyProviderInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/provider/JerseyProviderInstaller.java)
finds classes annotated with jersey `@Provider` annotation and register their instance in environment (forced singleton). Suitable for all type of extensions, 
like [InjectableProvider](https://github.com/xvik/dropwizard-guicey/tree/master/src/test/groovy/ru/vyarus/dropwizard/guice/support/provider/LocaleInjectableProvider.java), 
[ExceptionMapper](https://github.com/xvik/dropwizard-guicey/tree/master/src/test/groovy/ru/vyarus/dropwizard/guice/support/feature/DummyExceptionMapper.java) etc 
(everything you would normally pass into `environment.jersey().register()`.

[EagerSingletonInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/eager/EagerSingletonInstaller.java)
finds classes annotated with `@EagerSingleton` annotation and register them in guice injector. It is equivalent of eager singleton registration
`bind(type).asEagerSingleton()`.

This installer doesn't relate to dropwizard directly, but useful in case when you have bean not injected by other beans (so guice can't register
it automatically). Normally you would have to manually register it in module.

Most likely such bean will contain initialization logic. 

May be used in conjunction with @PostConstruct annotations (e.g. using [ext-annotations](https://github.com/xvik/guice-ext-annotations)):
installer finds and register bean and post construct annotation could run some logic. Note: this approach is against guice philosophy and should
be used for quick prototyping only.

[PluginInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/plugin/PluginInstaller.java)
used to simplify work with guice [multibindings](https://github.com/google/guice/wiki/Multibindings) mechanism: when you have different implementations of
some interface and want to automatically callect these implementations as set or map.

Suppose you have plugin interface `public interface PluginInterface`.
Annotating plugin implementations with `@Plugin`

```java
@Plugin(PluginInterface.class)
public class PluginImpl1 implements PluginInterface
```

Now all implementations could be autowired as

```java
@Inject Set<PluginInterface> plugins;
```

Also named mapping could be used. In this case most likely you would like to use enum for keys:

```java
public enum PluginKey {
    FIRST, SECOND
}
```

To use enum keys new annotation needs to be defined (it's impossible to use enum in annotation without explicit declaration, so no universal annotation could be made)

```java
@Plugin(PluginInterface.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyPlugin {
    PluginKey value();
}
```

Note that annotation itself is annotated with `@Plugin`, defining target plugin interface.

Now annotating plugin:

```java
@MyPlugin(PluginKey.FIRST)
public class PluginImpl1 implements PluginInterface
```

And all plugins could be referenced as map:

```java
@Inject Map<PluginKey, PluginInterface> plugins;
```

[AdminFilterInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/admin/AdminFilterInstaller.java)
installs filters annotated with `@AdminFilter` into administration context. Support ordering.

[AdminServletInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/admin/AdminServletInstaller.java)
installs servlets annotated with `@AdminServlet` into administration context. Support ordering.

NOTE: guice is too tied to `GuiceFilter`, which is registered on main context and can't be registered for admin context too. 
So you will not be able to use request scoped beans for filters and servlets (and can't inject request response objects) in admin scope 
(`ServletModule` can't be used for registration in admin context).

### Servlets and filters

To register servlets and filters for main context use `ServletModule`, e.g.

```java
public class WebModule extends ServletModule {

    @Override
    protected void configureServlets() {
        filter('/*').through(MyFilter.class)
        serve('/myservlet').with(MyServlet.class)
    }
}
```

### Commands support

Automatic scan for commands is disabled by default. You can enable it using `searchCommands(true)` bundle option.
If search enabled, all classes extending `Command` are instantiated using default constructor and registered in bootsrap object.
`EnvironmentCommand` must have construction with `Application` argument.

You can use guice injections only in `EnvironmentCommand`'s because only these commands start bundles (and so launch guice context creation).

No matter if environment command was registered with classpath scan or manually in bootstrap, `injector.injectMembers(commands)` will be called on it
to inject guice dependencies.

### Request scoped beans

You can use request scoped beans in main context. 

```java
@RequestScoped
public class MyRequestScopedBean {
```

To obtain bean reference use provider:

```java
Provider<MyRequestScopedBean> myBeanProvider;
```

You can get request and response objects in any bean:

```java
Provider<HttpServletRequest> requestProvider
Provider<HttpServletResponse> responseProvider
```

NOTE: this will work only for application context and will not work for admin context (because guice implementation
is limited to one servlet context). As a result request scope and request/response injections will not work in tasks, 
healthchecks and admin filters and servlets.

#### Jersey-guice provided objects

The following objects available for injection:

* com.sun.jersey.spi.container.WebApplication
* javax.ws.rs.ext.Providers
* com.sun.jersey.core.util.FeaturesAndProperties
* com.sun.jersey.spi.MessageBodyWorkers
* com.sun.jersey.api.core.ResourceContext
* com.sun.jersey.spi.container.ExceptionMapperContext

The following request-scope objects available for injection:

* com.sun.jersey.api.core.HttpContext
* javax.ws.rs.core.UriInfo
* com.sun.jersey.api.core.ExtendedUriInfo
* com.sun.jersey.api.core.HttpRequestContext
* javax.ws.rs.core.HttpHeaders
* javax.ws.rs.core.Request
* javax.ws.rs.core.SecurityContext
* com.sun.jersey.api.core.HttpResponseContext

#### Writing custom installer

Installer should implement [FeatureInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/FeatureInstaller.java)
interface. It will be automatically registered if auto scan is enabled. To register manually use `.features()` bundle option.

Installer `matches` method implements feature detection logic. You can use `FeatureUtils` for type checks, because it's denies
abstract classes. Method is called for classes found during scan to detect installable features and for classes directly specified
with `.beans()` bundle option to detect installer.

Three types of installation supported. Installer should implement one or more of these interfaces:
* `BindingInstaller` allows custom guice bindings. If installer doesn't implement this interface sinmple `bind(type)` will be called to register in guice.
* `TypeInstaller` used for registration based on type (no instance created during installation).
* `InstanceInstaller` used for instance registration. Instance created using `injector.getInstance(type)`.

`BindingInstaller` called in time of injector creation, whereas `TypeInstaller` and `InstanceInstaller` are called just after injector creation.

Installers are not guice beans! So injections can't be used inside them. This is because installers also used during initialization phase and instantiated before injector creation.

Example installer:

```java
public class CustomInstaller implements FeatureInstaller<CustomFeature> {
    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, CustomFeature.class);
    }    
}
```

Finds all CustomFeature derived classes and register them in guice (implicit registration). Note that no installer interfaces were used, 
because guice registration is enough.

Now suppose CustomFeature is a base class for our jersey extensions. Then installer will be:

```java
public class CustomInstaller implements FeatureInstaller<CustomFeature>, InstanceInstaller<CustomFeature> {
    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, CustomFeature.class);
    }
    
    @Override
    public void install(final Environment environment, final CustomFeature instance) {
        environment.jersey().register(instance);
    }
    
    @Override
    public void report() {
    }
}
```

#### Ordering

In order to support ordering, installer must implement `Ordered` interface.
If installer doesn't implement it extensions will not be sorted, even if extensions has `@Order` annotations. 

As example, see [ManagedInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)

#### Reporting

Installers `report()` method will be called after it finish installation of all found extensions. Report provides
user visibility of installed extensions. 

To simplify reporting use predefined `Reporter` class.See example usage in 
[ManagedInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)

For complex cases, reporter may be extended to better handle installed extensions. As examples see 
[plugin installer reporter](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/plugin/PluginReporter.java)
and [provider installer reporter](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/provider/ProviderReporter.java)

### Might also like

* [guice-validator](https://github.com/xvik/guice-validator) - hibernate validator integration for guice 
(objects validation, method arguments and return type runtime validation)
* [guice-ext-annotations](https://github.com/xvik/guice-ext-annotations) - @Log, @PostConstruct, @PreDestroy and
utilities for adding new annotations support
* [guice-persist-orient](https://github.com/xvik/guice-persist-orient) - guice integration for orientdb
* [dropwizard-orient-server](https://github.com/xvik/dropwizard-orient-server) - embedded orientdb server for dropwizard

-
[![Slush java lib generator](http://img.shields.io/badge/Powered%20by-Slush%20java%20lib%20generator-orange.svg?style=flat-square)](https://github.com/xvik/slush-lib-java)