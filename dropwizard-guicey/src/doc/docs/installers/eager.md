# Eager singleton installer

!!! summary ""
    CoreInstallersBundle / [EagerSingletonInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/dropwizard-guicey/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/eager/EagerSingletonInstaller.java)

## Recognition

Detects classes annotated with `#!java @EagerSingleton` annotation and registers them in the Guice injector.
It is the equivalent of eager singleton registration `#!java bind(type).asEagerSingleton()`.

Useful in cases when you have a bean that is not injected by other beans (so Guice can't register
it through AOT). Normally, you would have to manually register such a bean in a module.

Most likely, such bean will contain initialization logic.
Ideal for cases not directly covered by installers. For example:

```java
@EagerSingleton
public class MyListener implements LifeCycle.Listener {
    
    @Inject
    public MyListener(Environment environment) {
        environment.lifecicle.addListener(this);
    }
}
```

Class will be recognized by eager singleton installer, the environment object is injected by Guice, and we manually register the listener.

May be used in conjunction with `#!java @PostConstruct` annotations (e.g. using [ext-annotations](https://github.com/xvik/guice-ext-annotations)):
installer finds and registers the bean, and the post-construct annotation could run some logic. Note: this approach is against Guice philosophy and should
be used for quick prototyping only.
