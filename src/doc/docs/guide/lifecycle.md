# Guicey lifecycle

!!! tip 
    Guicey broadcast [events](events.md) in all major points.
    You can see most of them with enabled [lifecycle report](diagnostic/lifecycle-report.md).     
    
## Configuration phase

!!! note
    All manual registrations must be performed under this phase (the only exception is 
    guice modules). All bundles are registered and initialized only under configuration phase.

Everything below happens under bundle registration call:

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
* All [option](options.md) values set and can't be modified anymore.
* Apply registered [dropwizard bundles](bundles.md#dropwizard-bundles) (init delayed to count 
dropwizard bundle [disables](disables.md#disable-dropwizard-bundles)).  
* Perform [classpath scan](scan.md) (if configured). Scan resolve all classes in configured packages to use 
them later for detection.  
* Perform [bundles lookup](bundles.md#bundle-lookup)
* Initialize [bundles](bundles.md#guicey-bundles) 
* Search [for commands](commands.md#automatic-installation) (if classpath scan enabled)
* Prepare [installers](installers.md):
    - Detect installers with classpath scan (if configured)
    - Instantiate [not diabled](disables.md#disable-installers) installers
* Resolve [extensions](extensions.md):
    - Validate all [enabled](disables.md#disable-extensions) manually registered extensions:
    one of prepared installers must recognize extension or error will be thrown.
    - Recognize extensions from classpath scan classes (if configured)              

## Run phase

* Run [bundles](bundles.md#guicey-bundles)
    - Guice modules may be [registered here](bundles.md#guicey-bundles)
    - Extensions may still [be disabled](bundles.md#optional-extensions)
* [Autowire modules](guice/module-autowiring.md)
* [Analyze modules](guice/module-analysis.md)
    - Detect [extensions from bindings](guice/module-analysis.md#extensions-recognition)
    - Remove [disabled modules](guice/module-analysis.md#removed-bindings) and [disabled extensions](guice/module-analysis.md#disabled-extensions)
    - Re-package modules (to avoid duplicate modules parsing by guice)
    - Register `GuiceBootsrapModule`  
    - Apply [overriding](guice/override.md) modules
* Create injector (with [injector factory](guice/injector.md#injector-factory))
    - `GuiceBootsrapModule` configures:
        * Additional [bindings](guice/bindings.md) (like [environment](guice/bindings.md#environment-binding), 
        [configuration](guice/bindings.md#configuration) and [jersey-objects](guice/bindings.md#jersey-specific-bindings))
        * Performs [extensions registration](guice/bindings.md#extension-bindings) (either default binding or specific, 
        performed by `BindingInstaller`)
        * Register `GuiceFeature` (jersey `Feature`), which will perform jersey initialization
        * Activate [guice ServletModule support](guice/servletmodule.md)   
    - Since that moment injector could be [referenced statically](guice/injector.md#access-injector)
* Install extensions (except jersey extensions)
* [Inject commands](commands.md#guice-injections)


!!! note
    As dropwizard bundles were registered under `GuiceBundle` configuration, they will be run by dropwizard
    after `GuiceBundle`.
    
!!! note
    Your `Application.run()` method will be called *after* guicey startup, so you can [use created 
    injector](guice/injector.md#access-injector) there.   

## Jersey startup

!!! note
    Jersey startup will initiate [hk2](hk2.md) context creation

* [Managed beans](../installers/managed.md) started
* [hk2](hk2.md) context creation activates `GuiceFeature` (registered earlier)
    - Apply [guice bridge](hk2.md#hk2-guice-bridge) (if required)
    - Run jersey specific installers ([resource](../installers/resource.md), [extension](../installers/jersey-ext.md)):
    installers will register required bindings in hk2 context

!!! note
    Any `EnvironmentCommand` did no start jersey, so managed objects will not be started (but you can start required 
    services [manually](commands.md#environment-commands). Also, all jersey related extensions will not be started.
    Still, core guice context will be completely operable. 

!!! attention ""
    When guice context is created, *jersey context doesn't exist* and when jersey context is created *it doesn't aware of guice existence*.

