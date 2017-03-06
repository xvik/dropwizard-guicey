#Dropwizard guice integration
[![License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)
[![Build Status](http://img.shields.io/travis/xvik/dropwizard-guicey.svg?style=flat&branch=master)](https://travis-ci.org/xvik/dropwizard-guicey)
[![Coverage Status](https://img.shields.io/coveralls/xvik/dropwizard-guicey.svg?style=flat)](https://coveralls.io/r/xvik/dropwizard-guicey?branch=master)

> **[Examples](https://github.com/xvik/dropwizard-guicey-examples)**  |  **[Extensions and integrations](https://github.com/xvik/dropwizard-guicey-ext)**

Use [google group](https://groups.google.com/forum/#!forum/dropwizard-guicey) or [gitter chat](https://gitter.im/xvik/dropwizard-guicey) to ask questions, discuss current features (general support). 

**DOCUMENTATION**: http://xvik.github.io/dropwizard-guicey/



### About

[Dropwizard](http://dropwizard.io/) 1.0.5 [guice](https://github.com/google/guice) (4.1.0) integration. 

Originally inspired by [dropwizard-guice](https://github.com/HubSpot/dropwizard-guice) and 
[dropwizardy-guice](https://github.com/jclawson/dropwizardry/tree/master/dropwizardry-guice) 
(which was derived from first one).

Features:
* Guice injector created on run phase
* Compatible with guice restrictive options: `disableCircularProxies`, `requireExactBindingAnnotations` and `requireExplicitBindings`
* Flexible [HK2](https://hk2.java.net/2.5.0-b05/introduction.html) integration
* No base classes for application or guice module (only bundle registration required)
* Configurable [installers](#installers) mechanism: each supported feature (task install, health check install, etc) has it's own installer and may be disabled
* [Custom feature installers](#writing-custom-installer) could be added
* Optional [classpath scan](#classpath-scan) to search features: resources, tasks, commands, health checks etc (without dependency on reflections library)
* Injections [works in commands](#commands-support) (environment commands)
* Support injection of Bootstrap, Environment and Configuration objects into guice modules [before injector creation](#module-autowiring) 
* Guice [ServletModule](#guice-servletmodule-support) can be used to bind servlets and filters for main context (may be [disabled](#disable-guice-servlets))
* Dropwizard style [reporting](#reporting) of installed extensions
* Admin context [rest emulation](#admin-rest)
* Custom [junit rule](#useguiceyapp) for lightweight integration testing
* [Spock](http://spockframework.org) [extensions](#spock)

### Thanks to

* [Sébastien Boulet](https://github.com/gontard) ([intactile design](http://intactile.com)) for very useful feedback
* [Nicholas Pace](https://github.com/segfly) for governator integration

### Setup

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) (package appear immediately after release) 
and then to maven central (require few days after release to be published). 

[![JCenter](https://img.shields.io/bintray/v/vyarus/xvik/dropwizard-guicey.svg?label=jcenter)](https://bintray.com/vyarus/xvik/dropwizard-guicey/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus/dropwizard-guicey.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/dropwizard-guicey)

May be used through [extensions project BOM](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-bom) or directly.

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>dropwizard-guicey</artifactId>
  <version>4.0.1</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus:dropwizard-guicey:4.0.1'
```

- for dropwizard 0.9 use version 3.3.0 (see [old docs](https://github.com/xvik/dropwizard-guicey/tree/dw-0.9))
- for dropwizard 0.8 use version 3.1.0 (see [old docs](https://github.com/xvik/dropwizard-guicey/tree/dw-0.8))
- for dropwizard 0.7 use version 1.1.0 (see [old docs](https://github.com/xvik/dropwizard-guicey/tree/dw-0.7))

##### Snapshots

You can use snapshot versions through [JitPack](https://jitpack.io):

* Go to [JitPack project page](https://jitpack.io/#xvik/dropwizard-guicey)
* Select `Commits` section and click `Get it` on commit you want to use (top one - the most recent)
* Follow displayed instruction: add repository and change dependency (NOTE: due to JitPack convention artifact group will be different)

### Usage

You can use [classpath scanning](#classpath-scan) or configure everything manually (or combine both).
Auto scan configuration [example](https://github.com/xvik/dropwizard-guicey-examples/tree/master/autoconfig-base):

```java
@Override
public void initialize(Bootstrap<TestConfiguration> bootstrap) {
    bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
            .enableAutoConfig("package.to.scan")
            .searchCommands()            
            .build()
    );
}
```

Auto scan will resolve installers and use found installers to detect extensions in classpath (and install them). 
Commands will also be searched in classpath, instantiated and set into bootstrap object.

Manual configuration [example](https://github.com/xvik/dropwizard-guicey-examples/tree/master/manualconfig-base):

```java
@Override
void initialize(Bootstrap<TestConfiguration> bootstrap) {
    bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
            .noDefaultInstallers() 
            .installers(ResourceInstaller.class, TaskInstaller.class, ManagedInstaller.class)
            .extensions(MyTask.class, MyResource.class, MyManaged.class)
            .modules(new MyModule())
            .build()
    );
    bootstrap.addCommand(new MyCommand())
}
```

[Core installers](#installers) disabled and only specific installers registered manually. They will be used to detect provided bean classes and properly install them.

Look [tests](src/test/groovy/ru/vyarus/dropwizard/guice/support) 
and [examples repository](https://github.com/xvik/dropwizard-guicey-examples) for configuration examples.

After application start, look application log for dropwizard style extension installation [reports](#reporting).

#### Builder methods (configuration)

`GuiceBundle.<TestConfiguration> builder()` contains shortcuts for all available features (except [admin rest](#admin-rest)), so in most cases required function may be found 
only by looking at available methods (and reading javadoc).

##### Extensions methods

There are 4 configurable extensions:
* [Installers](#installers) used to recognize and install extension (usually incapsulates integration logic: get extension instance from guice injector and register in dropwizard (or jersey, hk, whatever))
* Extensions are actual application parts, written by you (resources, tasks, health checks, servlets etc)
* Installers and extensions could be grouped into [bundles](#guicey-bundles): to extract commonly used logic (e.g. some 3rd party library integration or just application part)
* Guice modules

[Options](#options) are low level configurations (mostly, ability to change opinionated defaults) for guicey. You may define and use your own options.

Configuration process is recorded and may be observed with by [diagnostic info](https://github.com/xvik/dropwizard-guicey/wiki/Diagnostic-info),
so there is always a way to understand what where and how was configured.

* `bundles` registers [guicey bundles](#guicey-bundles)
* `installers` registers feature [installers](#installers). Used either to add installers from packages not visible by [auto scan](#classpath-scan) or to
configure installers when auto scan not used.
* `disableInstallers` disables installers, found with auto scan or registered manually. May be used to override default installer or disable it.
* `extensions` manually register classes (for example, when auto scan disabled). Classes will be installed using configured installers.
* `modules` one or more guice modules to start. Not required: context could start even without custom modules.
* `option` sets option value ([options](#options) are used by guicey itself and may be used by 3rd party extensions)
* `build` allows specifying guice injector stage (production, development). By default, PRODUCTION stage used.

IMPORTANT: configured bundles, modules, installers and extensions are checked for duplicates using class type. Duplicate configurations will be simply ignored.
For modules and bundles, which configured using instances, duplicates removes means that if two instances of the same type registered, then second instance will
 be ignored.

##### Configuration methods

* `enableAutoConfig` enables [auto scan](#classpath-scan) on one or more packages to scan. If not set - no auto scan will be performed - all extensions must be registered manually.
* `searchCommands` if true, command classes will be [searched in classpath](#commands-support) and registered in bootstrap object.
Auto scan must be enabled. By default commands scan is disabled (false), because it may be not obvious.
* `configureFromDropwizardBundles` enables registered [dropwizard bundles lookup](#dropwizard-bundles-unification) if they implement `GuiceyBundle` (false by default)
* `bindConfigurationInterfaces` enables configuration class binding with [direct interfaces](#configuration-binding). This is useful for HasSomeConfig interfaces convention. Without it, configuration will be bound to all classes in configuration hierarchy

##### Bundles related methods

Guicey provides few bundles out of the box. These methods enables or configures these bundles.

* `noDefaultInstallers` disables default [installers](#installers) installation (automatic installation for [CoreInstallersBundle](#core-installers-bundle))
* `useWebInstallers` shortcut to install [WebInstallersBundle](#web-installers) which will register web filter, servlet and listener installers
* `strictScopeControl` shortcut to install [HK2DebugBundle](#hk-debug-bundle), which checks extensions instantiation in correct context (may be useful for debug)
* `printDiagnosticInfo` shortcut to install [DiagnosticBundle](#diagnostic-bundle), which prints **[diagnostic info](https://github.com/xvik/dropwizard-guicey/wiki/Diagnostic-info)**
* `printAvailableInstallers` prints to log all registered installers with registration sources (use to quickly understand available features) 

##### Other configuration methods

* `noGuiceFilter` [disables guice filter](#disable-guice-servlets) registration on both contexts (as a result no request and session scopes will be available and servlet modules will be rejected). Intended to be used when web installers enabled.
* `injectorFactory` sets custom injector factory (for example, to use [governator](https://github.com/Netflix/governator)). See [custom factory example](https://github.com/xvik/dropwizard-guicey/wiki/Governator-Integration).
* `bundleLookup` overrides default guicey [bundle lookup](#bundle-lookup) implementation (used for example to [plug-n-play modules](#service-loader-lookup) form 3rd party jars) 
* `disableBundleLookup` disables default [bundle lookup](#bundle-lookup)
* `options` sets multiple [options](#options) at once (useful for [custom option lookup mechanisms](#options-lookup))


#### Injector instance

In some cases it may be important to get injector instance outside of guice context.

Injector instance could be resolved with:
* getInjector method on GuiceBundle instance (note that injector initialized on run phase, and NPE will be thrown if injector not initialized)
* InjectorLookup.getInjector(app).get() static call using application instance (lookup returns Optional and last get() throws exception or returns injector instance).

If you need lazy injector reference, you can use `InjectorProvider` class (it's actually `Provider<Injector>`):

```java
InjectorProvider provider = new InjectorProvider(app);
// somewhere after run phase
Injector injector = provider.get();
```

When you are inside your Application class:

```java
InjectorLookup.getInjector(this).get().getInstance(SomeService.class)
```

Most likely, requirement for injector instance means integration with some third party library.
Consider writing custom installer in such cases (it will eliminate need for injector instance).

### Configuration binding 

`Configuration` bound to guice as:
* io.dropwizard.Configuration
* Your configuration class (MyConfiguration extends Configuration)
* All classes between then: e.g. if MyConfiguration extends MyAbstractConfiguration extends Configuration then MyAbstractConfiguration will be also bound

When `.bindConfigurationInterfaces()` enabled, all direct interfaces implemented by configuration class (or any subclass) are bound.
This may be used to support common Has<Something> configuration interfaces convention used to recognize your extension configuration in configuration object.

For example:

```java
    GuiceBundle.builder()
        .bundConfigurationInterfactes()
        ...

    public interface HasFeatureX {
        FeatureXConfig getFetureXConfig();
    }
        
    public class MyConfiguration extends Configuration implements HasFeatureXConfig {...}
    
    public class MyBean {
        @Inject HasFeatureX configuration;
        ...
    }
```

Interfaces binding will ignore interfaces in java.* or groovy.* packages (to avoid unnecessary bindings).

### Environment binding

Dropwizard `Environment` is bound to guice context (available for injection).

It is mostly useful to perform additional configurations in guice bean for features not covered with installers. For example:

```java
public class MyBean {
    
    @Inject
    public MyBean(Environment environment) {
        environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener {
            public void serverStarted(Server server) {
                callSomeMethod();
            }
        })
    }
}
```

It's not the best example, but it illustrates usage (and such things usually helps to quick-test something). 

### Authentication

All [dropwizard authentication](http://www.dropwizard.io/1.0.5/docs/manual/auth.html) 
configurations are pretty much the same. Here is an example of oauth configuration:

```java
@Provider
class OAuthDynamicFeature extends AuthDynamicFeature {

    @Inject
    OAuthDynamicFeature(MyAuthenticator authenticator, MyAuthorizer authorizer, Environment environment) {
        super(new OAuthCredentialAuthFilter.Builder<User>()
                .setAuthenticator(authenticator)
                .setAuthorizer(authorizer)
                .setPrefix("Bearer")
                .buildAuthFilter())

        environment.jersey().register(RolesAllowedDynamicFeature.class)
        environment.jersey().register(new AuthValueFactoryProvider.Binder(User.class))
    }
}
```

Here MyAuthenticator and MyAuthorizer are guice beans. OAuthDynamicFeature is guice bean also (created by guice),
but instance registered into jersey (by JerseyProviderInstaller)

For more details see [wiki page](https://github.com/xvik/dropwizard-guicey/wiki/Authentication-integration)

### Classpath scan

Classpath scanning is activated by specifying packages to scan in bundle `.enableAutoConfig("package.to.scan")`.

When auto scan enabled:
* Feature installers searched in classpath (including default installers): classes implementing `FeatureInstaller`.
* Search for extensions in classpath using `FeatureInstaller#matches` method.
* If commands search enabled (`.searchCommands()`), performs search for all classes extending `Command` and [install them into
bootstrap](#commands-support).

Classes are searched in specified packages and all their subpackages. 
[Inner static](src/test/groovy/ru/vyarus/dropwizard/guice/cases/innercls/AbstractExceptionMapper.groovy) 
classes are also resolved.

`@InvisibleForScanner` annotation hides class from scanner (for example, to install it manually or to avoid installation at all)

#### Motivation

Usually, dropwizard applications are not so big (middle to small) and all classes in application package are used (so you will load all of them in any case). 
 
Classpath scan looks for all classes in provided package(s) and loads all found classes. Usual solutions like [reflections](https://github.com/ronmamo/reflections), 
[fast scanner](https://github.com/lukehutch/fast-classpath-scanner) or even jersey's internal classpath scan parse class structure instead of loading classes. 
In general cases it is better solution, but, as we use all application classes in any case, loading all of them a bit earlier is not a big deal. 
Moreover, operations with loaded classes are much simpler then working with class structure (and so installers matching logic is very simple).

Using classpath scan is very handy during development: you simply add features (resources, tasks, servlets etc) and they are automatically discovered and installer.
Actual application configuration could always be checked with [diagnostic output](https://github.com/xvik/dropwizard-guicey/wiki/Diagnostic-info)),
so there should not be any problems for using classpath scan for production too.

IMPORTANT: It's a bad idea to use classpath scan for resolving extensions from 3rd party jars. Group extensions in external jars into bundles: you don't need classpath scan there 
cause all used extensions are already known and unlikely to change. Moreover, bundle "documents" extensions.
If you want plug-n-play behaviour (bundle installed when jar appear in classpath) then use bundle lookup (enabled by default) which could load 
bundles with service loader definition (ServiceLoaderBundleLookup).

To summarize: use scan only for application package. When part of application extracted to it's own library (usually already mature part) create bundle
for it with explicit extensions definition. Use manual bundles installation or bundle lookup mechanism.

### Module autowiring

Because guice modules are registered in init section, it's not possible to get reference for environment and configuration objects.
To overcome this limitation, you can implement `BootstrapAwareModule`, `EnvironmentAwareModule` or `ConfigurationAwareModule`
interfaces and reference object will be set to module just before injector creation (allowing you to use it during module configuration).

This will work only for modules set to `modules()` bundle option.

[Example](src/test/groovy/ru/vyarus/dropwizard/guice/support/AutowiredModule.groovy)

Alternatively, abstract module class `DropwizardAwareModule` may be used. It implements all aware interfaces and reduce implementation boilerplate.

```java
public class MyModule extends DropwizardAwareModule<MyConfiguration> {
    @Override
    protected void configure() {
        bootstrap()     // Bootstrap instance
        environment()   // Environment instance
        configuration() // MyConfiguration instance
        appPackage()    // application class package 
    }
} 
```

### Extension ordering

Some installers support extensions ordering (managed, lifecycle, servlets and filters).
To define extensions order use `@Order` annotation. Extensions sorted naturally (e.g. `@Order(1)` before `@Order(2)`).
Extensions without annotation goes last.

### Installers

Installer is a core integration concept: every extension point has it's own installer. Installers used for both [auto scan](#classpath-scan) and manual modes
(the only difference is in manual mode classes specified manually).
Installers itself are resolved using classpath scanning, so it's very easy to add custom installers (and possibly override default one by disabling it and registering alternative).

All default installers are registered by [CoreInstallersBundle](src/main/java/ru/vyarus/dropwizard/guice/module/installer/CoreInstallersBundle.java)

When installer recognize class, it binds it into guice `binder.bind(foundClass)` (or bind by installer if it [support binding](#writing-custom-installer)).
But extensions annotated with `@LazyBinding` are not bind to guice context. This may be useful to [delay bean creation](#jersey-guice-integration):
by default, guice production stage will instantiate all registered beans.

On run phase (after injector created) all found or manually provided extensions are installed by type or instantiated (`injector.getInstance(foundClass)`) and passed to installer 
to register extension within dropwizard (installation type is defined by installer).

Installers order is defined by `@Order` annotation. Default installers are ordered with indexes from 10 to 110 with gap 10.
If you need to run your installer before/after some installer simply annotate it with `@Order`. Installers without annotation goes last.

**IMPORTANT: each extension is installed by only one installer!** If extension could be recognized by more then one installers, it will be installed only by first matching installer (according to installers order). 

##### Resource
[ResourceInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/ResourceInstaller.java)
finds classes annotated with `@Path` and register their instance as resources. Resources **registered as singletons**, even if guice bean scope isn't set. If extension annotated as
`@HK2Managed` then jersey HK container will manage bean creation (still guice beans injections are possible).

Also recognize resources with `@Path` annotation on implemented interface. Annotations on interfaces are useful for [jersey client proxies](https://jersey.java.net/apidocs/2.22.1/jersey/org/glassfish/jersey/client/proxy/package-summary.html) 
([example](src/test/groovy/ru/vyarus/dropwizard/guice/cases/ifaceresource/InterfaceResourceDefinitionTest.groovy#L42)). 

Use `Provider` for [request scoped beans](#request-scoped-beans) injections.

##### Task
[TaskInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/TaskInstaller.java)
finds classes extending `Task` class and register their instance in environment.

##### Managed
[ManagedInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)
finds classes implementing `Managed` and register their instance in environment. Support ordering.


##### Lifecycle
[LifeCycleInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/LifeCycleInstaller.java)
finds classes implementing jetty `LifeCycle` interface and register their instance in environment. Support ordering.


##### Health
[HealthCheckInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/health/HealthCheckInstaller.java)
finds classes extending `NamedHealthCheck` class and register their instance in environment.

Custom base class is required, because default `HealthCheck` did not provide check name, which is required for registration.


##### Jersey extension
[JerseyProviderInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/provider/JerseyProviderInstaller.java)
finds classes annotated with jersey `@Provider` annotation and register their instance in jersey (**forced singleton**). Supports the following extensions:
like [Factory](src/test/groovy/ru/vyarus/dropwizard/guice/support/provider/LocaleInjectableProvider.groovy),
[ExceptionMapper](https://github.com/xvik/dropwizard-guicey/tree/master/src/test/groovy/ru/vyarus/dropwizard/guice/support/feature/DummyExceptionMapper.groovy),
[InjectionResolver](src/test/groovy/ru/vyarus/dropwizard/guice/support/provider/annotated/AuthInjectionResolver.groovy),
[ValueFactoryProvider](src/test/groovy/ru/vyarus/dropwizard/guice/support/provider/annotated/AuthFactoryProvider.groovy),
[ParamConverterProvider](src/test/groovy/ru/vyarus/dropwizard/guice/support/provider/paramconv/FooParamConverter.groovy), 
[ContextResolver](https://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/ContextResolver.html), 
[MessageBodyReader](https://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/MessageBodyReader.html), 
[MessageBodyWriter](https://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/MessageBodyWriter.html),
[ReaderInterceptor](https://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/ReaderInterceptor.html), 
[WriterInterceptor](https://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/WriterInterceptor.html), 
[ContainerRequestFilter](https://docs.oracle.com/javaee/7/api/javax/ws/rs/container/ContainerRequestFilter.html), 
[ContainerResponseFilter](https://docs.oracle.com/javaee/7/api/javax/ws/rs/container/ContainerResponseFilter.html),
[DynamicFeature](https://docs.oracle.com/javaee/7/api/javax/ws/rs/container/DynamicFeature.html), 
[ApplicationEventListener](https://jersey.java.net/apidocs/2.9/jersey/org/glassfish/jersey/server/monitoring/ApplicationEventListener.html) 
(all of this usually registered through `environment.jersey().register()`).

Due to specifics of HK integration (see below), you may need to use `@HK2Managed` to delegate bean creation to HK,
`@LazyBinding` to delay bean creation to time when all dependencies will be available and, of course, `Provider` (for guice or HK).


##### Jersey Feature
[JerseyFeatureInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/JerseyFeatureInstaller.java)
finds classes implementing `javax.ws.rs.core.Feature` and register their instance in jersey.

It may be useful to configure jersey inside guice components:

```java

public class MyClass ... {
    ...   
    public static class ConfigurationFeature implements Feature {
        @Override
        boolean configure(FeatureContext context) {
            context.register(RolesAllowedDynamicFeature.class)
            context.register(new AuthValueFactoryProvider.Binder(User.class))
            return true;
        }
    }
}
```

But often the same could be achieved by injecting `Environment` instance.

##### Eager
[EagerSingletonInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/eager/EagerSingletonInstaller.java)
finds classes annotated with `@EagerSingleton` annotation and register them in guice injector. It is equivalent of eager singleton registration
`bind(type).asEagerSingleton()`.

This installer doesn't relate to dropwizard directly, but useful in case when you have bean not injected by other beans (so guice can't register
it automatically). Normally you would have to manually register it in module.

Most likely such bean will contain initialization logic. 

Ideal for cases not directly covered by installers. For example:

```java
@EagerSingleton
public class MyListener implements LifeCycle.Listener {
    
    @Inject
    public MyListener(Environment environment) {
        environment.lifecicle.addListener(this);
    }
}
```

Class will be recognized by eager singleton extension, environment object injected by guice and we manually register listener.

May be used in conjunction with @PostConstruct annotations (e.g. using [ext-annotations](https://github.com/xvik/guice-ext-annotations)):
installer finds and register bean and post construct annotation could run some logic. Note: this approach is against guice philosophy and should
be used for quick prototyping only.


##### Plugin
[PluginInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/plugin/PluginInstaller.java)
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

NOTE: It's not required to use enum as key. Any type could be set in your custom annotation (for example, `String value();`) .

#### Web installers

Servlet api 3.0 provides @WebServlet, @WebFilter and @WebListener annotations, but they are not recognized in dropwizard
(because jersey-annotations module is not provided). Web installers recognize this annotations and register guice-managed filters, servlets and listeners 
instances.

Web installers are registered by [WebInstallersBundle](src/main/java/ru/vyarus/dropwizard/guice/module/installer/WebInstallersBundle.java).
Use `.useWebInstallers()` shortcut to enable.

These installers are not enabled by default, because dropwizard is primarily rest oriented framework and you may not use custom servlets and filters at all
(so no need to spent time trying to recognize them). Moreover, using standard servlet api annotations may confuse users and so 
it must be user decision to enable such support. Other developers should be guided bu option name and its javadoc (again to avoid confusion, no matter that
it will work exactly as expected)

There is a difference between using web installers and registering servlets and filters with [guice servlet module](#guice-servletmodule-support).
Guice servlet module handles registered servlets and filters internally in GuiceFilter (which is installed by guicey in both app and admin contexts).
As a side effect, there are some compatibility issues between guice servlets and native filters (rare and usually not blocking, but still).
Web installers use guice only for filter or servlet instance creation and register this instance directly in dropwizard environment (using annotation metadata).  

In many cases, annotations are more convenient way to declare servlet or filter registration comparing to servlet module. 

Note: using annotations you can register async servlets and filters (with annotations asyncSupported=true option).
In contrast, it is impossible to register async with guice servlet module.

##### Web Servlet
[WebServletInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/web/WebServletInstaller.java)
finds classes annotated with `@WebServlet` annotation and register them. Support ordering.

```java
@WebServlet("/mapped")
public class MyServlet extneds HttpServlet { ... }
```

Only the following annotation properties are supported: name, urlPatterns (or value), initParams, asyncSupported 
([example async](src/test/groovy/ru/vyarus/dropwizard/guice/web/async/AsyncServletTest.groovy#L52)).

Servlet name is not required. If name not provided, it will be generated as:
. (dot) at the beginning to indicate generated name, followed by lower-cased class name. If class ends with "servlet" then it will be cut off.
For example, for class "MyCoolServlet" generated name will be ".mycool".

One or more specified servlet url patterns may clash with already registered servlets. By default, such clashes are just logged as warnings.
If you want to throw exception in this case, use `InstallersOptions.DenyServletRegistrationWithClash` option. 
Note that clash detection relies on servlets registration order so clash may not appear on your servlet but on some other servlet registered later 
(and so exception will not be thrown).

##### Web Filter

[WebFilterInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/web/WebFilterInstaller.java)
finds classes annotated with `@WebFilter` annotation and register them. Support ordering.

```java
@WebFilter("/some/*")
public class MyFilter implements Filter { ... }
```

Only the following annotation properties are supported: filterName, urlPatterns (or value), servletNames, dispatcherTypes, initParams, asyncSupported
([example async](src/test/groovy/ru/vyarus/dropwizard/guice/web/async/AsyncFilterTest.groovy#L47)).
Url patterns and servlet names can't be used at the same time.

Filter name is not required. If name not provided, then it will be generated as: 
. (dot) at the beginning to indicate generated name, followed by lower-cased class name. If class ends with "filter" then it will be cut off.
For example, for class "MyCoolFilter" generated name will be ".mycool".

##### Web Listener

[WebListenerInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/web/listener/WebListenerInstaller.java)
finds classes annotated with `@WebListener` annotation and register them. Support ordering.

Supported listeners (the same as declared in annotation):
 * javax.servlet.ServletContextListener
 * javax.servlet.ServletContextAttributeListener
 * javax.servlet.ServletRequestListener
 * javax.servlet.ServletRequestAttributeListener
 * javax.servlet.http.HttpSessionListener
 * javax.servlet.http.HttpSessionAttributeListener
 * javax.servlet.http.HttpSessionIdListener

```java
@WebListener
public class MyListener implements ServletContextListener, ServletRequestListener {...}
```

Listener could implement multiple listener interfaces and all types will be registered.

By default, dropwizard is not configured to support sessions. If you define session listeners without configured session support
then warning will be logged (and servlet listeners will actually not be registered).
Error is not thrown to let writing more universal bundles with listener extensions (session related extensions will simply not work).
If you want to throw exception in such case, use `InstallersOptions#DenySessionListenersWithoutSession` option.

##### Admin context

By default, web installers (servlet, filter, listener) target application context. If you want to install into admin context then use `@AdminContext` annotation.

For example: 

```java
@AdminContext
@WebServlet("/mapped")
public class MyServlet extneds HttpServlet { ... }
```

Will install servlet in admin context only.

If you want to install in both contexts use andMain attribute:

```java
@AdminContext(andMain = true
@WebServlet("/mapped")
public class MyServlet extneds HttpServlet { ... }
```

In example above, servlet registered in both contexts.

### Guicey bundles

By analogy with dropwizard bundles, guicey has it's own `GuiceyBundle`. These bundles contains almost the same options as 
main `GuiceBundle` builder. The main purpose is to group installers, extensions and guice modules related to specific 
feature.

Guicey bundles are initialized during dropwizard `run` phase. All guice modules registered in bundles will also be checked if 
[dropwizard objects autowiring](https://github.com/xvik/dropwizard-guicey#module-autowiring) required.

For example, custom integration with some scheduler framework will require installers to register tasks and guice module
to configure framework. GuiceyBundle will allow reduce integration to just one bundle installation.

```java
public class XLibIntegrationBundle implements GuiceyBundle {

    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        bootstrap.installers(
                XLibFeature1Installer.class,
                XLibFeature2Installer.class,                
        )
        .modules(new XLibGuiceModule());
    }
}

bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
        .bundles(new XLibIntegrationBundle())
        .enableAutoConfig("package.to.scan")
        .build()
);
```

Bundles may be used to group application features: e.g. ResourcesBundle, TasksBundle (for example, when auto-scan not enabled to decompose configuration).

Bundles are transitive - bundle can install other bundles. 
Duplicate bundles are detected using bundle type, so infinite configuration loops or duplicate configurations are not possible.

NOTE: be careful if bundle is configurable (for example, requires constructor arguments). If two such bundles will be registered, only
first registration will be actually used and other instance ignored. Note that application configurations (using main GuiceBundle methods) 
performed before bundles processing and so bundle with correct parameters could be registered there.
 
Transitive bundles (or simply a lot of bundles) may cause confusion. Use [diagnostic info](https://github.com/xvik/dropwizard-guicey/wiki/Diagnostic-info) to see how guicey was actually configured.  

##### Core installers bundle

Default installers are grouped into `CoreInstallersBundle`. This bundle is always installed implicitly (so you always have default installers).
It may be disabled using `.noDefaultInstallers()` method.

##### Web installers bundle

`WebInstallersBundle` provides installers for servlets, filters and listeners installation using servlet api annotations
(@WebServlet, @WebFilter, @WebListener). 

Bundle is not installed by default to avoid confusion. May be enabled using `.useWebInstallers()`. 

NOTE: If web installers used, then you may not need guice ServletModule support. To remove GuiceFilter registrations and ServletModule support use
`.noGuiceFilter()`.

##### HK debug bundle 

`HK2DebugBundle` is special debug bundle to check that beans properly instantiated by guice or HK 
(and no beans are instantiated by both).

Only beans installed by installers implementing `JerseyInstaller` (`ResourceInstaller`, `JerseyProviderInstaller`).
All beans must be created by guice and only beans annotated with `@HK2Managed` must be instantiated by HK.

Bundle may be used in tests. For example using `guicey.bundles` property (see bundles lookup below).

May be enabled by `.strictScopeControl()` shortcut method.

##### Diagnostic bundle 

Bundle renders collected guicey diagnostic information (see below): [example output](https://github.com/xvik/dropwizard-guicey/wiki/Diagnostic-info#example-output).
 
Output is highly configurable, use: `DiagnosticBundle.builder()` to configure reporting (if required).
 
Bundle may be registered with bundle lookup mechanism: for example, using `guicey.bundles` property (see bundles lookup below). 
 
May be enabled by `.printDiagnosticInfo()` shortcut method.

Special shortcut `.printAvailableInstallers()` register diagnostic bundle configured for showing only installers. Useful when you looking for available features.
Only one one bundle instance accepted, both options can't be enabled at the same time.

##### Dropwizard bundles unification

Guicey bundles and dropwizard bundles may be unified providing single (standard) extension point for both 
dropwizard and guicey features.

Feature is disabled by default, to enable it use `.configureFromDropwizardBundles()` method.

```java
bootstrap.addBundle(new XLibBundle());
bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
        .configureFromDropwizardBundles(true)
        .build()
);
```

where

```java
public class XLibBundle implements Bundle, GuiceyBundle {
    public void initialize(Bootstrap<?> bootstrap) {...}
    public void initialize(GuiceyBootstrap bootstrap){...}
    public void run(Environment environment) {...}
}
```

When active, all registered bundles are checked if they implement `GuiceyBundle`.
Also, works with dropwizard `ConfiguredBundle`.

WARNING: don't assume if guicey bundle's `initialize` method will be called before/after dropwizard bundle's `run` method. 
Both are possible (it depends if bundle registered before or after GuiceBundle).

### Bundle lookup

Bundle lookup mechanism used to lookup guicey bundles in various sources. It may be used to activate specific bundles
in tests (e.g. HK2DebugBundle) or to install 3rd party extensions from classpath.

Bundle lookup is equivalent to registering bundle directly using builder `bundles` method.

By default, 2 lookup mechanisms active. All found bundles are logged into console.
Duplicate bundles are removed (using bundle class to detect duplicate).

To disable default lookups use `disableBundleLookup`:

```java
bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
        .disableBundleLookup()
        .build()
```

#### System property lookup

System property `guicey.bundles` could contain comma separated list of guicey bundle classes. These bundles 
must have no-args constructor.

For example, activate HK debug bundle for tests:

```
java ... -Dguicey.bundles=ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle
```

Alternatively, system property may be set in code:

```java
PropertyBundleLookup.enableBundles(HK2DebugBundle.class)
```

#### Service loader lookup

Using default java [ServiceLoader](https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html) 
mechanism, loads all GuiceyBundle services.

This is useful for automatically install 3rd party extensions (additional installers, extensions, guice modules).

3rd party jar must contain services file:

```
META-INF/services/ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
```

File contain one or more (per line) GuiceyBundle implementations. E.g.

```
com.foo.Bundle1
com.foo.Bundle2
```

Then Bundle1, Bundle2 would be loaded automatically on startup.

#### Customizing lookup mechanism

Custom bundle lookup must implement `GuiceyBundleLookup` interface:

```java
public class CustomBundleLookup implements GuiceyBundleLookup {

    @Override
    public List<GuiceyBundle> lookup() {
        List<GuiceyBundle> bundles = Lists.newArrayList();
        ...
        return bundles;
    }
}
```

Custom lookup implementation may be registered through:

```java
bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
        .bundleLookup(new CustomBundleLookup())
        .build()
```

But it's better to register it through default implementation `DefaultBundleLookup`, which performs composition 
of multiple lookup implementations and logs resolved bundles to console.

```java
bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
        .bundleLookup(new DefaultBundleLookup().addLookup(new CustomBundleLookup()))
        .build()
```

To override list of default lookups:

```java
bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
        .bundleLookup(new DefaultBundleLookup(new ServiceLoaderBundleLookup(), new CustomBundleLookup()))
        .build()
```

Here two lookup mechanisms registered (property lookup is not registered and will not be implicitly added).

### Options

Options are low level configurations. In contrast to dropwizard configuration (file), which is user specific,
options are set during development and represent developer decisions. Often, options allow to change opinionated default behaviours.

Options are declared with enums. Enums used to naturally group options (also cause pretty reporting). 
Enums must implement Option interface (this makes enum declaration more verbose (because it is impossible to use abstract class in enum),
but provides required option info).

Guicey use options to allow other part to know guice bundle configurations (configured packages to scan, search commands enabling etc) through `GuiceyOptions` enum
(for simplicity, main guicey options usages are already implemented as shortcut methods in guice bundle).
Another use is in web installers to change default behaviour though `InstallersOptions` enum. 

Custom options may be defined for 3rd party bundle or even application. Options is a general mechanism providing configuration and access points with 
standard reporting (part of diagnostic reporting). It may be used as feature triggers (like guicey do), to enable debug behaviour or to specialize
application state in tests (just to name a few).

#### Usage
 
Options may be set only in main GuiceBundle using `.option` method. This is important to let configuration parts to see the same values.
For example, if guicey bundles would be allowed to change options then one bundles would see one value and other bundles - different value and,
for sure, this will eventually lead to inconsistent behaviour.

Option could not be set to null. Option could be null only if it's default value is null and custom value not set.
Custom option value is checked for compatibility with option type (from option definition) and error thrown if does not match.
Of course, type checking is limited to top class and generics are ignored (so List<String> could not be specified and so
can't be checked), but it's a compromise between complexity and easy of use (the same as Enum & Option pair).

Options could be accessed by:
* Guicey bundles using `bootstrap.option()` method
* Installer by implementing `WithOptions` interface (or extend `InstallerOptionsSupport`)
* Any guice bean could inject `Options` bean and use it to access options.

Guicey tracks options definition and usage and report all used options as part of [diagnostic reporting](https://github.com/xvik/dropwizard-guicey/wiki/Diagnostic-info).
Pay attention that defined (value set) but not used (not consumed) options are marked as NOT_USED to indicate possibly redundant options.

Actual application may use options in different time and so option may be defined as NOT_USE even if its actually "not yet" used.
Try to consume options closer to actual usage to let user be aware if option not used with current configuration. For example,
GuiceyOptions.BindConfigurationInterfaces will not appear in report at all if no custom configuration class used.

#### Custom options

Options must be enum and implement Option interface, like this:

```java
enum MyOptions implements Option {

    DoExtraWork(Boolean, true),
    EnableDebug(Boolean, false),
    InternalConfig(String[], new String[]{"one", "two", "three"});

    private Class type
    private Object value

    // generic used only to check type - value correctness
    <T> SampleOptions(Class<T> type, T value) {
        this.type = type
        this.value = value
    }

    @Override
    public Class getType() {
        return type
    }

    @Override
    public Object getDefaultValue() {
        return value
    }
}
```

Each enum value declares option with exact type and default value. Option type is not limited, but implement proper toString for custom object used as option value.
This will require for pretty reporting, as simple toString used for option value (except collections and arrays are rendered as \[\]).

Now you can use option, for example, in bean:

```java
import static MyOptions.DoExtraWork;

public class MyBean {
    @Inject Options options;
    
    pulic void someMethod() {
        ... 
        if (options.get(DoExtraWork)) {
            // extra work impl
        }
    }
}
```

To provide custom option value:

```java
    GuiceBundle.builder()
        .option(DoExtraWork, false)
        ...
```

#### Options lookup

There is no lookup mechanism implementation, provided by default (for example, like bundles lookup mechanism)
because it's hard to do universal implementation considering wide range of possible values.

But you can write your own lookup, simplified for your case.

If you do, you can use `.options(Map<Enum, Object>)` method to set resolved options (note that contract simplified for just
Enum, excluding Option for simpler usage, but still only option enums must be provided).

```java
    GuiceBundle.builder()
        .options(new MyOptionsLookup().getOptions())
        ...
```

Such mechanism could be used, for example, to change application options in tests or to apply environment specific options.

### Diagnostic info

During startup guicey records main action timings and configuration process details. All this 
information is accessible through [GuiceyConfigurationInfo](src/main/java/ru/vyarus/dropwizard/guice/module/GuiceyConfigurationInfo.java).
This guice bean is always registered and available for injection.  

Provided DiagnosticBundle (see above) is an example of collected info representation. It may serve as api usage examples.

Example of recorded timings report:

```
    GUICEY started in 453.3 ms
    │   
    ├── [0,88%] CLASSPATH scanned in 4.282 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │   
    ├── [4,2%] COMMANDS processed in 19.10 ms
    │   └── registered 2 commands
    │   
    ├── [6,4%] BUNDLES processed in 29.72 ms
    │   ├── 2 resolved in 8.149 ms
    │   └── 6 processed
    │   
    ├── [86%] INJECTOR created in 390.3 ms
    │   ├── installers prepared in 13.79 ms
    │   │   
    │   ├── extensions recognized in 9.259 ms
    │   │   ├── using 11 installers
    │   │   └── from 7 classes
    │   │   
    │   └── 3 extensions installed in 4.188 ms
    │   
    ├── [1,3%] HK bridged in 6.583 ms
    │   ├── using 2 jersey installers
    │   └── 2 jersey extensions installed in 660.9 μs
    │   
    └── [1,1%] remaining 5 ms
```

See complete [diagnostics demo](https://github.com/xvik/dropwizard-guicey/wiki/Diagnostic-info)

#### Guice injector creation timings

You will see in guicey timings that almost all time spent creating guice injector. 
To see some guice internal timings enable guice debug logs:

```
logging:
  loggers:
    com.google.inject.internal.util: DEBUG
```

Logs will be something like this:

```
DEBUG [2016-08-03 21:09:45,963] com.google.inject.internal.util.Stopwatch: Module execution: 272ms
DEBUG [2016-08-03 21:09:45,963] com.google.inject.internal.util.Stopwatch: Interceptors creation: 1ms
DEBUG [2016-08-03 21:09:45,965] com.google.inject.internal.util.Stopwatch: TypeListeners & ProvisionListener creation: 2ms
DEBUG [2016-08-03 21:09:45,966] com.google.inject.internal.util.Stopwatch: Scopes creation: 1ms
DEBUG [2016-08-03 21:09:45,966] com.google.inject.internal.util.Stopwatch: Converters creation: 0ms
DEBUG [2016-08-03 21:09:45,992] com.google.inject.internal.util.Stopwatch: Binding creation: 26ms
DEBUG [2016-08-03 21:09:45,992] com.google.inject.internal.util.Stopwatch: Module annotated method scanners creation: 0ms
DEBUG [2016-08-03 21:09:45,993] com.google.inject.internal.util.Stopwatch: Private environment creation: 1ms
DEBUG [2016-08-03 21:09:45,993] com.google.inject.internal.util.Stopwatch: Injector construction: 0ms
DEBUG [2016-08-03 21:09:46,170] com.google.inject.internal.util.Stopwatch: Binding initialization: 177ms
DEBUG [2016-08-03 21:09:46,171] com.google.inject.internal.util.Stopwatch: Binding indexing: 1ms
DEBUG [2016-08-03 21:09:46,172] com.google.inject.internal.util.Stopwatch: Collecting injection requests: 1ms
DEBUG [2016-08-03 21:09:46,179] com.google.inject.internal.util.Stopwatch: Binding validation: 7ms
DEBUG [2016-08-03 21:09:46,183] com.google.inject.internal.util.Stopwatch: Static validation: 4ms
DEBUG [2016-08-03 21:09:46,191] com.google.inject.internal.util.Stopwatch: Instance member validation: 8ms
DEBUG [2016-08-03 21:09:46,192] com.google.inject.internal.util.Stopwatch: Provider verification: 1ms
DEBUG [2016-08-03 21:09:46,201] com.google.inject.internal.util.Stopwatch: Static member injection: 9ms
DEBUG [2016-08-03 21:09:46,204] com.google.inject.internal.util.Stopwatch: Instance injection: 3ms
DEBUG [2016-08-03 21:09:46,427] com.google.inject.internal.util.Stopwatch: Preloading singletons: 223ms
```

NOTE: 'Preloading singletons' line will be logged long after other guice log messages, so search it at the end of your startup log.

### Admin REST

All rest resources could be "published" in the admin context too.  This is just an emulation of rest: the same resources 
are accessible in both contexts. On admin side special servlet simply redirects all incoming requests into jersey context.

Such approach is better than registering completely separate jersey context for admin rest because
of no overhead and simplicity of jersey extensions management.

To install admin rest servlet, register bundle:

```java
bootstrap.addBundle(new AdminRestBundle());
```

In this case rest is registered either to '/api/*', if main context rest is mapped to root ('/*')
or to the same path as main context rest.

To register on custom path:

```java
bootstrap.addBundle(new AdminRestBundle("/custom/*"));
```

##### Security

In order to hide specific resource methods or entire resources on main context, annotate resource methods
or resource class with `@AdminResource` annotation.

For example:

```java
@GET
@Path("/admin")
@AdminResource
public String admin() {
    return "admin"
}
```

This (annotated) method will return 403 error when called from main context and process when called from admin context.

This is just the simplest option to control resources access. Any other method may be used (with some security
framework or something else).

### Commands support

Automatic scan for commands is disabled by default. You can enable it using `searchCommands(true)` bundle option.
If search enabled, all classes extending `Command` are instantiated using default constructor and registered in bootsrap object.
`EnvironmentCommand` must have construction with `Application` argument.

You can use guice injections only in `EnvironmentCommand`'s because only these commands start bundles (and so launch guice context creation).

No matter if environment command was registered with classpath scan or manually in bootstrap, `injector.injectMembers(commands)` will be called on it
to inject guice dependencies.

### Jersey objects available for injection

The following objects available for injection:

* javax.ws.rs.core.Application
* javax.ws.rs.ext.Providers
* org.glassfish.hk2.api.ServiceLocator
* org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider

The following request-scope objects available for injection:

* javax.ws.rs.core.UriInfo
* javax.ws.rs.core.HttpHeaders
* javax.ws.rs.core.SecurityContext
* javax.ws.rs.core.Request
* org.glassfish.jersey.server.ContainerRequest
* org.glassfish.jersey.server.internal.process.AsyncContext

Note that it's not guice request scope, but HK request scope. So these objects may be resolved only inside resource method.
Event when guice servlets support disabled (`.noGuiceFilter()`) `HttpServletRequest` and `HttpServletResponse` may still be injected
using provider.

### Guice ServletModule support

By default, GuiceFilter is registered for both application and admin contexts. And so request and session scopes will be 
be available in both contexts. Also it makes injection of request and response objects available with provider (in any bean).

To register servlets and filters for main context use `ServletModule`, e.g.

```java
public class WebModule extends ServletModule {

    @Override
    protected void configureServlets() {
        filter("/*").through(MyFilter.class)
        serve("/myservlet").with(MyServlet.class)
    }
}
```

#### Request scoped beans

You can use request scoped beans in both main and admin contexts. 

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

#### Limitations

By default, GuiceFilter is registered with REQUEST dispatcher type. If you need to use other types use `GuiceyOptions.GuiceFilterRegistration` option:

```java
    .option(GuiceFilterRegistration, EnumSet.of(REQUEST, FORWARD))
```

Note that async servlets and filters can't be used with guice servlet module (and so it is impossible to register GuiceFilter for ASYNC type). 
Use web installers for such cases. 

GuiceFilter dispatch all requests for filters and servlets registered by ServletModule internally and there may be problems combining servlets from ServletModule
and filters in main scope.

#### Disable guice servlets

If you don't use servlet modules (for example, because web installers cover all needs) you can disable guice servlet modules support using `.noGuiceFilter()` method.

It will:
* Avoid registration of GuiceFilter in both contexts
* Remove request and session guice scopes support (because no ServletModule registered)
* Prevent installation of any ServletModule (error will be thrown indicating duplicate binding)
* HttpServletRequest and HttpServletResponse still may be injected in resources with Provider (but it will not be possible to use such injections in servlets, filters or any other place)

Also, disabling servlet module saves some startup time (~50ms). 
 
### Testing

Tests requires `'io.dropwizard:dropwizard-testing:1.0.5'` dependency.

For integration testing of guice specific logic you can use `GuiceyAppRule`. It works almost like 
[DropwizardAppRule](http://www.dropwizard.io/1.0.5/docs/manual/testing.html),
but doesn't start jetty (and so jersey and guice web modules will not be initialized). Managed and lifecycle objects 
supported.

```java
public class MyTest {

    @Rule
    GuiceyAppRule<MyConfiguration> RULE = new GuiceyAppRule<>(MyApplication.class, "path/to/configuration.yaml")
    
    public void testSomething() {
        RULE.getBean(MyService.class).doSomething();
        ...
    }
}
```

As with dropwizard rule, configuration is optional

```java
new GuiceyAppRule<>(MyApplication.class, null)
```

#### Spock

If you use [spock framework](http://spockframework.org) you can use spock specific extensions:
* `@UseGuiceyApp` - internally use `GuiceyAppRule`
* `@UseDropwizardApp` - internally use `DropwizardAppRule`

Both extensions allows using injections directly in specifications (like spock-guice).

##### UseGuiceyApp

```groovy
@UseGuiceyApp(MyApplication)
class AutoScanModeTest extends Specification {

    @Inject MyService service

    def "My service test" {
        when: 'calling service'
        def res = service.getSmth()
        then: 'correct result returned'
        res == 'hello'
    }
```

Annotation allows you to configure the same things as rules does: application class, configuration file (optional),
configuration overrides.

```groovy
@UseGuiceyApp(value = MyApplication,
    config = 'path/to/my/config.yml',
    configOverride = [
            @ConfigOverride(key = "foo", value = "2"),
            @ConfigOverride(key = "bar", value = "12")
    ])
class ConfigOverrideTest extends Specification {
```

As with rules, `configOverride` may be used without setting config file (simply to fill some configurations)

##### UseDropwizardApp

For complete integration testing (when web part is required):

```groovy
@UseDropwizardApp(MyApplication)
class WebModuleTest extends Specification {

    @Inject MyService service

    def "Check web bindings"() {

        when: "calling filter"
        def res = new URL("http://localhost:8080/dummyFilter").getText()
        then: "filter active"
        res == 'Sample filter and service called'
        service.isCalled()
```

Annotation supports the same configuration options as `@UseGuiceyApp` (see above)

##### Spock extensions details

Extensions follow spock-guice style - application started once for all tests in class. It's the same as using rule with
`@ClassRule` annotation. Rules may be used with spock too (the same way as in junit), but don't mix them with
annotation extensions.

To better understand how injections works, see [this test](src/test/groovy/ru/vyarus/dropwizard/guice/test/InjectionTest.groovy)
Also, look other tests - they all use spock extensions.

There are two limitations comparing to rules:
* Application can't be created for each test separately (like with `@Rule` annotation). This is because of `@Shared` instances support.
* You can't customize application creation: application class must have no-args constructor (with rules you can extend rule class
and override `newApplication` method). But this should be rare requirement.

### Jersey guice integration

Jersey2 guice integration is much more complicated, because of [HK2](https://hk2.java.net/2.4.0-b34/introduction.html) container, used by jersey.

Guice integration done in guice exclusive way as much as possible: everything should be managed by guice and invisibly integrated into HK2.
Anyway, it is not always possible to hide integration details, especially if you need to register jersey extensions.

Lifecycle:
* Guice context starts first. This is important for commands support: command did not start jersey and so jersey related extensions will not be activated,
still core guice context will be completely operable.
* Guice context includes special module with [jersey related bindings](src/main/java/ru/vyarus/dropwizard/guice/module/jersey/hk2/GuiceBindingsModule.java).
These bindings are lazy (it's impossible to resolve them before jersey will start). So if these dependencies are used in singleton beans, they must be wrapped with `Provider`.
* [Guice feature](src/main/java/ru/vyarus/dropwizard/guice/module/jersey/GuiceFeature.java) registered in jersey.
It will bind guice specific extensions into jersey, when jersey starts.
* [JerseyInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/install/JerseyInstaller.java)
installer type is called to bind extensions into HK2 context.
This binding is done, using HK `Factory` as much as possible to make definitions lazy and delay actual creation.
* HK [guice-bridge](https://hk2.java.net/2.4.0-b34/guice-bridge.html) is also registered (not bi-directional) (but, in fact, this bridge is not required).
Bridge adds just injection points resolution for hk managed beans, and not bean resolution. Anyway, this may be useful in some cases.

So when guice context is created, jersey context doesn't exist and when jersey context is created it doesn't aware of guice existence.
But, `JerseyInstaller` installs HK bindings directly in time of hk context creation, which allows to workaround HK's lack of guice knowledge.
Extensions on both sides must be registered lazily (using `Factory` and `Provider`).
Special [utility](src/main/java/ru/vyarus/dropwizard/guice/module/installer/util/JerseyBinding.java)
helps with this.

The problems may appear with binding of jersey extensions.
Good example is `ValueFactoryProvider`. Most likely you will use `AbstractValueFactoryProvider` as base class, but it declares
direct binding for `MultivaluedParameterExtractorProvider`. So such bean would be impossible to create eagerly in guice context.

There are two options to solve this:
* use `@LazyBinding`: bean instance will not be created together with guice context (when `MultivaluedParameterExtractorProvider` is not available),
and creation will be initiated by HK, when binding could be resolved.
* or use `@HK2Managed` this will delegate instance management to HK, but still guice specific extensions may be used.

In other cases simply wrap jersey specific bindings into `Provider`.

Note, that current integration could be extended: you can write custom installer in order to register additional types into HK directly.
On guice side you can register additional bindings for jersey components the same way as in
[jersey bindings module](src/main/java/ru/vyarus/dropwizard/guice/module/jersey/hk2/GuiceBindingsModule.java)

If you just want to add some beans in HK context, annotate such beans with `@Provider` and `@HK2Managed` - provider
will be recognized by installer and hk managed annotation will trigger simple registration (overall it's the same
as write binding manually).

### Writing custom installer

Installer should implement [FeatureInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/FeatureInstaller.java)
interface. It will be automatically registered if auto scan is enabled. To register manually use `.installers()` bundle option.

Installer `matches` method implements feature detection logic. You can use `FeatureUtils` for type checks, because it's denies
abstract classes. Method is called for classes found during scan to detect installable features and for classes directly specified
with `.beans()` bundle option to detect installer.

Three types of installation supported. Installer should implement one or more of these interfaces:
* `BindingInstaller` allows custom guice bindings. If installer doesn't implement this interface sinmple `bind(type)` will be called to register in guice.
* `TypeInstaller` used for registration based on type (no instance created during installation).
* `InstanceInstaller` used for instance registration. Instance created using `injector.getInstance(type)`.
* `JerseyInstaller` used for registration of bindings in HK context.

Note that extensions may use `@LazyBinding` annotation. In general case such extensions will not be registered in guice.
In case of `BindingInstaller`, special hint will be passed and installer should decide how to handle it (may throw exception as not supported).

`BindingInstaller` called in time of injector creation, whereas `TypeInstaller` and `InstanceInstaller` are called just after injector creation.
`JerseyInstaller` is called on jersey start.

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
public class CustomInstaller implements FeatureInstaller<CustomFeature>, JerseyInstaller<CustomFeature> {
    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, CustomFeature.class);
    }
    
    @Override
    public void install(final AbstractBinder binder, final Class<CustomFeature> type) {
        JerseyBinding.bindComponent(binder, type);
    }
    
    @Override
    public void report() {
    }
}
```

#### Ordering

In order to support ordering, installer must implement `Ordered` interface.
If installer doesn't implement it extensions will not be sorted, even if extensions has `@Order` annotations. 

As example, see [ManagedInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)

#### Reporting

Installers `report()` method will be called after it finish installation of all found extensions. Report provides
user visibility of installed extensions. 

To simplify reporting use predefined [Reporter](src/main/java/ru/vyarus/dropwizard/guice/module/installer/util/Reporter.java) class. 
See example usage in [ManagedInstaller](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)

```
INFO  [2016-08-21 23:49:49,534] ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller: managed =

    (ru.vyarus.dropwizard.guice.support.feature.DummyManaged)
```

For complex cases, reporter may be extended to better handle installed extensions. As examples see 
[plugin installer reporter](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/plugin/PluginReporter.java)
and [provider installer reporter](src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/provider/ProviderReporter.java)

```
INFO  [2016-08-21 23:49:49,535] ru.vyarus.dropwizard.guice.module.installer.feature.plugin.PluginInstaller: plugins =

    Set<PluginInterface>
        (ru.vyarus.dropwizard.guice.support.feature.DummyPlugin1)
        (ru.vyarus.dropwizard.guice.support.feature.DummyPlugin2)

    Map<DummyPluginKey, PluginInterface>
        ONE        (ru.vyarus.dropwizard.guice.support.feature.DummyNamedPlugin1)
        TWO        (ru.vyarus.dropwizard.guice.support.feature.DummyNamedPlugin2)
```

### Might also like

* [generics-resolver](https://github.com/xvik/generics-resolver) - runtime generics resolution
* [guice-validator](https://github.com/xvik/guice-validator) - hibernate validator integration for guice 
(objects validation, method arguments and return type runtime validation)
* [guice-ext-annotations](https://github.com/xvik/guice-ext-annotations) - @Log, @PostConstruct, @PreDestroy and
utilities for adding new annotations support
* [guice-persist-orient](https://github.com/xvik/guice-persist-orient) - guice integration for orientdb
* [dropwizard-orient-server](https://github.com/xvik/dropwizard-orient-server) - embedded orientdb server for dropwizard

-
[![java lib generator](http://img.shields.io/badge/Powered%20by-%20Java%20lib%20generator-green.svg?style=flat-square)](https://github.com/xvik/generator-lib-java)
