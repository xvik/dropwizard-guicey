# Health check installer

!!! summary ""
    CoreInstallersBundle / [HealthCheckInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/health/HealthCheckInstaller.java)        

Installs [dropwizard health check](http://www.dropwizard.io/1.0.6/docs/manual/core.html#health-checks).

## Recognition

Detects classes extending guicey `#!java NamedHealthCheck` and register their instances in environment.
Custom base class is required, because default `#!java HealthCheck` did not provide check name, which is required for registration.

```java
public class MyHealthCheck extends NamedHealthCheck {
    
    @Inject
    private MyService service;
    
    @Override
    protected Result check() throws Exception {
        if (service.isOk()) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Service is not ok");
        }
    }
    
    @Override
    public String getName() {
        return "my-service";
    }
}
```
