### Dropwizard authentication configuration example

[Documentation](http://xvik.github.io/dropwizard-guicey/4.2.2/examples/authentication/) describes 
dropwizard auth module usage in general. This example app shows OAuth configuration only.

Dropwizard-auth module required: `implementation 'io.dropwizard:dropwizard-auth`

`OAuthDynamicFeature` is installed automatically by classpath scan.
 
```java
@Singleton
@Provider
public class OAuthDynamicFeature extends AuthDynamicFeature {

    @Inject
    public OAuthDynamicFeature(UserAuthenticator authenticator,
                               UserAuthorizer authorizer,
                               Environment environment) {
        super(new OAuthCredentialAuthFilter.Builder<User>()
                .setAuthenticator(authenticator)
                .setAuthorizer(authorizer)
                .setPrefix("Bearer")
                .buildAuthFilter());

        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder<User>(User.class));
    }
}
```

Auth configuration is almost the same as in dropwizard documentation.

Authenticator and Authorizer are simple guice beans. In the example beans are not described in 
guice module - registration will be automatic on first injection request 
(when `OAuthDynamicFeature` will be instantiated). 

Check token - provide user object
```java
@Singleton
public class UserAuthenticator implements Authenticator<String, User> {

    @Override
    public Optional<User> authenticate(String credentials) throws AuthenticationException {
        return Optional.ofNullable("valid".equals(credentials) ? new User("admin", "ADMIN") : null);
    }
}
```

`@RolesAllowed` annotation support - check if authorized user contains role:
```java
@Singleton
public class UserAuthorizer implements Authorizer<User> {
    @Override
    public boolean authorize(User user, String role) {
        return user.getRoles().contains(role);
    }
}
```


Usage in resources is exactly as described in dropwizard guide:
```java
    // authorization required (or 401 error)
    @GET
    @Path("/auth")
    public String auth(@Auth User user) {
        return user.getName();
    }

    // authorized user must have ADMIN role (or 403 error)
    @GET
    @Path("/adm")
    @RolesAllowed("ADMIN")
    public String admin(@Auth User user) {
        return user.getName();
    }
``` 

Also see sample spock tests using both [GuiceyAppRule](https://github.com/xvik/dropwizard-guicey#testing) (start only guice context - very fast) and 
[DropwizardAppRule](http://www.dropwizard.io/1.0.0/docs/manual/testing.html) (when http server started).