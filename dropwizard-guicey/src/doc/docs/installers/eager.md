# Eager singleton installer

!!! summary ""
    CoreInstallersBundle / [EagerSingletonInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/eager/EagerSingletonInstaller.java)

## Recognition

Detects classes annotated with `#!java @EagerSingleton` annotation and registers them in guice injector. 
It is equivalent of eager singleton registration `#!java bind(type).asEagerSingleton()`.

Useful in cases when you have a bean which is not injected by other beans (so guice can't register
it through aot). Normally, you would have to manually register such bean in module.

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

Class will be recognized by eager singleton installer, environment object injected by guice and we manually register listener.

May be used in conjunction with `#!java @PostConstruct` annotations (e.g. using [ext-annotations](https://github.com/xvik/guice-ext-annotations)):
installer finds and register bean and post construct annotation could run some logic. Note: this approach is against guice philosophy and should
be used for quick prototyping only.
