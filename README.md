#Dropwizard guice integration
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/xvik/dropwizard-guicey)
[![License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)
[![Build Status](http://img.shields.io/travis/xvik/dropwizard-guicey.svg?style=flat&branch=master)](https://travis-ci.org/xvik/dropwizard-guicey)
[![Coverage Status](https://img.shields.io/coveralls/xvik/dropwizard-guicey.svg?style=flat)](https://coveralls.io/r/xvik/dropwizard-guicey?branch=master)

### About

[Dropwizard](http://dropwizard.io/) integration based on ideas from [dropwizard-guice](https://github.com/HubSpot/dropwizard-guice) and 
[dropwizardy-guice](https://github.com/jclawson/dropwizardry/tree/master/dropwizardry-guice) 
(which was derived from first one).

Features:
* Guice injector created on run phase (now both dropwizard-guice and dropwizardry-guice do the same)
* Flexible HK2 integration
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
* Admin context rest emulation
* Custom junit rule for lightweight integration testing
* [Spock](http://spockframework.org) extensions

### Thanks to

* [Sébastien Boulet](https://github.com/gontard) ([intactile design](http://intactile.com)) for very useful feedback
* [Nicholas Pace](https://github.com/segfly) for governator integration

### Setup

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) (package appear immediately after release) 
and then to maven central (require few days after release to be published). 

[![Download](https://api.bintray.com/packages/vyarus/xvik/dropwizard-guicey/images/download.svg) ](https://bintray.com/vyarus/xvik/dropwizard-guicey/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus/dropwizard-guicey.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/dropwizard-guicey)

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>dropwizard-guicey</artifactId>
  <version>3.1.0</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus:dropwizard-guicey:3.1.0'
```

for dropwizard 0.7 use version 1.1.0 (see [old docs](https://github.com/xvik/dropwizard-guicey/tree/dw-0.7))

##### Snapshots

You can use snapshot versions through [JitPack](https://jitpack.io):

* Go to [JitPack project page](https://jitpack.io/#xvik/dropwizard-guicey)
* Select `Commits` section and click `Get it` on commit you want to use (top one - the most recent)
* Follow displayed instruction: add repository and change dependency (NOTE: due to JitPack convention artifact group will be different)

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

After application start, look application log for dropwizard style extension installation reports.

Bundle options:
* `injectorFactory` sets custom injector factory (see below)
* `enableAutoConfig` enables auto scan on one or more packages to scan. If not set - no auto scan will be performed and default installers will not be available.
* `searchCommands` if true, command classes will be searched in classpath and registered in bootstrap object. 
Auto scan must be enabled. By default commands scan is disabled (false), because it may be not obvious.
* `modules` one or more guice modules to start. Not required: context could start even without custom modules.
* `disableInstallers` disables installers, found with auto scan. May be used to override default installer or disable it.
Note: when auto scan not enabled no installers will be registered automatically.
* `installers` registers feature installers. Used either to add installers from packages not visible by auto scan or to
configure installers when auto scan not used.
* `extensions` manually register classes (for example, when auto scan disabled). Classes will be installed using configured installers.
* `bundles` registers guicey bundles (see below)
* `configureFromDropwizardBundles` enables registered dropwizard bundles lookup if they implement `GuiceyBundle` (false by default)
* `build` allows specifying guice injector stage (production, development). By default, PRODUCTION stage used.

#### Using custom injector factory

Some Guice extension libraries require injector created by their API.
You can control injector creation with custom `InjectorFactory` implementation.

For example, to support [governator](https://github.com/Netflix/governator):

```java
public class GovernatorInjectorFactory implements InjectorFactory {
    @Override
    public Injector createInjector(final Stage stage, final Iterable<? extends Module> modules) {
        return LifecycleInjector.builder().withModules(modules).build().createInjector();
    }
}
```

Configure custom factory in bundle:

```java
@Override
void initialize(Bootstrap<TestConfiguration> bootstrap) {
    bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
            .injectorFactory(new GovernatorInjectorFactory())
            .enableAutoConfig("package.to.scan")
            .modules(new MyModule())
            .build()
    );
}
```

[Read more about governator integration](https://github.com/xvik/dropwizard-guicey/wiki/Governator-Integration)

#### Injector instance

In some cases it may be important to get injector instance outside of guice context.

Injector instance could be resolved with:
* getInjector method on GuiceBundle instance (note that injector initialized on run phase, and NPE will be thrown if injector not initialized)
* InjectorLookup.getInjector(app).get() static call using application instance (lookup returns Optional and last get() throws exception or returns injector instance).

If you need lazy injector reference, you can use `InjectorProvider` class (its actually `Provider<Injector>`):

```java
InjectorProvider provider = new InjectorProvider(app);
// somewhere after run phase
Injector injector = provider.get();
```

Most likely, requirement for injector instance means integration with some third party library.
Consider writing custom installer in such cases (it will eliminate need for injector instance).

##### Authentication

Authentication is a good case when injector is required externally:

```java
@Override
public void run(ExampleConfiguration configuration, Environment environment) {
    environment.jersey().register(AuthFactory.binder(new BasicAuthFactory<String>(
                          InjectorLookup.getInjector(this).get().getInstance(SimpleAuthenticator.class),
                          "SUPER SECRET STUFF",
                          User.class)));
}                         
```

For more details see [wiki page](https://github.com/xvik/dropwizard-guicey/wiki/Authentication-integration)

### Classpath scan

Classpath scanning is activated by specifying packages to scan in bundle `.enableAutoConfig("package.to.scan")`.

When auto scan enabled:
* Feature installers searched in classpath (including default installers): classes implementing `FeatureInstaller`.
Without auto scan default installers not registered.
* Search for features in classpath using `FeatureInstaller#matches` method.
* If commands search enabled `.searchCommands(true)`, performs search for all classes extending `Command` and install them into
bootstrap.

Classes are searched in specified packages and all their subpackages. 
[Inner static](https://github.com/xvik/dropwizard-guicey/blob/master/src/test/groovy/ru/vyarus/dropwizard/guice/cases/innercls/AbstractExceptionMapper.groovy) 
classes are also resolved.

`@InvisibleForScanner` annotation hides class from scanner (for example, to install it manually or to avoid installation at all)

### Module autowiring

Because guice modules are registered in init section, it's not possible to get reference for environment and configuration objects.
To overcome this limitation, you can implement `BootstrapAwareModule`, `EnvironmentAwareModule` or `ConfigurationAwareModule`
interfaces and reference object will be set to module just before injector creation (allowing you to use it during module configuration).

This will work only for modules set to `modules()` bundle option.

[Example](https://github.com/xvik/dropwizard-guicey/blob/master/src/test/groovy/ru/vyarus/dropwizard/guice/support/AutowiredModule.groovy)

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

Some installers support extensions ordering (managed, lifecycle and admin servlet and filters).
To define extensions order use `@Order` annotation. Extensions sorted naturally (e.g. `@Order(1)` before `@Order(2)`).
Extensions without annotation goes last.

### Installers

Installer is a core integration concept: every extension point has it's own installer. Installers used for both auto scan and manual modes
(the only difference is in manual mode classes specified manually).
Installers itself are resolved using classpath scanning, so it's very easy to add custom installers (and possibly override default one by disabling it and registering alternative).

All default installers could be found [here](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature)

When installer recognize class, it binds it into guice `binder.bind(foundClass)` (or bind by installer if it support binding).
But extensions annotated with `@LazyBinding` are not bind to guice context. This may be useful to delay bean creation:
by default, guice production stage will instantiate all registered beans.

On run phase (after injector created) all found or manually provided extensions are installed by type or instantiated (`injector.getInstance(foundClass)`) and passed to installer 
to register extension within dropwizard (installation type is defined by installer).

Installers order is defined by `@Order` annotation. Default installers are ordered with indexes from 10 to 100 with gap 10.
If you need to run your installer before/after some installer simply annotate it with `@Order`. Installers without annotation goes last.

##### Resource
[ResourceInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/ResourceInstaller.java)
finds classes annotated with `@Path` and register their instance as resources. Resources **registered as singletons**, even if guice bean scope isn't set. If extension annotated as
`@HK2Managed` then jersey HK container will manage bean creation (still guice beans injections are possible).

Use `Provider` for [request scoped beans](#request-scoped-beans) injections.

##### Task
[TaskInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/TaskInstaller.java)
finds classes extending `Task` class and register their instance in environment.

##### Managed
[ManagedInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)
finds classes implementing `Managed` and register their instance in environment. Support ordering.


##### Lifecycle
[LifeCycleInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/LifeCycleInstaller.java)
finds classes implementing jetty `LifeCycle` interface and register their instance in environment. Support ordering.


##### Health
[HealthCheckInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/health/HealthCheckInstaller.java)
finds classes extending `NamedHealthCheck` class and register their instance in environment.

Custom base class is required, because default `HealthCheck` did not provide check name, which is required for registration.


##### Jersey extension
[JerseyProviderInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/provider/JerseyProviderInstaller.java)
finds classes annotated with jersey `@Provider` annotation and register their instance in jersey (**forced singleton**). Supports the following extensions:
like [Factory](https://github.com/xvik/dropwizard-guicey/blob/master/src/test/groovy/ru/vyarus/dropwizard/guice/support/provider/LocaleInjectableProvider.groovy),
[ExceptionMapper](https://github.com/xvik/dropwizard-guicey/tree/master/src/test/groovy/ru/vyarus/dropwizard/guice/support/feature/DummyExceptionMapper.groovy),
[InjectionResolver](https://github.com/xvik/dropwizard-guicey/blob/master/src/test/groovy/ru/vyarus/dropwizard/guice/support/provider/annotated/AuthInjectionResolver.groovy),
[ValueFactoryProvider](https://github.com/xvik/dropwizard-guicey/blob/master/src/test/groovy/ru/vyarus/dropwizard/guice/support/provider/annotated/AuthFactoryProvider.groovy),
[ParamConverterProvider](https://github.com/xvik/dropwizard-guicey/blob/master/src/test/groovy/ru/vyarus/dropwizard/guice/support/provider/paramconv/FooParamConverter.groovy), 
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


##### Eager
[EagerSingletonInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/eager/EagerSingletonInstaller.java)
finds classes annotated with `@EagerSingleton` annotation and register them in guice injector. It is equivalent of eager singleton registration
`bind(type).asEagerSingleton()`.

This installer doesn't relate to dropwizard directly, but useful in case when you have bean not injected by other beans (so guice can't register
it automatically). Normally you would have to manually register it in module.

Most likely such bean will contain initialization logic. 

May be used in conjunction with @PostConstruct annotations (e.g. using [ext-annotations](https://github.com/xvik/guice-ext-annotations)):
installer finds and register bean and post construct annotation could run some logic. Note: this approach is against guice philosophy and should
be used for quick prototyping only.


##### Plugin
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

##### Admin filter
[AdminFilterInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/admin/AdminFilterInstaller.java)
installs filters annotated with `@AdminFilter` into administration context. Support ordering.


##### Admin servlet
[AdminServletInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/admin/AdminServletInstaller.java)
installs servlets annotated with `@AdminServlet` into administration context. Support ordering.

### Guicey bundles

By analogy with dropwizard bundles, guicey has its own `GuiceyBundle`. These bundles contains almost the same options as 
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

Or, if auto-scan is not used, bundles may be used to group application features: e.g. ResourcesBundle, TasksBundle.

##### Core bundle

Core installers are grouped into `CoreInstallersBundle`.
If classpath scan is not active, all core installers may be registered like this:

```java
bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
        .bundles(new CoreInstallersBundle())
        .extensions(MyTask.class, MyResource.class, MyManaged.class)
        .build()
);
```

##### Dropwizard bundles unification

Guicey bundles and dropwizard bundles may be unified providing single (standard) extension point for both 
dropwizard and guicey features.

Feature is disabled by default, to enable it use `configureFromDropwizardBundles` option.

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

### Servlets and filters

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

### Commands support

Automatic scan for commands is disabled by default. You can enable it using `searchCommands(true)` bundle option.
If search enabled, all classes extending `Command` are instantiated using default constructor and registered in bootsrap object.
`EnvironmentCommand` must have construction with `Application` argument.

You can use guice injections only in `EnvironmentCommand`'s because only these commands start bundles (and so launch guice context creation).

No matter if environment command was registered with classpath scan or manually in bootstrap, `injector.injectMembers(commands)` will be called on it
to inject guice dependencies.

### Request scoped beans

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

#### Jersey objects available for injection

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

### Testing

Tests requires `'io.dropwizard:dropwizard-testing:0.8.0-rc1'` dependency.

For integration testing of guice specific logic you can use `GuiceyAppRule`. It works almost like 
[DropwizardAppRule](https://dropwizard.github.io/dropwizard/manual/testing.html),
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

To better understand how injections works, see [this test](https://github.com/xvik/dropwizard-guicey/blob/master/src/test/groovy/ru/vyarus/dropwizard/guice/test/InjectionTest.groovy)
Also, look other tests - they all use spock extensions.

There are two limitations comparing to rules:
* Application can't be created for each test separately (like with `@Rule` annotation). This is because of `@Shared` instances support.
* You can't customize application creation: application class must have no-args constructor (with rules you can extend rule class
and override `newApplication` method). But this should be rare requirement.

### Jersey guice integration

Jersey2 guice integration is much more complicated, because of [HK2](https://hk2.java.net/2.4.0-b06/introduction.html) container, used by jersey.

Guice integration done in guice exclusive way as much as possible: everything should be managed by guice and invisibly integrated into HK2.
Anyway, it is not always possible to hide integration details, especially if you need to register jersey extensions.

Lifecycle:
* Guice context starts first. This is important for commands support: command did not start jersey and so jersey related extensions will not be activated,
still core guice context will be completely operable.
* Guice context includes special module with [jersey related bindings](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/jersey/hk2/GuiceBindingsModule.java).
This bindings are lazy (it's impossible to resolve them before jersey will start). So if these dependencies are used in singleton beans, they must be wrapped with `Provider`.
* [Guice feature](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/jersey/GuiceFeature.java) registered in jersey.
It will bind guice specific extensions into jersey, when jersey starts.
* [JerseyInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/install/JerseyInstaller.java)
installer type is called to bind extensions into HK2 context.
This binding is done, using HK `Factory` as much as possible to make definitions lazy and delay actual creation.
* HK [guice-bridge](https://hk2.java.net/2.4.0-b06/guice-bridge.html) is also registered (not bi-directional) (but, in fact, this bridge is not required).
Bridge adds just injection points resolution for hk managed beans, and not bean resolution. Anyway, this may be useful in some cases.

So when guice context is created, jersey context doesn't exist and when jersey context is created it doesn't aware of guice existence.
But, `JerseyInstaller` installs HK bindings directly in time of hk context creation, which allows to workaround HK's lack of guice knowledge.
Extensions on both sides must be registered lazily (using `Factory` and `Provider`).
Special [utility](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/util/JerseyBinding.java)
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
[jersey bindings module](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/jersey/hk2/GuiceBindingsModule.java)

If you just want to add some beans in HK context, annotate such beans with `@Provider` and `@HK2Managed` - provider
will be recognized by installer and hk managed annotation will trigger simple registration (overall it's the same
as write binding manually).

### Writing custom installer

Installer should implement [FeatureInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/FeatureInstaller.java)
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

As example, see [ManagedInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)

#### Reporting

Installers `report()` method will be called after it finish installation of all found extensions. Report provides
user visibility of installed extensions. 

To simplify reporting use predefined [Reporter](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/util/Reporter.java) class. 
See example usage in [ManagedInstaller](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/ManagedInstaller.java)

For complex cases, reporter may be extended to better handle installed extensions. As examples see 
[plugin installer reporter](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/plugin/PluginReporter.java)
and [provider installer reporter](https://github.com/xvik/dropwizard-guicey/blob/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/provider/ProviderReporter.java)

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
