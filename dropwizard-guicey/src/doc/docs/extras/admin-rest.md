# Admin REST

Mirror all resources in the admin context: on the admin side, a special servlet simply redirects all incoming requests into the Jersey context.
Hides admin-only resources from the user context: resources work under the admin context and return 404 in the user context.

Such an approach is better than registering a completely separate Jersey context for admin REST because
of no overhead and the simplicity of jersey extensions management.

Features:
* All user-context REST resources are available in the admin context
* Admin-only resources are not visible in the user context

## Setup

Maven:

```xml
<dependency>
  <groupId>ru.vyarus.guicey</groupId>
  <artifactId>guicey-admin-rest</artifactId>
  <version>{{ gradle.version }}</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus.guicey:guicey-admin-rest:{{ gradle.version }}'
```

Omit the version if the Guicey BOM is used.

## Usage

Register bundle:

```java
GuiceBundle.builder()
    .bundles(new AdminRestBundle());
```

In this case, REST is registered either to '/api/*', if the main-context REST is mapped to the root ('/*')
or to the same path as the main-context REST.

To register on a custom path:

```java
.bundles(new AdminRestBundle("/custom/*"));
```

!!! note
    If multiple bundles are registered, only the first registration will be used (due to [de-duplication](../guide/deduplication.md))

### Security

To hide specific resource methods or entire resources in the main context, annotate resource methods
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

This annotated method will return a 404 error when called from the main context, but should function normally
when called from the admin context.
