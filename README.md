# Dropwizard guice integration
[![License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)
[![CI](https://github.com/xvik/dropwizard-guicey/actions/workflows/CI.yml/badge.svg)](https://github.com/xvik/dropwizard-guicey/actions/workflows/CI.yml)
[![Appveyor build status](https://ci.appveyor.com/api/projects/status/github/xvik/dropwizard-guicey?svg=true&branch=master)](https://ci.appveyor.com/project/xvik/dropwizard-guicey)
[![codecov](https://codecov.io/gh/xvik/dropwizard-guicey/branch/master/graph/badge.svg)](https://codecov.io/gh/xvik/dropwizard-guicey)

**DOCUMENTATION**: http://xvik.github.io/dropwizard-guicey/

Additional repositories:

* [Examples](https://github.com/xvik/dropwizard-guicey-examples)
* [Extensions and integrations](https://github.com/xvik/dropwizard-guicey-ext)

Support: [discussions](https://github.com/xvik/dropwizard-guicey/discussions) | [gitter chat](https://gitter.im/xvik/dropwizard-guicey) 

### About 

[Dropwizard](http://dropwizard.io/) 2.0.28 [guice](https://github.com/google/guice) 5.1.0 integration.

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

[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus/dropwizard-guicey.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/dropwizard-guicey)

May be used through [extensions project BOM](https://github.com/xvik/dropwizard-guicey-ext) or directly.

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>dropwizard-guicey</artifactId>
  <version>5.4.2</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus:dropwizard-guicey:5.4.2'
```

Dropwizard | Guicey
----------|---------
2.0| [5.4.2](http://xvik.github.io/dropwizard-guicey/5.4.2)
1.3| [4.2.3](http://xvik.github.io/dropwizard-guicey/4.2.3)
1.1, 1.2 | [4.1.0](http://xvik.github.io/dropwizard-guicey/4.1.0) 
1.0 | [4.0.1](http://xvik.github.io/dropwizard-guicey/4.0.1)
0.9 | [3.3.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.9)
0.8 | [3.1.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.8)
0.7 | [1.1.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.7)


#### BOM

Guicey pom may be also used as maven BOM.

NOTE: If you use guicey extensions then use [extensions BOM](https://github.com/xvik/dropwizard-guicey-ext) 
instead (it already includes guicey BOM).

BOM usage is highly recommended as it allows you to correctly update dropwizard dependencies.

Gradle:

```groovy
dependencies {
    implementation platform('ru.vyarus:dropwizard-guicey:5.4.2')
    // uncomment to override dropwizard and its dependencies versions    
    //implementation platform('io.dropwizard:dropwizard-dependencies:2.0.28')

    // no need to specify versions
    implementation 'ru.vyarus:dropwizard-guicey'
   
    implementation 'io.dropwizard:dropwizard-auth'
    implementation 'com.google.inject:guice-assistedinject'   
     
    testImplementation 'io.dropwizard:dropwizard-test'
    testImplementation 'org.spockframework:spock-core'
}
```

Maven:

```xml      
<dependencyManagement>  
    <dependencies>
        <dependency>
            <groupId>ru.vyarus</groupId>
            <artifactId>dropwizard-guicey</artifactId>
            <version>5.4.2</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency> 
        <!-- uncomment to override dropwizard and its dependencies versions  
        <dependency>
            <groupId>io.dropwizard/groupId>
            <artifactId>dropwizard-dependencies</artifactId>
            <version>2.0.28</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency> -->                 
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>ru.vyarus</groupId>
        <artifactId>dropwizard-guicey</artifactId>
    </dependency>
</dependencies>
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


### Snapshots

<details>
      <summary>Snapshots may be used through JitPack</summary>

Add [JitPack](https://jitpack.io/#ru.vyarus/dropwizard-guicey) repository:

```groovy
repositories { maven { url 'https://jitpack.io' } }
```

For spring dependencies plugin (when guicey pom used as BOM):

```groovy
dependencyManagement {
    resolutionStrategy {
        cacheChangingModulesFor 0, 'seconds'
    }
    imports {
        mavenBom "ru.vyarus:dropwizard-guicey:master-SNAPSHOT"
    }
}
``` 

For direct guicey dependency:

```groovy
configurations.all {
    resolutionStrategy.cacheChangingModulesFor 0, 'seconds'
}

dependencies {
    implementation 'ru.vyarus:dropwizard-guicey:master-SNAPSHOT'
}
```

Note that in both cases `resolutionStrategy` setting required for correct updating snapshot with recent commits
(without it you will not always have up-to-date snapshot)

OR you can depend on exact commit:

* Go to [JitPack project page](https://jitpack.io/#ru.vyarus/dropwizard-guicey)
* Select `Commits` section and click `Get it` on commit you want to use and 
 use commit hash as version: `ru.vyarus:dropwizard-guicey:56537f7d23`


Maven:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>  

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>ru.vyarus</groupId>
            <artifactId>dropwizard-guicey</artifactId>
            <version>master-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>ru.vyarus</groupId>
        <artifactId>dropwizard-guicey</artifactId>
    </dependency>
</dependencies>
```     

Or simply change version if used as direct dependency (repository must be also added):

```xml
<dependency>
    <groupId>ru.vyarus</groupId>
    <artifactId>dropwizard-guicey</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

</details> 

### Usage

Read [documentation](http://xvik.github.io/dropwizard-guicey/)

### Might also like

* [yaml-updater](https://github.com/xvik/yaml-updater) - yaml configuration update tool, preserving comments and whitespaces (has dropwizard module)
* [generics-resolver](https://github.com/xvik/generics-resolver) - runtime generics resolution
* [guice-validator](https://github.com/xvik/guice-validator) - hibernate validator integration for guice 
(objects validation, method arguments and return type runtime validation)
* [guice-ext-annotations](https://github.com/xvik/guice-ext-annotations) - @Log, @PostConstruct, @PreDestroy and
utilities for adding new annotations support
* [guice-persist-orient](https://github.com/xvik/guice-persist-orient) - guice integration for orientdb
* [dropwizard-orient-server](https://github.com/xvik/dropwizard-orient-server) - embedded orientdb server for dropwizard

---
[![java lib generator](http://img.shields.io/badge/Powered%20by-%20Java%20lib%20generator-green.svg?style=flat-square)](https://github.com/xvik/generator-lib-java)
