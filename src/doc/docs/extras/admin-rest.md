# Admin REST

All rest resources could be "published" in the admin context too.  This is just an emulation of rest: the same resources 
are accessible in both contexts. On admin side special servlet simply redirects all incoming requests into the jersey context.

Such approach is better than registering a completely separate jersey context for admin rest because
of no overhead and the simplicity of jersey extensions management.

## Configuration

To install admin rest servlet, register bundle:

```java
bootstrap.addBundle(new AdminRestBundle());
```

In this case, rest is registered either to '/api/*', if main context rest is mapped to root ('/*')
or to the same path as main context rest.

To register on a custom path:

```java
bootstrap.addBundle(new AdminRestBundle("/custom/*"));
```

## Security

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

This (annotated) method will return 403 error when called from main context, but should function normally 
when called from the admin context.

This is just the simplest option to control resources access. Any other method may be used (with some security
framework or something else).
