# Integration lifecycle

Jersey2 guice integration is more complicated than for jersey1, because of [HK2](https://hk2.java.net/2.4.0-b34/introduction.html) container, used by jersey2.

!!! note
    Many people ask why not just use HK2 instead of guice as it's already provided. Unfortunately, it's hard to use it 
    in the same elegant way as we can use guice. HK2 context is launched too late (after dropwizard run phase).
    For example, it is impossible to use HK2 to instantiate dropwizard managed object because managed
    must be registered before HK2 context starts.

Guice integration done in guice exclusive way as much as possible: everything should be managed by guice and invisibly integrated into HK2.
Anyway, it is not always possible to hide integration details, especially if you need to register jersey extensions.

!!! tip 
    You can use [guicey lifecycle events](events.md) to see initialization stages in logs:
    `.printLifecyclePhases()` 

## Lifecycle

* Dropwizard configuration phase (`~Application.initialize`)   
    * Apply [configuration hooks](configuration.md#guicey-configuration-hooks)
    * Guice bundle registered 
    * Perform [classpath scan for commands](commands.md#automatic-installation) ([optional](configuration.md#commands-search))
* Dropwizard run phase (`~Application.run`)
    * Dropwizard runs bundles (guice bundle is one of them so guice initialization may be performed between other dropwizard bundles)
    * Search guicey bundles in dropwizard bundles ([optional](configuration.md#dropwizard-bundles-unification)) 
    * [Lookup guicey bundles](bundles.md#bundle-lookup)   
    * Apply configuration from guicey bundles
    * **Injector creation** ([using factory](guice/injector.md#injector-factory))
        * [Bind dropwizard objects](guice/bindings.md): Environment, Configuration, Bootstrap
        * Scan for installers (in auto configuration mode)
        * Scan for extensions (in auto configuration mode)
        * Register `GuiceFeature` in environment (jersey `Feature` which will trigger jersey side installations)
        * Apply [lazy jersey bindings](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/jersey/hk2/GuiceBindingsModule.java)
        * Activate [guice servlet support](web.md), register GuiceFilter on admin and main contexts ([could be disabled](configuration.md#servletmodule))
    * Injector created
        * Call installers to register extensions
    * Your application's `run` method executed. Injector is already available, so any guice bean could be [accessed](guice/injector.md)          
* Jersey start
    * [Managed beans](../installers/managed.md) started
    * **HK2 context creation** (jersey start)
        * `GuiceFeature` (registered earlier) called
            * [Optionally](configuration.md#hk2-bridge) register [HK2-guice bridge](https://hk2.java.net/2.4.0-b34/guice-bridge.html) (only guice to hk2 way to let hk2 managed beans inject guice beans)
            * Run jersey specific installers ([resource](../installers/resource.md), [extension](../installers/jersey-ext.md))

!!! note
    Any `EnvironmentCommand` did no start jersey, so managed objects will not be started.
    Also, all jersey related extensions will not be started. Still, core guice context will be completely operable. 

!!! attention ""
    When guice context is created, *jersey context doesn't exist* and when jersey context is created *it doesn't aware of guice existence*.

