# Health check installer

!!! summary ""
    CoreInstallersBundle / [HealthCheckInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/dropwizard-guicey/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/health/HealthCheckInstaller.java)

Installs [Dropwizard health checks](https://www.dropwizard.io/en/release-5.0.x/manual/core.html#health-checks).

## Recognition

Detects classes extending Guicey `#!java NamedHealthCheck` and registers their instances in the environment.
A custom base class is required because the default `#!java HealthCheck` does not provide a check name, which is required for registration.

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
