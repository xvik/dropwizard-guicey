# Welcome to dropwizard-guicey

!!! summary ""
    [Guice](https://github.com/google/guice) `{{ gradle.guice }}` integration for [dropwizard](http://dropwizard.io) `{{ gradle.dropwizard }}`.        
    Compiled for `java 8`, compatible with `java 11 - 16` ([17 not supported by dropwizard](https://github.com/dropwizard/dropwizard/issues/4347)). 

**[Release Notes](about/release-notes.md)** - [History](about/history.md) - [Javadoc](https://javadoc.io/doc/ru.vyarus/dropwizard-guicey/) - [Support](about/support.md) - [License](about/license.md)       

!!! note ""
    If you migrate from dropwizard 1.x then see [dropwizard upgrade instructions](https://www.dropwizard.io/en/release-2.0.x/manual/upgrade-notes/upgrade-notes-2_0_x.html)
    and [guicey migration guide](http://xvik.github.io/dropwizard-guicey/5.0.0/about/release-notes/#migration-guide).
    
## Main features

* Auto configuration from [classpath scan](guide/scan.md) and [guice bindings](guide/guice/module-analysis.md#extensions-recognition).  
* [Yaml config values bindings](guide/yaml-values.md) by path or unique sub objects. 
* Advanced [Web support](guide/web.md)
* Dropwizard style [console reporting](guide/installers.md#reporting): detected (and installed) extensions are printed to console to remove uncertainty 
* [Test support](guide/test/overview.md): custom junit and [spock](http://spockframework.org) extensions
    - Advanced test abilities to [disable](guide/disables.md) or [override](guide/guice/override.md) application logic
* Developer friendly: 
    - core integrations [may be replaced](guide/disables.md#disable-installers) (to better fit needs)
    - rich api for developing [custom integrations](guide/installers.md#writing-custom-installer), and hooking into [lifecycle](guide/events.md)) 
    - out of the box support for plug-n-play plugins ([auto discoverable](guide/bundles.md#service-loader-lookup))
    - [diagnostic tools](guide/diagnostic/diagnostic-tools.md) (reports), support for [custom diagnostic tools](guide/hooks.md#diagnostic)   

## Sponsors

:   [![Channel](img/sponsors/zoyi-ch.png)](https://channel.io "Channel")

  
<sup>If guicey makes your life easier, you can [support its development](https://www.patreon.com/guicey).</sup>

## Documentation Summary

### Introduction

* [**Getting started**](getting-started.md) guide describes installation and provides core usage examples
* [**Concepts overview**](concepts.md) guide introduces core guicey concepts and demonstrates differences from pure dropwizard usage
* [**Guice**](guice.md) the essence of guice integration
* [**Testing**](tests.md) describes integration testing techniques
* [**Decomposition**](tests.md) guide on writing re-usable modules

### Reference
* [**User guide**](guide/configuration.md) contains detailed feature descriptions. It is good to read, but it also functions 
  well as a reference if you're short on time.
* [**Installers**](installers/resource.md) describes all guicey installers. Use it as a *extensions hand book*.
* [**Modules**](guide/modules.md) external extension modules overview.
* [**Examples**](examples/authentication.md) important usage examples. 

## Sources structure

* [Guicey repository]((https://github.com/xvik/dropwizard-guicey)): guicey itself and (these) docs
* [Modules repository](https://github.com/xvik/dropwizard-guicey-ext): extension [modules](guide/modules.md) (integrations) 
are maintained in the separate repository
* [Examples repository](https://github.com/xvik/dropwizard-guicey-examples): holds code samples for main features dropwizard 
bundles and extension modules.
