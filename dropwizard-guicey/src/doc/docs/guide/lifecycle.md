# Guicey lifecycle

!!! tip
    Guicey broadcasts [events](events.md) at all major points.
    You can see most of them with enabled [lifecycle report](diagnostic/lifecycle-report.md).

## Configuration phase

!!! note
    All manual registrations must be performed under this phase (the only exception is
    Guice modules). All bundles are registered and initialized only under the configuration phase.

Everything below happens during the bundle registration call:

```java
@Override
public void initialize(Bootstrap<Configuration> bootstrap) {
    bootstrap.addBundle(GuiceBundle.builder()
            ...
            .build());

    // everything here will be called after all steps below
}
```  

* [Main bundle](configuration.md#main-bundle) configuration
* Apply [configuration hooks](hooks.md)
* All [option](options.md) values are set and can't be modified anymore.
* Apply registered [Dropwizard bundles](bundles.md#dropwizard-bundles) (initialization is delayed to account for
Dropwizard bundle [disables](disables.md#disable-dropwizard-bundles)).
* Perform a [classpath scan](scan.md) (if configured). The scan resolves all classes in configured packages to use
them later for detection.
* Perform [bundles lookup](bundles.md#bundle-lookup)
* Initialize [Guicey bundles](bundles.md#guicey-bundles)
* Search [for commands](commands.md#automatic-installation) (if classpath scanning is enabled)
* Prepare [installers](installers.md):
    - Detect installers with classpath scan (if configured)
    - Instantiate [not disabled](disables.md#disable-installers) installers
* Resolve [extensions](extensions.md):
    - Validate all [enabled](disables.md#disable-extensions) manually registered extensions:
    one of the prepared installers must recognize the extension or an error will be thrown.
    - Recognize extensions from classpath scan classes (if configured)

## Run phase

* Run [Guicey bundles](bundles.md#guicey-bundles)
    - Guice modules may be [registered here](bundles.md#guicey-bundles)
    - Extensions may still [be disabled](disables.md#disable-extensions)
* [Autowire modules](guice/module-autowiring.md)
* [Analyze enabled modules](guice/module-analysis.md)
    - Detect [extensions from bindings](guice/module-analysis.md#extensions-recognition)
    - Remove [disabled modules](guice/module-analysis.md#removed-bindings) and [disabled extensions](guice/module-analysis.md#disabled-extensions)
    - Re-package modules (to avoid duplicate module parsing by Guice)
    - Register `GuiceBootsrapModule`
    - Apply [overriding](guice/override.md) modules
* Create injector (with [injector factory](guice/injector.md#injector-factory))
    - `GuiceBootsrapModule` configures:
        * Additional [bindings](guice/bindings.md) (like [environment](guice/bindings.md#environment-binding), 
        [configuration](guice/bindings.md#configuration) and [jersey-objects](guice/bindings.md#jersey-specific-bindings))
        * Performs [extensions registration](guice/bindings.md#extension-bindings) (either default binding or specific,
        performed by `BindingInstaller`)
        * Register `GuiceFeature` (Jersey `Feature`), which will perform Jersey initialization
        * Activate [Guice ServletModule support](guice/servletmodule.md)
    - Since that moment, the injector can be [referenced statically](guice/injector.md#access-injector)
* Install extensions (except Jersey extensions)
* [Inject commands](commands.md#guice-injections)


!!! note
    As Dropwizard bundles were registered under `GuiceBundle` configuration, they will be run by Dropwizard
    after `GuiceBundle`.

!!! note
    Your `Application.run()` method will be called *after* Guicey startup, so you can [use the created
    injector](guice/injector.md#access-injector) there.

## Jersey startup

!!! note
    Jersey startup will initiate [HK2](hk2.md) context creation

* [Managed beans](../installers/managed.md) start
* [HK2](hk2.md) context creation activates `GuiceFeature` (registered earlier)
    - Apply the [Guice bridge](hk2.md#hk2-guice-bridge) (if required)
    - Run jersey specific installers ([resource](../installers/resource.md), [extension](../installers/jersey-ext.md)):
    installers will register the required bindings in the HK2 context

!!! note
    Any `EnvironmentCommand` did not start Jersey, so managed objects will not be started (but you can start the required
    services [manually](commands.md#environment-commands)). Also, all Jersey-related extensions will not be started.
    Still, the core Guice context will be completely operable.

!!! attention ""
    When the Guice context is created, *the Jersey context doesn't exist* and when the Jersey context is created *it isn't aware of Guice existence*.
