#Dropwizard guice integration
[![License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)
[![Build Status](http://img.shields.io/travis/xvik/dropwizard-guicey.svg?style=flat&branch=master)](https://travis-ci.org/xvik/dropwizard-guicey)
[![Coverage Status](https://img.shields.io/coveralls/xvik/dropwizard-guicey.svg?style=flat)](https://coveralls.io/r/xvik/dropwizard-guicey?branch=master)

### About

Based on ideas from [dropwizard-guice](https://github.com/HubSpot/dropwizard-guice) and 
[dropwizardy-guice](https://github.com/jclawson/dropwizardry/tree/master/dropwizardry-guice) 
(which was derived from first one).

Features:
* Guice injector created on run phase (in opposite to dropwizard-guice; the same as dropwizardry-guice)
* Jersey integration through jersey-guice (the same in both libs)
* No base classes for application or guice module (only bundle registration required)
* Classpath scan (optional) to auto configure resources, tasks, commands, health checks etc (but no dependency on reflections library)
* Extending classpath scanner with custom installer plugins (and ability to switch off default installers)
* Injections works in commands
* Support injection of Bootstrap, Environment and Configuration objects into guice modules before injector creation 

### Setup

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) (package appear immediately after release) 
and then to maven central (require few days after release to be published). 

[![Download](https://api.bintray.com/packages/vyarus/xvik/dropwizard-guicey/images/download.png) ](https://bintray.com/vyarus/xvik/dropwizard-guicey/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/dropwizard-guicey/badge.svg?style=flat)](https://maven-badges.hrokuapp.com/maven-central/ru.vyarus/dropwizard-guicey)

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>dropwizard-guicey</artifactId>
  <version>0.1.0</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus:dropwizard-guicey:0.1.0'
```

### Usage

Register bundle:

```java
@Override
public void initialize(Bootstrap<TestConfiguration> bootstrap) {
    bootstrap.addBundle(GuiceBundle.<TestConfiguration>builder()
            .enableAutoConfig("package.to.scan")
            .searchCommands(true)
            .modules(new MyModule())
            .build()
    );
}
```

Options:
* `enableAutoConfig` one or more packages to scan (enables auto scan). If not set - no auto scan will be performed.
* `searchCommands` if true, command classes will be searched in classpath and registered in bootstrap. Auto scan must be enabled.
By default commands scan is disabled (false), because it may be not obvious.
* `modules` one or more guice modules to start. Not required: with auto scan you may not need custom module
* `disableInstallers` disables installers. Useful to override default installer implementations or simply disable them
* `build` allows specifying guice injector stage (production, development). By default, PRODUCTION stage used.

TODO
-
[![Slush java lib generator](http://img.shields.io/badge/Powered%20by-Slush%20java%20lib%20generator-orange.svg?style=flat-square)](https://github.com/xvik/slush-lib-java)