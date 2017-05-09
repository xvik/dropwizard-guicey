# Dropwizard guice integration
[![License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)
[![Build Status](http://img.shields.io/travis/xvik/dropwizard-guicey.svg?style=flat&branch=master)](https://travis-ci.org/xvik/dropwizard-guicey)
[![Coverage Status](https://img.shields.io/coveralls/xvik/dropwizard-guicey.svg?style=flat)](https://coveralls.io/r/xvik/dropwizard-guicey?branch=master)

**DOCUMENTATION**: http://xvik.github.io/dropwizard-guicey/

Additional repositories:

* [Examples](https://github.com/xvik/dropwizard-guicey-examples)
* [Extensions and integrations](https://github.com/xvik/dropwizard-guicey-ext)

Support:

* [google group](https://groups.google.com/forum/#!forum/dropwizard-guicey)
* [gitter chat](https://gitter.im/xvik/dropwizard-guicey) 


### About

[Dropwizard](http://dropwizard.io/) 1.1.0 [guice](https://github.com/google/guice) (4.1.0) integration. 

Originally inspired by [dropwizard-guice](https://github.com/HubSpot/dropwizard-guice) and 
[dropwizardy-guice](https://github.com/jclawson/dropwizardry/tree/master/dropwizardry-guice) 
(which was derived from first one).

Features:
* Guice injector created on run phase
* Compatible with guice restrictive options: `disableCircularProxies`, `requireExactBindingAnnotations` and `requireExplicitBindings`
* Flexible [HK2](https://hk2.java.net/2.5.0-b05/introduction.html) integration
* No base classes for application or guice module (only bundle registration required)
* Configurable [installers](#installers) mechanism: each supported feature (task install, health check install, etc) has it's own installer and may be disabled
* [Custom feature installers](#writing-custom-installer) could be added
* Optional [classpath scan](#classpath-scan) to search features: resources, tasks, commands, health checks etc (without dependency on reflections library)
* Injections [works in commands](#commands-support) (environment commands)
* Support injection of Bootstrap, Environment and Configuration objects into guice modules [before injector creation](#module-autowiring) 
* Guice [ServletModule](#guice-servletmodule-support) can be used to bind servlets and filters for main context (may be [disabled](#disable-guice-servlets))
* Dropwizard style [reporting](#reporting) of installed extensions
* Admin context [rest emulation](#admin-rest)
* Custom [junit rule](#useguiceyapp) for lightweight integration testing
* [Spock](http://spockframework.org) [extensions](#spock)

### Thanks to

* [Sébastien Boulet](https://github.com/gontard) ([intactile design](http://intactile.com)) for very useful feedback
* [Nicholas Pace](https://github.com/segfly) for governator integration

### Setup

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) (package appear immediately after release) 
and then to maven central (require few days after release to be published). 

[![JCenter](https://img.shields.io/bintray/v/vyarus/xvik/dropwizard-guicey.svg?label=jcenter)](https://bintray.com/vyarus/xvik/dropwizard-guicey/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus/dropwizard-guicey.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/dropwizard-guicey)

May be used through [extensions project BOM](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-bom) or directly.

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>dropwizard-guicey</artifactId>
  <version>4.1.0</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus:dropwizard-guicey:4.1.0'
```

Dropwizard | Guicey
----------|---------
1.0 | [4.0.1](http://xvik.github.io/dropwizard-guicey/4.0.1)
0.9 | [3.3.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.9)
0.8 | [3.1.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.8)
0.7 | [1.1.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.7)


#### BOM

Guicey pom may be also used as maven BOM:

```groovy
plugins {
    id "io.spring.dependency-management" version "1.0.2.RELEASE"
}
dependencyManagement {
    imports {
        mavenBom 'ru.vyarus.guicey:guicey:4.1.0'
    }
}

dependencies {
    compile 'ru.vyarus.guicey:guicey:4.1.0'
   
    // no need to specify versions
    compile 'io.dropwizard:dropwizard-auth'
    compile 'com.google.inject:guice-assistedinject'   
     
    testCompile 'io.dropwizard:dropwizard-test'
    testCompile 'org.spockframework:spock-core'
}
```

Bom includes:

* Dropwizard BOM (io.dropwizard:dropwizard-bom)
* Guice BOM (com.google.inject:guice-bom)
* Hk bridge (org.glassfish.hk2:guice-bridge) 
* System rules, required for StartupErrorRule (com.github.stefanbirkner:system-rules)
* Spock (org.spockframework:spock-core)

Guicey extensions project provide extended BOM with guicey and all guicey modules included. 
See [extensions project BOM](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-bom) section for more details of BOM usage.

##### Snapshots

You can use snapshot versions through [JitPack](https://jitpack.io):

* Go to [JitPack project page](https://jitpack.io/#xvik/dropwizard-guicey)
* Select `Commits` section and click `Get it` on commit you want to use (top one - the most recent)
* Follow displayed instruction: add repository and change dependency (NOTE: due to JitPack convention artifact group will be different)

### Usage

Read [documentation](http://xvik.github.io/dropwizard-guicey/)

### Might also like

* [generics-resolver](https://github.com/xvik/generics-resolver) - runtime generics resolution
* [guice-validator](https://github.com/xvik/guice-validator) - hibernate validator integration for guice 
(objects validation, method arguments and return type runtime validation)
* [guice-ext-annotations](https://github.com/xvik/guice-ext-annotations) - @Log, @PostConstruct, @PreDestroy and
utilities for adding new annotations support
* [guice-persist-orient](https://github.com/xvik/guice-persist-orient) - guice integration for orientdb
* [dropwizard-orient-server](https://github.com/xvik/dropwizard-orient-server) - embedded orientdb server for dropwizard

---
[![java lib generator](http://img.shields.io/badge/Powered%20by-%20Java%20lib%20generator-green.svg?style=flat-square)](https://github.com/xvik/generator-lib-java)
