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