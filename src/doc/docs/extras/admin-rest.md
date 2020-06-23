# Admin REST

!!! summary ""
    [Extensions project](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-admin-rest) module

Mirror all resources in admin context: on admin side special servlet simply redirects all incoming requests into the jersey context.
Hides admin-only resources from user context: resource is working under admin context and return 404 on user context.

Such approach is better than registering a completely separate jersey context for admin rest because
of no overhead and the simplicity of jersey extensions management.

Features:
* All user context rest available in admin context
* Admin-only resources not visible in user context

## Setup

[![JCenter](https://img.shields.io/bintray/v/vyarus/xvik/dropwizard-guicey-ext.svg?label=jcenter)](https://bintray.com/vyarus/xvik/dropwizard-guicey-ext/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus.guicey/guicey-admin-rest.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus.guicey/guicey-admin-rest)

Remove `version` in dependency declaration below if you using [the BOM extensions](bom.md). 

Maven:

```xml
<dependency>
  <groupId>ru.vyarus.guicey</groupId>
  <artifactId>guicey-admin-rest</artifactId>
  <version>5.1.0-2</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus.guicey:guicey-admin-rest:5.1.0-2'
```

See the most recent version in the badge above.

## Usage

Register bundle:

```java
GuiceBundle.builder()
    .bundles(new AdminRestBundle());
```

In this case, rest is registered either to '/api/*', if main context rest is mapped to root ('/*')
or to the same path as main context rest.

To register on a custom path:

```java
.bundles(new AdminRestBundle("/custom/*"));
```

!!! note
    In case if multiple bundles registered, only first registration will be used (due to [de-duplication](../guide/deduplication.md))

### Security

In order to hide specific resource methods or entire resources on the main context, annotate resource methods
or resource classes with the `@AdminResource` annotation.

For example:

```java
@GET
@Path("/admin")
@AdminResource
public String admin() {
    return "admin"
}
```

This (annotated) method will return 404 error when called from main context, but should function normally 
when called from the admin context.
