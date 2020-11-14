# Welcome to dropwizard-guicey

!!! summary ""
    [Guice](https://github.com/google/guice) `4.2.3` integration for [dropwizard](http://dropwizard.io) `2.0.10`.        
    Compiled for `java 8`, binary compatible with `java 11`. 

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

## How to use docs

### Introduction

* [**Getting started**](getting-started.md) guide describes installation and shows core usage examples
* [**Concepts overview**](concepts.md) guide introduce core guicey concepts and explains differences with pure dropwizard usage
* [**Gucie**](guice.md) the essence of guice integration
* [**Testing**](tests.md) describes integration testing techniques
* [**Decomposition**](tests.md) guide on writing re-usable modules

### Reference
* [**User guide**](guide/configuration.md) contain detailed features descriptions. Good to read, but if no time, read as you need it.
* [**Installers**](installers/resource.md) describes all guicey installers. Use it as a *extensions hand book*.
* [**Modules**](guide/modules.md) extension modules 
* [**Examples**](examples/authentication.md) important usage examples. Look also [examples repository](https://github.com/xvik/dropwizard-guicey-examples) for additional examples. 

## Sources structure

* [Guicey repository]((https://github.com/xvik/dropwizard-guicey)): guicey itself and (this) docs
* [Modules repository](https://github.com/xvik/dropwizard-guicey-ext): extension [modules](guide/modules.md) (integrations) 
are maintained in the separate repository
* [Examples repository](https://github.com/xvik/dropwizard-guicey-examples) holds usage examples of main features usage, 
dropwizard bundles integrations and extension modules samples.  
