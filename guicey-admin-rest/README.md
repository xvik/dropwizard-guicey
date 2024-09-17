# Admin REST

Mirror all resources in admin context: on admin side special servlet simply redirects all incoming requests into the jersey context.
Hides admin-only resources from user context: resource is working under admin context and return 404 on user context.

Such approach is better than registering a completely separate jersey context for admin rest because
of no overhead and the simplicity of jersey extensions management.

Features:
* All user context rest available in admin context
* Admin-only resources not visible in user context

### Setup

Maven:

```xml
<dependency>
  <groupId>ru.vyarus.guicey</groupId>
  <artifactId>guicey-admin-rest</artifactId>
  <version>{guicey.version}</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus.guicey:guicey-admin-rest:{guicey.version}'
```

Omit version if guicey BOM used.


### Usage

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

#### Security

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

This (annotated) method will return 404 error when called from main context, but will function normally 
when called from the admin context.

#### Logs

As admin servlet just redirects to the main context, then all admin rest requests would be logged like this

```
127.0.0.1 - - [17/Sep/2024:09:27:43 +0000] "GET /async HTTP/1.1" 200 5 "-" "Java/17.0.2" 342
```

If custom mapping path is used then admin requests could be identified easily:

```
127.0.0.1 - - [17/Sep/2024:09:27:43 +0000] "GET /api/async HTTP/1.1" 200 5 "-" "Java/17.0.2" 202
```

(here "/async" path showed under "/api" context, whereas in the main context it is mapped on root)

If you need an additional identification, then enable it with `identifyAdminContextInRequestLogs`:

```java
GuiceBundle.builder()
    .bundles(new AdminRestBundle().identifyAdminContextInRequestLogs());
```

With it all admin calls would have " (ADMIN REST)" identity appended to uri:

```
127.0.0.1 - - [17/Sep/2024:09:32:16 +0000] "GET /api/async (ADMIN REST) HTTP/1.1" 200 5 "-" "Java/17.0.2" 202
```
