# Dropwizard guice integration
[![License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)
[![Build Status](https://travis-ci.org/xvik/dropwizard-guicey.svg?branch=master)](https://travis-ci.org/xvik/dropwizard-guicey)
[![Appveyor build status](https://ci.appveyor.com/api/projects/status/github/xvik/dropwizard-guicey?svg=true&branch=master)](https://ci.appveyor.com/project/xvik/dropwizard-guicey)
[![codecov](https://codecov.io/gh/xvik/dropwizard-guicey/branch/master/graph/badge.svg)](https://codecov.io/gh/xvik/dropwizard-guicey)

**DOCUMENTATION**: http://xvik.github.io/dropwizard-guicey/

Additional repositories:

* [Examples](https://github.com/xvik/dropwizard-guicey-examples)
* [Extensions and integrations](https://github.com/xvik/dropwizard-guicey-ext)

Support:

* [google group](https://groups.google.com/forum/#!forum/dropwizard-guicey)
* [gitter chat](https://gitter.im/xvik/dropwizard-guicey) 

### About 

[Dropwizard](http://dropwizard.io/) 2.0.0 [guice](https://github.com/google/guice) 4.2.2 integration.

Features:

* Auto configuration from classpath scan and guice bindings.
* Yaml config values bindings by path or unique sub objects.
* Advanced Web support
* Dropwizard style console reporting: detected (and installed) extensions are printed to console to remove uncertainty
* Test support: custom junit and spock extensions
* Advanced test abilities to disable or override application logic
* Developer friendly:
    - core integrations may be replaced (to better fit needs)
    - rich api for developing custom integrations, and hooking into lifecycle)
    - out of the box support for plug-n-play plugins (auto discoverable)
    - diagnostic tools (reports), support for custom diagnostic tools

### Sponsors

&nbsp;&nbsp;&nbsp;&nbsp;[![Channel](src/doc/docs/img/sponsors/zoyi-ch.png)](https://channel.io "Channel")

  
<sup>If guicey makes your life easier, you can [support its development](https://www.patreon.com/guicey).</sup>

#### Thanks to

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
  <version>5.0.0</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus:dropwizard-guicey:5.0.0'
```

Dropwizard | Guicey
----------|---------
2.0| [5.0.0](http://xvik.github.io/dropwizard-guicey/5.0.0)
1.3| [4.2.2](http://xvik.github.io/dropwizard-guicey/4.2.2)
1.1, 1.2 | [4.1.0](http://xvik.github.io/dropwizard-guicey/4.1.0) 
1.0 | [4.0.1](http://xvik.github.io/dropwizard-guicey/4.0.1)
0.9 | [3.3.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.9)
0.8 | [3.1.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.8)
0.7 | [1.1.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.7)


#### BOM

Guicey pom may be also used as maven BOM:

```groovy
plugins {
    id "io.spring.dependency-management" version "1.0.8.RELEASE"
}
dependencyManagement {
    imports {
        mavenBom 'ru.vyarus:dropwizard-guicey:5.0.0'
        // uncomment to override dropwizard version    
        // mavenBom 'io.dropwizard:dropwizard-bom:2.0.0'
        // mavenBom 'io.dropwizard:dropwizard-dependencies:2.0.0'  
    }
}

dependencies {
    // no need to specify versions
    compile 'ru.vyarus:dropwizard-guicey'
   
    compile 'io.dropwizard:dropwizard-auth'
    compile 'com.google.inject:guice-assistedinject'   
     
    testCompile 'io.dropwizard:dropwizard-test'
    testCompile 'org.spockframework:spock-core'
}
```

BOM includes:

BOM           | Artifact
--------------|-------------------------
Guicey itself | `ru.vyarus:dropwizard-guicey`
Dropwizard BOM | `io.dropwizard:dropwizard-bom`
Guice BOM | `com.google.inject:guice-bom`
HK2 bridge | `org.glassfish.hk2:guice-bridge` 
System rules (required for StartupErrorRule) | `com.github.stefanbirkner:system-rules`
Spock | `org.spockframework:spock-core`

Guicey extensions project provide extended BOM with guicey and all guicey modules included. 
See [extensions project BOM](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-bom) section for more details of BOM usage.

### Snapshots

Snapshots could be used through JitPack:

* Go to [JitPack project page](https://jitpack.io/#ru.vyarus/dropwizard-guicey)
* Select `Commits` section and click `Get it` on commit you want to use (you may need to wait while version builds if no one requested it before)
* Follow displayed instruction: 
    - Add jitpack repository: `maven { url 'https://jitpack.io' }`
    - Use commit hash as version: `ru.vyarus:dropwizard-guicey:56537f7d23`

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
