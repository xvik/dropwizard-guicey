# Welcome to dropwizard-guicey

!!! summary ""
    [Guice](https://github.com/google/guice) `{{ gradle.guice }}` integration for [dropwizard](http://dropwizard.io) `{{ gradle.dropwizard }}`.        
    Compiled for `java 11`, compatible with `java 11 - 21`. 

**[Release Notes](about/release-notes.md)** - [History](about/history.md) - [Javadoc](https://javadoc.io/doc/ru.vyarus/dropwizard-guicey/) - [Support](about/support.md) - [License](about/license.md)       

!!! note ""
    For migration see [migration guide](about/migration.md)
    
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

:   [![Channel Talk](img/sponsors/channel2.png)](https://channel.io "Channel Talk")

  
<sup>If guicey makes your life easier, you can [support its development](https://www.patreon.com/guicey).</sup>

## Project structure

* [ru.vyarus:dropwizard-guicey](https://github.com/xvik/dropwizard-guicey/tree/master/dropwizard-guicey) - core
    guicey module. Could be used without any extra modules
* [ru.vyarus.guicey:guicey-[module name]](https://github.com/xvik/dropwizard-guicey) - guicey extension 
    [modules](guide/modules.md) (use with `ru.vyarus.guicey:guicey-bom`). Modules provide additional functionality like 
    3rd party libraries integration. Also, serve as an example of possible extension implementations. 
* [Examples](https://github.com/xvik/dropwizard-guicey/tree/master/examples) - various usage examples for core guicey,
    extension modules and some direct integrations
  
!!! note "" 
    Before, guicey and extensions were released separately in different repositories - different packages were preserved after merge

## Supported versions

All active (not EOL) dropwizard versions supported.

Dropwizard | Guicey                                                       | Reason
----------|--------------------------------------------------------------|-------
2.1.x| [5.x](https://github.com/xvik/dropwizard-guicey/tree/dw-2.1) | Last java 8 compatible version (EOL [January 31 2024](https://github.com/dropwizard/dropwizard/discussions/7880))
3.x | [6.x](https://github.com/xvik/dropwizard-guicey/tree/dw-3)   | [Changed core dropwizard packages](https://www.dropwizard.io/en/stable/manual/upgrade-notes/upgrade-notes-3_0_x.html) - old 3rd paty bundles would be incompatible (EOL [January 31 2026](https://github.com/dropwizard/dropwizard/discussions/10798))
4.x | [7.x](https://github.com/xvik/dropwizard-guicey/tree/dw-4)   | [Jakarta namespace migration](https://www.dropwizard.io/en/stable/manual/upgrade-notes/upgrade-notes-4_0_x.html) - 3rd party guice modules might be incompatible (EOL [January 31 2026](https://github.com/dropwizard/dropwizard/discussions/10799))
5.x | 8.x | [Java 17 required](https://www.dropwizard.io/en/stable/manual/upgrade-notes/upgrade-notes-5_0_x.html)  

All branches use the same project structure: core guicey merged with extension modules.
It greatly simplifies releases and keeps actual examples in one branch.

## SBOM

[SBOM (cyclonedx)](https://cyclonedx.org/) is published for every guicey module with `cyclonedx` classifier (same way as dropwizard)
as json and xml files.

For example: [XML](https://repo1.maven.org/maven2/ru/vyarus/dropwizard-guicey/{{ gradle.version }}/dropwizard-guicey-{{ gradle.version }}-cyclonedx.xml),
[JSON](https://repo1.maven.org/maven2/ru/vyarus/dropwizard-guicey/{{ gradle.version }}/dropwizard-guicey-{{ gradle.version }}-cyclonedx.json)

## Documentation Summary

### Introduction

* [**Getting started**](getting-started.md) guide describes installation and provides core usage examples
* [**Concepts overview**](concepts.md) guide introduces core guicey concepts and demonstrates differences from pure dropwizard usage
* [**Guice**](guice.md) the essence of guice integration
* [**Testing**](tests.md) describes integration testing techniques
* [**Decomposition**](decomposition.md) guide on writing re-usable modules

### Reference
* [**User guide**](guide/configuration.md) contains detailed feature descriptions. It is good to read, but it also functions 
  well as a reference if you're short on time.
* [**Installers**](installers/resource.md) describes all guicey installers. Use it as a *extensions hand book*.
* [**Modules**](guide/modules.md) external extension modules overview.
* [**Examples**](examples/authentication.md) some usage examples. 
* [Standalone sample application](https://github.com/xvik/dropwizard-app-todo)
