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

### 0.9.0 (2014-09-05)

* Initial release