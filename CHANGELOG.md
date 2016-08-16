* Update to dropwizard 1.0.0
* (breaking) Remove AdminServletInstaller and AdminFilterInstaller (replaced with new web bundle)
* Add WebInstallersBundle (not installed by default) to install servlet and filters in both main and admin contexts:
    - WebFilterInstaller installs filters annotated with java.servlet.annotation.WebFilter
    - WebServletInstaller installs servlets annotated with java.servlet.annotation.WebServlet
    - WebListenerInstaller installs filters annotated with java.servlet.annotation.WebListener    
* Add general options mechanism. Used to generify core guicey options, provide runtime options access (for bundles and reporting) and allow 3rd party bundles use it's own low-level options.
    - GuiceBundle option(option, value) method used to specify options
    - GuiceyBootstrap option(option) method provides access to defined options from bundles
    - Options guice bean provide access to options from guice services
    - OptionsInfo guice bean used for accessing options metadata (also accessible through GuiceyConfigurationInfo.getOptions())
    - Options reporting added to DiagnosticBundle
* (breaking) remove GuiceBunldle methods: searchCommands(boolean), configureFromDropwizardBundles(boolean), bindConfigurationInterfaces(boolean) 
    (use either shortcuts without parameters or generic options method instead)
* (breaking) core installers bundle now always installed (for both auto scan and manual modes)
    - GuiceBundle noDefaultInstallers() shortcut method or GuiceyOptions.UseCoreInstallers option directly may be used to disable installation
* Add methods to GuiceBundle builder:
    - printAvailableInstallers() - diagnostic reporting configured to show only available installers (to easily spot available features)
    
    
### 3.3.0 (2016-08-02)
* Update to guice 4.1.0
* Update to dropwizard 0.9.3
* Grant compatibility with guice options: disableCircularProxies, requireExactBindingAnnotations and requireExplicitBindings
* ResourceInstaller looks for @Path on directly implemented interface (#10)
* Fix bundles lookup reporting (correct multiline)
* Fix duplicate extensions installation when registered both manually and by auto scan
* Restrict extension installation to one installer (first matching, according to installers order)
* Improve dropwizard configuration class binding:
    - Complete configuration hierarchy bound (root, all classes between root and Configuration and Configuration itself)
    - (optional) Bind interfaces directly implemented by classes in configuration hierarchy except interfaces from java and groovy packages 
 (it's common to use HasSomeConfig interface convention and now interface may be directly used for binding (when bindConfigurationInterfaces()))
* Add GuiceyBootstrap methods (extend GuiceyBundle abilities):
    - bundles(): add transitive guicey bundles support (to install other guicey bundles from bundle). Duplicate bundles are detected by type.
    - application(): returns current application instance
* Rewrite internal configuration mechanism (bundles, installers etc) to generalize it and introduce complete configuration tracking: store registration sources, disabling, used installers and other specific information for each item
    - Add GuiceyConfigurationInfo service to access tracked guicey configuration information (may be used for configuration diagnostic purposes, performing post configuration checks, printing complete configuration tree etc)
    - Add DiagnosticBundle to log configuration items diagnostic information. Log format is configurable. Rendering is externalized and may be re-used (e.g. for web page). 
* Add GuiceBundle builder configuration options:
    - bindConfigurationInterfaces() to enable configuration interface bindings
    - strictScopeControl() is shortcut to enable HK2DebugBundle (to control beans creation scope during development and tests)
    - printDiagnosticInfo() is shortcut to enable DiagnosticBundle with default preset (enable diagnostic logs)
    - shortcut methods for disabled boolean options: searchCommands(), configureFromDropwizardBundles() and bindConfigurationInterfaces()

NOTE: if used FeaturesHolder (internal api bean), now it's renamed to ExtensionsHolder to force upgrade: use new GuiceyConfigurationInfo bean instead (public api)    
    
### 3.2.0 (2016-01-23)
* Clear possible duplicate guicey bundle instances
* Add GuiceyBundleLookup to automatically resolve and install guicey bundles from various sources.
    - Default: check 'guicey.bundles' system property and install bundles described there. May be useful for tests to enable debug bundles.
    - Default: use ServiceLoader mechanism to load declared GuiceyBundle services. Useful for automatic loading of third party extensions.
    - Add builder bundleLookup method to register custom lookup implementation
    - Add builder disableBundleLookup to disable default lookups
    - Default lookup implementation logs all resolved bundles
* Fix JerseyProviderInstaller: prevent hk beans duplicate instantiations; fix DynamicFeature support.
* Add HK2DebugBundle. When enabled, checks that beans are instantiated by guice only and annotated with @HK2Managed 
are managed by HK2 only. May be used in tests as extra validation.
* Add JerseyFeatureInstaller (included in code bundle) which installs javax.ws.rs.core.Feature. Useful for low level configuration. 
* Update to dropwizard 0.9
* Revert system exit on guice injector creation error (added in 3.1.1)

### 3.1.1 (2015-11-24)
* Exit on guice injector creation error.
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
