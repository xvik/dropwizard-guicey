# Jersey feature installer

!!! summary ""
    CoreInstallersBundle / [JerseyFeatureInstaller](https://github.com/xvik/dropwizard-guicey/tree/dw-3/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/jersey/JerseyFeatureInstaller.java)

## Recognition

Detects classes implementing `#!java javax.ws.rs.core.Feature` and register their instances in jersey.

It may be useful to configure jersey inside guice components:

```java

public class MyClass {
    ...   
    public static class ConfigurationFeature implements Feature {
        @Override
        public boolean configure(FeatureContext context) {
            context.register(RolesAllowedDynamicFeature.class);
            context.register(new AuthValueFactoryProvider.Binder(User.class));
            return true;
        }
    }
}
```

!!! note ""
    Inner classes are also recognized by classpath scan.

But often the same could be achieved by injecting `#!java Environment` instance.

```java
@Singleton
public class MyClass {
    
    @Inject
    public MyClass(Environment environment) {
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(
                new AuthValueFactoryProvider.Binder(User.class));
    }    
}
```