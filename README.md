# Dropwizard guice integration
[![License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)
[![CI](https://github.com/xvik/dropwizard-guicey/actions/workflows/CI.yml/badge.svg)](https://github.com/xvik/dropwizard-guicey/actions/workflows/CI.yml)
[![Appveyor build status](https://ci.appveyor.com/api/projects/status/github/xvik/dropwizard-guicey?svg=true&branch=master)](https://ci.appveyor.com/project/xvik/dropwizard-guicey/branch/master)
[![codecov](https://codecov.io/gh/xvik/dropwizard-guicey/branch/master/graph/badge.svg)](https://codecov.io/gh/xvik/dropwizard-guicey)

**DOCUMENTATION**: http://xvik.github.io/dropwizard-guicey/

* [Examples](https://github.com/xvik/dropwizard-guicey/tree/master/examples)
* [Standalone sample app](https://github.com/xvik/dropwizard-app-todo)
* [Extensions and integrations](https://github.com/xvik/dropwizard-guicey/)

Support: [discussions](https://github.com/xvik/dropwizard-guicey/discussions) | [gitter chat](https://gitter.im/xvik/dropwizard-guicey) 

### About 

[Dropwizard](http://dropwizard.io/) 5.0.1 [guice](https://github.com/google/guice) 7.0.0 integration.

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

&nbsp;&nbsp;&nbsp;&nbsp;[![Channel Talk](dropwizard-guicey/src/doc/docs/img/sponsors/channel2.png)](https://channel.io "Channel Talk")

  
<sup>If guicey makes your life easier, you can [support its development](https://www.patreon.com/guicey).</sup>

### Supported versions

All active (not EOL) dropwizard versions supported. 

Dropwizard | Guicey                                                       | Reason
----------|--------------------------------------------------------------|-------
2.1.x| [5.x](https://github.com/xvik/dropwizard-guicey/tree/dw-2.1) | Last java 8 compatible version (EOL [January 31 2024](https://github.com/dropwizard/dropwizard/discussions/7880))
3.x | [6.x](https://github.com/xvik/dropwizard-guicey/tree/dw-3)   | [Changed core dropwizard packages](https://github.com/dropwizard/dropwizard/blob/release/3.0.x/docs/source/manual/upgrade-notes/upgrade-notes-3_0_x.rst) - old 3rd paty bundles would be incompatible; Java 11 required
4.x | [7.x](https://github.com/xvik/dropwizard-guicey/tree/dw-4)   | [Jakarta namespace migration](https://github.com/dropwizard/dropwizard/blob/release/4.0.x/docs/source/manual/upgrade-notes/upgrade-notes-4_0_x.rst) - 3rd party guice modules might be incompatible
5.x | 8.x | Java 17 required  

Upcoming guicey changes would be ported in all active branches.

### Setup

[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus/dropwizard-guicey.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/dropwizard-guicey)

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>dropwizard-guicey</artifactId>
  <version>8.0.2</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus:dropwizard-guicey:8.0.2'
```

Dropwizard | Guicey
----------|---------
5.0| [8.0.2](http://xvik.github.io/dropwizard-guicey/8.0.2)
4.0| [7.3.2](http://xvik.github.io/dropwizard-guicey/7.3.2)
3.0| [6.4.2](http://xvik.github.io/dropwizard-guicey/6.4.2)
2.1| [5.10.2](http://xvik.github.io/dropwizard-guicey/5.10.2)
2.0| [5.5.0](http://xvik.github.io/dropwizard-guicey/5.5.0)
1.3| [4.2.3](http://xvik.github.io/dropwizard-guicey/4.2.3)
1.1, 1.2 | [4.1.0](http://xvik.github.io/dropwizard-guicey/4.1.0) 
1.0 | [4.0.1](http://xvik.github.io/dropwizard-guicey/4.0.1)
0.9 | [3.3.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.9)
0.8 | [3.1.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.8)
0.7 | [1.1.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.7)

#### BOM

Use [BOM](http://xvik.github.io/dropwizard-guicey/latest/extras/bom/) for guice, dropwizard and guicey modules dependency management.
BOM usage is highly recommended as it allows you to correctly update dropwizard dependencies.

Gradle:

```groovy
dependencies {
    implementation platform('ru.vyarus.guicey:guicey-bom:8.0.2')
    // uncomment to override dropwizard and its dependencies versions    
    //implementation platform('io.dropwizard:dropwizard-dependencies:5.0.1')

    // no need to specify versions
    implementation 'ru.vyarus:dropwizard-guicey'
    implementation 'ru.vyarus.guicey:guicey-eventbus'
   
    implementation 'io.dropwizard:dropwizard-auth'
    implementation 'com.google.inject:guice-assistedinject'   
    
    testImplementation 'io.dropwizard:dropwizard-testing'
}
```

Maven:

```xml      
<dependencyManagement>  
    <dependencies>
        <dependency>
            <groupId>ru.vyarus.guicey</groupId>
            <artifactId>guicey-bom</artifactId>
            <version>8.0.2</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency> 
        <!-- uncomment to override dropwizard and its dependencies versions  
        <dependency>
            <groupId>io.dropwizard/groupId>
            <artifactId>dropwizard-dependencies</artifactId>
            <version>5.0.1</version>
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
Guicey modules | `ru.vyarus.guicey:guicey-[module]`
Dropwizard BOM | `io.dropwizard:dropwizard-bom`
Guice BOM | `com.google.inject:guice-bom`
HK2 bridge | `org.glassfish.hk2:guice-bridge`
Spock-junit5 | `ru.vyarus:spock-junit5`

#### Sample project

You can also use [sample gradle project](https://github.com/xvik/dropwizard-app-todo) for a new project bootstrap  

### Snapshots

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fru%2Fvyarus%2Fdropwizard-guicey%2Fmaven-metadata.xml)](https://central.sonatype.com/repository/maven-snapshots/ru/vyarus/dropwizard-guicey/maven-metadata.xml)

<details>
      <summary>Snapshots published into Maven Central</summary>

Add maven snapshots repository:

```groovy
repositories {
        mavenLocal()
        mavenCentral()
        maven {
            name = 'Central Portal Snapshots'
            url = 'https://central.sonatype.com/repository/maven-snapshots/'
            mavenContent {
                snapshotsOnly()
                includeGroupAndSubgroups('ru.vyarus')
            }
        }
    }
```

Use [snapshot version](https://central.sonatype.com/repository/maven-snapshots/ru/vyarus/dropwizard-guicey/maven-metadata.xml):

```groovy
dependencies {
    implementation 'ru.vyarus:dropwizard-guicey:8.0.3-SNAPSHOT'
}
```

To avoid caching you may use:

```groovy
configurations.all {
    resolutionStrategy.cacheChangingModulesFor 0, 'seconds'
}
```

Maven:

```xml
<repositories>
    <repository>
        <name>Central Portal Snapshots</name>
        <id>central-portal-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```     

Use [shapshot version](https://central.sonatype.com/repository/maven-snapshots/ru/vyarus/dropwizard-guicey/maven-metadata.xml):

```xml
<dependency>
    <groupId>ru.vyarus</groupId>
    <artifactId>dropwizard-guicey</artifactId>
    <version>8.0.3-SNAPSHOT</version>
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
