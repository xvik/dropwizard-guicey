# Plugin installer

!!! summary ""
    CoreInstallersBundle / [PluginInstaller](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/feature/plugin/PluginInstaller.java)

Allows automatic gathering of multiple implementations of some interface into bindable set or map (dynamic plugins case).  

## Recognition

Detects classes annotated with guicey `#!java @Plugin` annotation and bind them into set or map using guice 
[multibindings](https://github.com/google/guice/wiki/Multibindings) mechanism.

Suppose you have plugin interface `#!java public interface PluginInterface`.

Annotate plugin implementations with `#!java @Plugin`:

```java
@Plugin(PluginInterface.class)
public class PluginImpl1 implements PluginInterface
```

Now all implementations could be autowired as

```java
@Inject Set<PluginInterface> plugins;
```

!!! warning
    At least one implementation must be provided because otherwise guicey will not be able to register
    Set<PluginInterface> binding and guice startup will fail.
    If no plugins situation is possible, then you will have to manually register empty (default)
    plugins binding: 
    ```java
    public class MyModule extends AbstractModule {    
        @Override
        protected configure() {
            Multibinder.newSetBinder(binder(), PluginInterface.class);
        }
    }
    ```
    Guicey can't register empty plugin set for you because it's impossible to know what plugins are you expecting.

### Named plugins

Sometimes it's required to have named plugin mapping: to bind, Map<String, PluginClass> instead of simple set.
For example, when you have multiple authorization providers and each provider implementation must be registered with name.

Most likely, you would use enum for keys:

```java
public enum PluginKey {
    FIRST, SECOND
}
```

Custom plugin annotation needs to be defined to use new keys:

```java
@Plugin(PluginInterface.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyPlugin {
    PluginKey value();
}
```

!!! note
    Annotation itself is annotated with `#!java @Plugin`, defining target plugin interface.
    Guicey will detect your custom annotation usage by analyzing its annotations. 

Annotating plugin implementation:

```java
@MyPlugin(PluginKey.FIRST)
public class PluginImpl1 implements PluginInterface
```

All plugins could be referenced as map:

```java
@Inject Map<PluginKey, PluginInterface> plugins;
```

!!! note
    It's not required to use enum as key. Any type could be set in your custom annotation. 
    For example, string key could be used: 
    ```java
    public @interface MyPlugin {
        String value();
    }
    
    @MyPlugin("first")
    public class PluginImpl1 implements PluginInterface
    
    @Inject Map<String, PluginInterface> plugins;
    ```

!!! warning
    As with simple plugin bindings, at least one plugin must be registered so guice could create map binding.
    Otherwise, you need to manually declare empty (default) plugins map binding:
    ```java
    MapBinder.newMapBinder(binder, keyType, pluginType);
    ```
