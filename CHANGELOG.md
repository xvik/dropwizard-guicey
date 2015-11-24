### 3.1.1 (2015-11-24)
* Exit on guice injector creation error. Contributed by [Adam Dougal](https://github.com/adamdougal)
* Add classpath scan packages validation for intersection (to prevent duplicate instances)

### 3.1.0 (2015-09-06)
* JerseyProviderInstaller: 
  - add support for: ParamConverterProvider, ContextResolver, MessageBodyReader, MessageBodyWriter, ReaderInterceptor, WriterInterceptor,
  ContainerRequestFilter, ContainerResponseFilter, DynamicFeature, ApplicationEventListener
  - support multiple extension interfaces on the same bean
* Introduce bundles (GuiceyBundle) to simplify extensions:
  - core installers now registered with CoreInstallersBundle and classpath scan on core installers package is removed
  - builder bundles() method to add guicey bundles
  - builder configureFromDropwizardBundles method enables all registered dropwizard bundles lookup if they implement GuiceyBundle (unified extension mechanism)
* Add admin context rest support (AdminRestBundle)
* Add request scoped beans support in admin context

### 3.0.1 (2015-07-04)
* Add DropwizardAwareModule abstract module to remove boilerplate of using all aware interfaces

### 3.0.0 (2015-04-26)
* Fix HealthCheckInstaller: now installs only NamedHealthCheck classes and ignore other HealthCheck types (which it can't install properly) 
* (breaking) Remove static state from GuiceBundle:
  - GuiceBundle.getInjector method remain, but its now instance specific (instead of static)
  - Injector could be referenced statically using application instance: InjectorLookup.getInjector(app).get() 
  - JerseyInstaller interface signature changed: now install method receives injector instance

### 2.2.0 (2015-04-17)
* Fix ExceptionMapper registration
* Add installers ordering support with @Order annotation. Default installers are ordered now with indexes from 10 to 100 with gap 10 
(to simplify custom installers injection between them)

### 2.1.2 (2015-03-03)
* Spock 1.0 compatibility

### 2.1.1 (2015-01-25)
* Dropwizard 0.8-rc2 compatibility

### 2.1.0 (2015-01-04)
* Add ability to customize injector creation (required by some guice third party modules, like governator). Contributed by [Nicholas Pace](https://github.com/segfly)
* Add spock extensions to use injections directly in specification (like spock-guice do)

### 2.0.0 (2014-11-25)
* Dropwizard 0.8 integration (as result, no more depends on jersey-guice, but depends on guice-bridge(hk2)).
Jersey integration completely rewritten.
* Add JerseyInstaller installer type
* Add @LazyBinding annotation, which allows extension not to be registered in guice context (it will be created on first request)

### 1.1.0 (2014-10-23)
* Fix interface generics resolution to support nested generics and moved generics resolution into GenericsUtils instead of FeatureUtils
* Drop java 1.6 compatibility, because dropwizard is 1.7 compatible
* Add junit rule for lightweight testing (run guice without starting jetty)

### 1.0.0 (2014-10-14)
* Add dependency on guice-multibindings
* Installers may choose now from three types of installation (binding, type or instance) or combine them.
* Add PluginInstaller: shortcut for multibindings mechanism
* Updated guice (4.0.beta4 -> 4.0.beta5)
* Force singleton for resources
* @Eager renamed to @EagerSingleton and now forces singleton scope for bean
* Add dropwizard style reporting for installed features (like resources or tasks)
* Removed JerseyInjectableProviderInstaller. Now injectable providers must be annotated with @Provider
* Add extensions ordering support using @Order annotation (by default for LifeCycle and Managed installers)
* Add admin context filter and servlet installers
* Rename bundle options: features -> installers, disabledFeatures -> disabledInstallers, beans -> extensions

### 0.9.0 (2014-09-05)

* Initial release