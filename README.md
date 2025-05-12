# Dropwizard guice integration
[![License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)
[![CI](https://github.com/xvik/dropwizard-guicey/actions/workflows/CI.yml/badge.svg)](https://github.com/xvik/dropwizard-guicey/actions/workflows/CI.yml)
[![Appveyor build status](https://ci.appveyor.com/api/projects/status/github/xvik/dropwizard-guicey?svg=true&branch=master)](https://ci.appveyor.com/project/xvik/dropwizard-guicey/branch/master)
[![codecov](https://codecov.io/gh/xvik/dropwizard-guicey/branch/master/graph/badge.svg)](https://codecov.io/gh/xvik/dropwizard-guicey)

**DOCUMENTATION**: http://xvik.github.io/dropwizard-guicey/

* [Examples](https://github.com/xvik/dropwizard-guicey/tree/master/examples)
* [Extensions and integrations](https://github.com/xvik/dropwizard-guicey/)

Support: [discussions](https://github.com/xvik/dropwizard-guicey/discussions) | [gitter chat](https://gitter.im/xvik/dropwizard-guicey) 

### About 

[Dropwizard](http://dropwizard.io/) 4.0.13 [guice](https://github.com/google/guice) 7.0.0 integration.

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
5.x | Trunk compatible (8.x), not released                         | Java 17 required  

Upcoming guicey changes would be ported in all active branches.

### Setup

[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus/dropwizard-guicey.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/dropwizard-guicey)

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>dropwizard-guicey</artifactId>
  <version>7.2.1</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus:dropwizard-guicey:7.2.1'
```

Dropwizard | Guicey
----------|---------
4.0| [7.2.1](http://xvik.github.io/dropwizard-guicey/7.2.1)
3.0| [6.3.1](http://xvik.github.io/dropwizard-guicey/6.3.1)
2.1| [5.10.2](http://xvik.github.io/dropwizard-guicey/5.10.2)
2.0| [5.5.0](http://xvik.github.io/dropwizard-guicey/5.5.0)
1.3| [4.2.3](http://xvik.github.io/dropwizard-guicey/4.2.3)
1.1, 1.2 | [4.1.0](http://xvik.github.io/dropwizard-guicey/4.1.0) 
1.0 | [4.0.1](http://xvik.github.io/dropwizard-guicey/4.0.1)
0.9 | [3.3.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.9)
0.8 | [3.1.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.8)
0.7 | [1.1.0](https://github.com/xvik/dropwizard-guicey/tree/dw-0.7)

**GRADLE 6 users**: You might face `Could not resolve com.google.guava:guava:32.1.2-jre.`
problem. This caused by guava packaging [issue](https://github.com/google/guava/issues/6612) (affected many people). 
Either upgrade to gradle 7-8 or [apply workaround](https://github.com/google/guava/issues/6612#issuecomment-1614992368)

#### BOM

Use [BOM](http://xvik.github.io/dropwizard-guicey/latest/extras/bom/) for guice, dropwizard and guicey modules dependency management.
BOM usage is highly recommended as it allows you to correctly update dropwizard dependencies.

Gradle:

```groovy
dependencies {
    implementation platform('ru.vyarus.guicey:guicey-bom:7.2.1')
    // uncomment to override dropwizard and its dependencies versions    
    //implementation platform('io.dropwizard:dropwizard-dependencies:4.0.8')

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
            <version>7.2.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency> 
        <!-- uncomment to override dropwizard and its dependencies versions  
        <dependency>
            <groupId>io.dropwizard/groupId>
            <artifactId>dropwizard-dependencies</artifactId>
            <version>4.0.13</version>
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


### Snapshots

<details>
      <summary>Snapshots may be used through GitHub packages</summary>

WARNING: Accessing GitHub package requires [GitHub authorization](https://docs.github.com/en/enterprise-cloud@latest/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#authenticating-to-github-packages)!

Read the [detailed guide](https://blog.vyarus.ru/using-github-packages-in-gradle-and-maven-projects) for token creation

An actual published version could be seen on [package page](https://github.com/xvik/dropwizard-guicey/packages/2340608)
 

For [Gradle](https://docs.github.com/en/enterprise-cloud@latest/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry):

* Add GitHub repository in build.gradle:

    ```groovy
    repositories {
        maven {
            url  = 'https://maven.pkg.github.com/xvik/dropwizard-guicey'
            credentials {
                username = findProperty('gpr.user') ?: System.getenv("USERNAME")
                password = findProperty('gpr.key') ?: System.getenv("TOKEN")
            }
        }
    }
    ```
    
    or in settings.gradle:
    
    ```groovy
    dependencyResolutionManagement {
        repositories {
            mavenCentral()
            maven {
                url  = 'https://maven.pkg.github.com/xvik/dropwizard-guicey'
                credentials {
                    username = settings.ext.find('gpr.user') ?: System.getenv("USERNAME")
                    password = settings.ext.find('gpr.key') ?: System.getenv("TOKEN")
                }
            }
        }
    }
    ```

* In global gradle file `~/.gradle/gradle.properties` add
    ```
    gpr.user=<your github user name>
    gpr.key=<your github password or classic token>
    ```                                            
    (or credentials must be declared in environment: USERNAME/TOKEN (more usable for CI))
    Read [personal access tokens creation guide](https://docs.github.com/en/enterprise-cloud@latest/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic)
    Note that token needs only "package/read" permission
* Use a snapshot version (as usual):
    ```groovy
    dependencies {
        implementation 'ru.vyarus:dropwizard-guicey:8.0.0-SNAPSHOT'
    }
    ```
* If there would be problems loading the latest snapshot, change cache policy:
    ```groovy
    configurations.all {
        resolutionStrategy.cacheChangingModulesFor 0, 'seconds'
    }   
    ```

For [Maven](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry),

* Add credentials into ~/.m2/settings.xml:
    ```xml
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          http://maven.apache.org/xsd/settings-1.0.0.xsd">            
    
        <servers>
            <server>
                <id>github</id>
                <username>USERNAME</username>
                <password>TOKEN</password>
            </server>
        </servers>
    </settings>  
    ```
  (where USERNAME- github username and TOKEN - [classic token with packages:read permission](https://docs.github.com/en/enterprise-cloud@latest/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic))
* Add repository (in project or using profile in settings.xml)
  ```xml
  <repositories>    
    <repository>
          <id>github</id>
          <url>https://maven.pkg.github.com/xvik/dropwizard-guicey</url>
          <snapshots>
              <enabled>true</enabled>
          </snapshots>
      </repository>
  </repositories>
  ```            
  (repository name MUST be the same as declared server)

* Use dependency
    ```xml
    <dependencies>
        <dependency>
            <groupId>ru.vyarus</groupId>
            <artifactId>dropwizard-guicey</artifactId>
            <version>8.0.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
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
