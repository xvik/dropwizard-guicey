# Guicey BOM

!!! summary ""
    [Extensions project](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-bom) module

Maven BOM contains guicey and guicey ext modules versions. Also includes dropwizard and guice boms.

!!! tip
    BOM's are useful for versions management. After including bom you can simply include required dependencies
    (dropwizard, guice, guicey, guicey-ext) without versions: bom will control all versions.

| BOM version | Guicey | Dropwizard | Guice |
|-------------|--------|-----------|-------|
| 5.5.0-1     | 5.5.0  | 2.0.28    | 5.1.0 |
| 5.4.2-1     | 5.4.2  | 2.0.28    | 5.1.0 |
| 5.4.0-1     | 5.4.0  | 2.0.25    | 5.0.1 |
| 5.3.0-1     | 5.3.0  | 2.0.20    | 5.0.1 |
| 5.2.0-1     | 5.2.0  | 2.0.16    | 4.2.3 |
| 5.1.0-2     | 5.1.0  | 2.0.10    | 4.2.3 |
| 5.0.1-1     | 5.0.1  | 2.0.2     | 4.2.2 |
| 5.0.0-0     | 5.0.0  | 2.0.0     | 4.2.2 |
| 0.7.0       | 4.2.2  | 1.3.7     | 4.2.2 |
| 0.6.0       | 4.2.2  | 1.3.7     | 4.2.2 |
| 0.5.0       | 4.2.1  | 1.3.5     | 4.2.0 |
| 0.4.0       | 4.2.0  | 1.3.5     | 4.2.0 |
| 0.3.0       | 4.1.0  | 1.1.0     | 4.1.0 |

Since 5.0.0 extension modules version is derived from guicey version: guiceyVersion-Number 
(the same convention as for dropwizard modules). For example version 5.0.0-1 means
first extensions release (1) for guicey 5.0.0. 

## Setup

[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus.guicey/guicey-bom.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus.guicey/guicey-bom)


Maven:

```xml
<!-- Implicitly imports Dropwizard and Guice BOMs -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>ru.vyarus.guicey</groupId>
            <artifactId>guicey-bom</artifactId>
            <version>{{ gradle.ext }}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>  
        <!-- uncomment to override dropwizard and its dependencies versions  
        <dependency>
            <groupId>io.dropwizard/groupId>
            <artifactId>dropwizard-dependencies</artifactId>
            <version>{{ gradle.dropwizard }}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency> --> 
    </dependencies>
</dependencyManagement>

<!-- declare guice and ext modules without versions -->
<dependencies>
    <dependency>
      <groupId>ru.vyarus</groupId>
      <artifactId>dropwizard-guicey</artifactId>
    </dependency>
    <!-- For example, using dropwizard module (without version) -->
    <dependency>
      <groupId>io.dropwizard</groupId>
      <artifactId>dropwizard-auth</artifactId>
    </dependency>
    <!-- Example of extension module usage -->
    <dependency>
          <groupId>ru.vyarus.guicey</groupId>
          <artifactId>guicey-eventbus</artifactId>
        </dependency>
</dependencies>
```

Gradle:

```groovy
// declare guice and ext modules without versions 
dependencies {
    implementation platform('ru.vyarus.guicey:guicey-bom:{{ gradle.ext }}')
    // uncomment to override dropwizard and its dependencies versions    
    //implementation platform('io.dropwizard:dropwizard-dependencies:{{ gradle.dropwizard }}')

    implementation 'ru.vyarus:dropwizard-guicey'
    // For example, using dropwizard module (without version)
    implementation 'io.dropwizard:dropwizard-auth'
    // Example of extension module usage
    implementation 'ru.vyarus.guicey:guicey-eventbus' 
}
    
```

## Dependencies override

You may override BOM version for any dependency by simply specifying exact version in dependecy declaration section.

If you want to use newer version (then provided by guicey BOM) of dropwizard or guice then import also their BOMs directly:

* `io.dropwizard:dropwizard-dependencies:$VERSION` for dropwizard
* `com.google.inject:guice-bom:$VERSION` for guice
