# Authentication

Example of [dropwizard authentication](https://www.dropwizard.io/en/release-2.0.x/manual/auth.html) usage with guice.

## Simple auth

Using [dropwizard oauth](https://www.dropwizard.io/en/release-2.0.x/manual/auth.html#oauth2) example as basement.
Other auth types are configured in similar way.

```java
@Provider
public class OAuthDynamicFeature extends AuthDynamicFeature {

    @Inject
    public OAuthDynamicFeature(OAuthAuthenticator authenticator, 
                                UserAuthorizer authorizer, 
                                Environment environment) {
        super(new OAuthCredentialAuthFilter.Builder<User>()
                .setAuthenticator(authenticator)
                .setAuthorizer(authorizer)
                .setPrefix("Bearer")
                .buildAuthFilter());

        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder(User.class));
    }

    // classes below may be external (internal for simplicity)
    
    @Singleton
    public static class OAuthAuthenticator implements Authenticator<String, User> {

        @Override
        public Optional<User> authenticate(String credentials) throws AuthenticationException {
            return Optional.fromNullable("valid".equals(credentials) ? new User() : null);        }
    }
    
    @Singleton
    public static class UserAuthorizer implements Authorizer<User> {
        @Override
        public boolean authorize(User user, String role) {
            return user.getName().equals("good-guy") && role.equals("ADMIN");
        }
    }   
}
```

The class is automatically picked up by the [jersey installer](../installers/jersey-ext.md#dynamicfeature).
`OAuthAuthenticator` and `OAuthAuthorizer` are simple guice beans (no special installation required).

Constructor injection is used to obtain required guice managed instances and then configure
authentication the same way as described in dropwizard docs.

If auto configuration is enabled, then the class will be resolved and installed automatically.

!!! note ""
    Complete [OAuth example source](https://github.com/xvik/dropwizard-guicey-examples/tree/master/integration-auth)

## Chained auth

[Chained auth](https://www.dropwizard.io/en/release-2.0.x/manual/auth.html#chained-factories) can be used to support different authentication schemes.

Integration approach is the same as in simple case:

```java
@Provider
public class ChainedAuthDynamicFeature extends AuthDynamicFeature {

    @Inject
    public ChainedAuthDynamicFeature(BasicAuthenticator basicAuthenticator,
                                      OAuthAuthenticator oauthAuthenticator, 
                                      UserAuthorizer authorizer, 
                                      Environment environment) {
        super(new ChainedAuthFilter(Arrays.asList(
                new BasicCredentialAuthFilter.Builder<>()
                            .setAuthenticator(basicAuthenticator)
                            .setAuthorizer(authorizer)
                            .setPrefix("Basic")
                            .buildAuthFilter(),
                new OAuthCredentialAuthFilter.Builder<>()
                            .setAuthenticator(oauthAuthenticator)
                            .setAuthorizer(authorizer)
                            .setPrefix("Bearer")
                            .buildAuthFilter()
        )));                

        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder(User.class));
    }   
}
```

## Polymorphic auth

[Polymorphic auth](https://www.dropwizard.io/en/release-2.0.x/manual/auth.html#multiple-principals-and-authenticators) allows using different auth schemes simultaneously.

Integration approach is the same as in simple case:

```java
@Provider
public class PolyAuthDynamicFeature extends PolymorphicAuthDynamicFeature {

    @Inject
    public PolyAuthDynamicFeature(BasicAuthenticator basicAuthenticator,
                                   OauthAuthenticator oauthAuthenticator,
                                   UserAuthorizer authorizer,
                                   Environment environment) {
        super(ImmutableMap.of(
                  BasicPrincipal.class, new BasicCredentialAuthFilter.Builder<BasicPrincipal>()
                                                .setAuthenticator(basicAuthenticator)
                                                .setAuthorizer(authorizer)
                                                .setRealm("SUPER SECRET STUFF")
                                                .buildAuthFilter(),
                  OAuthPrincipal.class, new OAuthCredentialAuthFilter.Builder<OAuthPrincipal>()
                                                .setAuthenticator(oauthAuthenticator)
                                                .setAuthorizer(authorizer)
                                                .setPrefix("Bearer")
                                                .buildAuthFilter()));             
        
        final AbstractBinder binder = new PolymorphicAuthValueFactoryProvider.Binder<>(
            ImmutableSet.of(BasicPrincipal.class, OAuthPrincipal.class));
        
        environment.jersey().register(binder);
        environment.jersey().register(RolesAllowedDynamicFeature.class);
    }
}
```

