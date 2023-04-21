# Guicey BOM

Maven BOM contains guicey and guicey ext modules versions. Also includes dropwizard and guice boms.

!!! tip
    BOMs are useful for versions management. After including bom you can simply include required dependencies
    (dropwizard, guice, guicey, guicey-ext) without versions: bom will control all versions.


## Setup

Maven:

```xml
<!-- Implicitly imports Dropwizard and Guice BOMs -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>ru.vyarus.guicey</groupId>
            <artifactId>guicey-bom</artifactId>
            <version>{{ gradle.version }}</version>
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
    implementation platform('ru.vyarus.guicey:guicey-bom:{{ gradle.version }}')
    // uncomment to override dropwizard and its dependencies versions    
    //implementation platform('io.dropwizard:dropwizard-dependencies:{{ gradle.dropwizard }}')

    implementation 'ru.vyarus:dropwizard-guicey'
    // For example, using dropwizard module (without version)
    implementation 'io.dropwizard:dropwizard-auth'
    // Example of extension module usage
    implementation 'ru.vyarus.guicey:guicey-eventbus' 
}
```

Bom includes:

BOM           | Artifact
--------------|-------------------------
Guicey modules | `ru.vyarus.guicey:guicey-[module]`
Dropwizard BOM | `io.dropwizard:dropwizard-bom`
Guice BOM | `com.google.inject:guice-bom`
HK2 bridge | `org.glassfish.hk2:guice-bridge`
Spock-junit5 | `ru.vyarus:spock-junit5`

## Dependencies override

You may override BOM version for any dependency by simply specifying exact version in dependency declaration section.

If you want to use newer version (then provided by guicey BOM) of dropwizard or guice then import also their BOMs directly:

* `io.dropwizard:dropwizard-dependencies:$VERSION` for dropwizard
* `com.google.inject:guice-bom:$VERSION` for guice
