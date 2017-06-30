# Governator integration

Include the [Netflix Governator](https://github.com/Netflix/governator) dependency:

```groovy
compile "com.netflix.governator:governator:1.5.11"
```

Governator [owns injector creation](https://github.com/Netflix/governator/wiki/Getting-Started#quick-start), 
so we need to create custom guicey `InjectorFactory`

```java
public class GovernatorInjectorFactory implements InjectorFactory {
    public Injector createInjector(final Stage stage, final Iterable<? extends Module> modules) {
        return LifecycleInjector.builder().withModules(modules).inStage(stage).build().createInjector();
    }
}
```

Configure the new factory in the guice bundle:

```java
@Override
public void initialize(Bootstrap<Configuration> bootstrap) {
    bootstrap.addBundle(GuiceBundle.builder()
            .injectorFactory(new GovernatorInjectorFactory())
            .enableAutoConfig("com.mycompany.myapp")
            ...
            .build()
    );
}
```

!!! note
    Auto scan is enabled and managed bean, described below, will be discovered and installed automatically (assuming its inside scanned package).

## Governator Lifecycle
Many Governator enhancements are only available when the Governator [LifecycleManager](http://netflix.github.io/governator/javadoc/index.html?com/netflix/governator/lifecycle/LifecycleManager.html) 
is properly [started and closed](https://github.com/Netflix/governator/wiki/Getting-Started#just-a-bit-more) 
with the application. 

Use dropwizard's [managed object](http://dropwizard.io/manual/core.html#managed-objects) 
to control governator lifecycle:

```java
import io.dropwizard.lifecycle.Managed;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import com.netflix.governator.lifecycle.LifecycleManager;
import javax.inject.Inject;

public class GovernatorLifecycle implements Managed {

    @Inject
    private LifecycleManager manager;

    @Override
    public void start() throws Exception {
        manager.start();
    }

    @Override
    public void stop() throws Exception {
        manager.close();
    }
}

```

Guicey will find this managed bean, create governator injector (using a custom factory), create a managed bean instance and register it in dropwizard. 
This will "bind" the governator lifecycle to the dropwizard lifecycle.

!!! note
    If you need to control the order which the managed beans are started, use the [@Order annotation](../guide/ordering.md). 
