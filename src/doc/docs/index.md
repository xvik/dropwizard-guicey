# Welcome to dropwizard-guicey

!!! summary ""
    [Guice](https://github.com/google/guice) `4.2.2` integration for [dropwizard](http://dropwizard.io) `2.0.0`.        
    Compiled for `java 8`, binary compatible with `java 11`. 

**[Release Notes](about/release-notes.md)** - [History](about/history.md) - [Javadoc](https://javadoc.io/doc/ru.vyarus/dropwizard-guicey/) - [Support](about/support.md) - [License](about/license.md)       

## Main features

* Auto configuration from [classpath scan](guide/configuration.md#auto-configuration) and guice bindings.  
* [Yaml config values bindings](guide/guice/bindings.md#configuration-by-path) by path or unique sub objects. 
* [Web](guide/web.md) (servlets, filters, listeners):
    - supports both contexts (main and admin)
    - guice ServletModule support is enabled by default ([could be disabled](guide/web.md#disable-servletmodule-support))
    - jee web annotations (@WebServlet, @WebFilter) support ([could be enabled](guide/web.md#web-instalers))
* Dropwizard style [reporting](guide/installers.md#reporting): detected (and installed) extensions are printed to console to remove uncertainty
* Admin context [rest emulation](extras/admin-rest.md) 
* [Test support](guide/test.md): custom junit and [spock](http://spockframework.org) extensions
* Developer friendly: 
    - core integrations [may be replaced](guide/configuration.md#disable-installers) (to better fit needs)
    - rich api for developing [custom integrations](guide/installers.md#writing-custom-installer), [custom behaviours](guide/events.md#events) and [configuration modification](guide/configuration.md#guicey-configuration-hooks) 
    - out of the box support for plug-n-play plugins ([auto discoverable](guide/bundles.md#service-loader-lookup))
    - self diagnostic tools ([configuration diagnostic report](guide/diagnostic/diagnostic-tools.md), [bindable configuration paths](guide/guice/bindings.md#configuration-bindings-report), [lifecycle stages](guide/configuration.md#lifecycle-events)) 

## How to use docs

### Introduction

!!! note ""
    If you are migrating from [dropwizard-guice](https://github.com/HubSpot/dropwizard-guice) read [migration guide](guide/dg-migration.md) first

* [**Getting started**](getting-started.md) guide describes installation and shows core usage examples
* [**Concepts overview**](concepts.md) guide introduce core guicey concepts and explains differences with pure dropwizard usage

### Reference
* [**User guide**](guide/configuration.md) section contain detailed features descriptions. Good to read, but if no time, read as you need it.
* [**Installers**](installers/resource.md) section describes all guicey installers. Use it as a *extensions hand book*.

### Examples and integrations

* [**Examples**](examples/authentication.md) section contains common example cases. Look also [examples repository](https://github.com/xvik/dropwizard-guicey-examples) for additional examples.
* [**Extras**](extras/admin-rest.md) section covers extra modules: admin rest, 3rd party integrations (event bus, jdbi) provided by guicey itself 
or extensions project.

## Sources structure

[The main repository](https://github.com/xvik/dropwizard-guicey) contains library itself and this documentation sources.

Guicey core. Provides rich api for building custom integrations (for specific needs). 
Only core version follow semantic versioning.

[Examples repository](https://github.com/xvik/dropwizard-guicey-examples) holds examples of main features usage, dropwizard bundles 
integrations and extensions samples.

[Extensions repository](https://github.com/xvik/dropwizard-guicey-ext) contains guicey external integrations. 

Extensions project shows what is possible to achieve based on guicey. Besides, provided integrations 
itself might be quite useful. Version scheme is the same as dropwizard modules: guiceyVersion-releaseNumber
(no semantic versioning).

* [BOM module](extras/bom.md) unifies dependencies management for extensions, dropwizard and guice (as it includes boms for them).
   Overall, extensions project is more like spring: growing set of solutions for everything (more "enterprisy").

