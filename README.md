#Dropwizard guice integration
[![License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)
[![Build Status](http://img.shields.io/travis/xvik/dropwizard-guicey.svg?style=flat&branch=master)](https://travis-ci.org/xvik/dropwizard-guicey)
[![Coverage Status](https://img.shields.io/coveralls/xvik/dropwizard-guicey.svg?style=flat)](https://coveralls.io/r/xvik/dropwizard-guicey?branch=master)

### About

[Dropwizard](http://dropwizard.io/) integration based on ideas from [dropwizard-guice](https://github.com/HubSpot/dropwizard-guice) and 
[dropwizardy-guice](https://github.com/jclawson/dropwizardry/tree/master/dropwizardry-guice) 
(which was derived from first one).

Features:
* Guice injector created on run phase (in opposite to dropwizard-guice; the same as dropwizardry-guice)
* Jersey integration through [jersey-guice](https://jersey.java.net/documentation/1.18/chapter_deps.html#d4e1876) (the same in both libs)
* No base classes for application or guice module (only bundle registration required)
* Configurable installers mechanism: each supported feature (task install, health check install, etc) has it's own installer and may be disabled.
* Custom feature installers could be added
* Optional classpath scan to search features: resources, tasks, commands, health checks etc (without dependency on reflections library)
* Injections works in commands (environment commands)
* Support injection of Bootstrap, Environment and Configuration objects into guice modules before injector creation 

### Setup

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) (package appear immediately after release) 
and then to maven central (require few days after release to be published). 

[![Download](https://api.bintray.com/packages/vyarus/xvik/dropwizard-guicey/images/download.png) ](https://bintray.com/vyarus/xvik/dropwizard-guicey/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/dropwizard-guicey/badge.svg?style=flat)](https://maven-badges.hrokuapp.com/maven-central/ru.vyarus/dropwizard-guicey)

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>dropwizard-guicey</artifactId>
  <version>0.9.0</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus:dropwizard-guicey:0.9.0'
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
            .features(ResourceInstaller.class, TaskInstaller.class, ManagedInstaller.class)
            .beans(MyTask.class, MyResource.class, MyManaged.class)
            .modules(new MyModule())
            .build()
    );
    bootstrap.addCommand(new MyCommand())
}
```

Installers defined manually. They will be used to detect provided bean classes and properly install them.

Look [tests](https://github.com/xvik/dropwizard-guicey/tree/master/src/test/groovy/ru/vyarus/dropwizard/guice/support) 
for configuration examples.

Bundle options:
* `enableAutoConfig` enables auto scan on one or more packages to scan. If not set - no auto scan will be performed.
* `searchCommands` if true, command classes will be searched in classpath and registered in bootstrap object. 
Auto scan must be enabled. By default commands scan is disabled (false), because it may be not obvious.
* `modules` one or more guice modules to start. Not required: context could start even without custom modules.
* `disableInstallers` disables installers, found with auto scan. May be used to override default installer or disable it.
Note: when auto scan not enabled no installers will be registered automatically.
* `features` registers feature installers. Used either to add installers from packages not visible by auto scan or to
configure installers when auto scan not used.
* `beans` manually register classed (for example, when auto scan disabled). Classes will be installed using configured installers.
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

### Installers

Installer is a core integration concept: every extension point has it's own installer. Installers used for both auto scan and manual modes
(the only difference is in manual mode classes specified manually).
Installers itself are resolved using classpath scanning, so it's very easy to add custom installers (and possibly override default).

All default installers could be found [here](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature)

When installer recognize class, it binds it into guice `binder.bind(foundClass)`.
On run phase (when injector created) all found or manually provided extensions are instantiated (`injector.getInstance(foundClass)`) and passed to installer 
to register extension within dropwizard.

##### Resource installer

[ResourceInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ResourceInstaller.java) 
finds classes annotated with `@Path` and register them as resources (after class registration
in binder, jersey-guice performs actual registration implicitly)

##### Task installer

[TaskInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/TaskInstaller.java)
finds classes extending `Task` class and register their instance in environment.

##### Managed installer

[ManagedInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)
finds classes implementing `Managed` and register their instance in environment.

##### Lifecycle installer

[LifeCycleInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/LifeCycleInstaller.java)
finds classes implementing jetty `LifeCycle` interface and register their instance in environment.

##### Health check installer

[HealthCheckInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/health/HealthCheckInstaller.java)
finds classes extending `NamedHealthCheck` class and register their instance in environment.

Custom base class is required, because default `HealthCheck` did not provide check name, which is required for registration.

##### Jersey injectable provider installer

[JerseyInjectableProviderInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/JerseyInjectableProviderInstaller.java)
finds classes implementing `InjectableProvider` interface and register their instance in environment.

#### Jersey provider installer

[JerseyProviderInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/JerseyProviderInstaller.java)
finds classes annotated with jersey `@Provider` annotation and register their instance in environment.

##### Eager installer

[EagerInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/eager/EagerInstaller.java)
finds classes annotated with `@Eager` annotation and register them in guice injector.

This installer doesn't relate to dropwizard directly, but useful in case when you have bean not injected by other beans (so guice can't register
it automatically). Normally you would have to manually register it in module.
Also, found bean automatically instantiated, so may be used as a king of `.asEagerSingleton()` in development stage.

Most likely such bean will contain initialization logic. 

May be used in conjunction with @PostConstruct annotations (e.g. using [ext-annotations](https://github.com/xvik/guice-ext-annotations)):
installer finds and register bean and post construct annotation could run some logic. Note: this approach is against guice philosophy and should
be used for quick prototyping only.

#### Writing custom installer

Installer should implement [FeatureInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/FeatureInstaller.java)
interface. It will be automatically registered if auto scan is enabled. To register manually use `.features()` bundle option.

Installer `matches` method implements feature detection logic. You can use `FeatureUtils` for type checks, because it's denies
abstract classes. Method is called for classes found during scan to detect installable features and for classes directly specified
with `.beans()` bundle option to detect installer.

Installer `install` method called after guice injector creation (in run phase) to register extension instance within dropwizard environment
(but can be used to do any other action: probably integration with some additional library).

Installers are not guice beans! So injections can't be used inside them. This is because installers also used during initialization phase.

Example installer:

```java
public class CustomInstaller implements FeatureInstaller<CustomFeature> {
    @Override
    boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, CustomFeature.class)
    }

    @Override
    void install(final Environment environment, final CustomFeature instance) {
        // do nothing - bean already registered
    }
}
```

Finds all CustomFeature derived classes and register them in guice (implicit registration; almost the same as eager installer, but with type)

### Commands support

Automatic scan for commands is disabled by default. You can enable it using `searchCommands(true)` bundle option.
If search enabled, all classes extending `Command` are instantiated using default constructor and registered in bootsrap object.
`EnvironmentCommand` must have construction with `Application` argument.

You can use guice injections only in `EnvironmentCommand`'s because only these commands start bundles (and so launch guice context creation).

No matter if environment command was registered with classpath scan or manually in bootstrap, `injector.injectMembers(commands)` will be called on it
to inject guice dependencies.

### Might also like

* [guice-validator](https://github.com/xvik/guice-validator) - hibernate validator integration for guice 
(objects validation, method arguments and return type runtime validation)
* [guice-ext-annotations](https://github.com/xvik/guice-ext-annotations) - @Log, @PostConstruct, @PreDestroy and
utilities for adding new annotations support
* [guice-persist-orient](https://github.com/xvik/guice-persist-orient) - guice integration for orientdb
* [dropwizard-orient-server](https://github.com/xvik/dropwizard-orient-server) - embedded orientdb server for dropwizard

-
[![Slush java lib generator](http://img.shields.io/badge/Powered%20by-Slush%20java%20lib%20generator-orange.svg?style=flat-square)](https://github.com/xvik/slush-lib-java)