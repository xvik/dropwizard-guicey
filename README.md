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

**NOTE** Dropwizard 2.0.0 compatible [release candidate is ready](https://github.com/xvik/dropwizard-guicey/issues/61#issuecomment-536259075). 
Please try it and [share your feedback](https://github.com/xvik/dropwizard-guicey/issues/61).

### About 

[Dropwizard](http://dropwizard.io/) 1.3.7 [guice](https://github.com/google/guice) 4.2.2 integration.

Originally inspired by [dropwizard-guice](https://github.com/HubSpot/dropwizard-guice) and 
[dropwizardy-guice](https://github.com/jclawson/dropwizardry/tree/master/dropwizardry-guice) 
(which was derived from first one).

Features:
* Guice injector created on run phase
* Auto configuration (classpath scan)
* Configuration bindings by path or unique sub configuration object 
* Support guice ServletModule and servlet 3.0 annotations (on both contexts)
* Dropwizard style reporting
* Admin context rest emulation
* Tests support for junit and spock
* Developer friendly: includes debugging tools and api for extensions
* Flexible [HK2](https://hk2.java.net/2.5.0-b05/introduction.html) integration

### Thanks to

* [Sébastien Boulet](https://github.com/gontard) ([intactile design](http://intactile.com)) for very useful feedback
* [Nicholas Pace](https://github.com/segfly) for governator integration

### Setup

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) (package appear immediately after release) 
and then to maven central (require few days after release to be published). 

[![JCenter](https://api.bintray.com/packages/vyarus/xvik/dropwizard-guicey/images/download.svg)](https://bintray.com/vyarus/xvik/dropwizard-guicey/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus/dropwizard-guicey.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/dropwizard-guicey)

May be used through [extensions project BOM](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-bom) or directly.

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>dropwizard-guicey</artifactId>
  <version>4.2.2</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus:dropwizard-guicey:4.2.2'
```

Dropwizard | Guicey
----------|---------
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
    id "io.spring.dependency-management" version "1.0.6.RELEASE"
}
dependencyManagement {
    imports {
        mavenBom 'ru.vyarus:dropwizard-guicey:4.2.2'
        // uncomment to override dropwizard version    
        // mavenBom 'io.dropwizard:dropwizard-bom:1.3.7' 
    }
}

dependencies {
    compile 'ru.vyarus:dropwizard-guicey:4.2.2'
   
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
* HK2 bridge (org.glassfish.hk2:guice-bridge) 
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
