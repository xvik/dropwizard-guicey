# Configuration hooks

Guicey provides special mechanism for external configuration:

```java
public class MyHook implements GuiceyConfigurationHook {
    @Override
    public void configure(GuiceBundle.Builder builder) {
        builder.bundles(new AdditinoalBundle());    
    }       
}
```

Hook implementation receive *the same builder instance* as used in `GuiceBundle` 
and so it is able to change everything (for example, `GuiceyBundle` abilities are limited).

!!! note
    Hooks intended to be used in tests (e.g. to activate some diagnostic tools or disable application parts)
    and to activate diagnostic/tracking tools on compiled application.

## Registration

When hook implemented as separate class it could be registered directly:

```java
new MyHook().register()
```

For lambda-hook registration use:

```java
ConfigurationHooksSupport.register(builder -> { 
    // do modifications 
})
```

## Lifecycle

All hooks are executed just before guice bundle builder finalization (when you call last `.build()` 
method of `GuiceBundle`). Hooks registered after this moment will simply be never used.           

## Tests

!!! note
    For hooks usage in tests there is a [special test support](test/overview.md) (spock and junit).

In context of tests, the most important hook modifications are:
 
* Change options
* Disable any bundle, installer, extension, module
* Register disable predicate (to disable features by package, registration source etc.)
* Override guice bindings
* Register additional bundles, extensions, modules (usually test-specific, for example guicey tests register 
additional guice module with restricted guice options (disableCircularProxies, requireExplicitBindings, requireExactBindingAnnotations))

## Diagnostic

Hooks could be activated on compiled application with a system property:

```
-Dguicey.hooks=com.company.MyHook1,com.company.MyHook2 
```

To simplify usage you can register hook alias:

```java    
GuiceBindle.builder()
    .hookAlias("alias1", MyHook1.class)
    .hookAlias("alias2", MyHook2.class)   
```

Now, hooks could be activated as: 

```
-Dguicey.hooks=alias1,alias2 
```

Moreover, you will always see these aliases in application startup logs (to not forget about this abilities):

```
INFO  [2019-09-16 16:26:35,229] ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport: Available hook aliases [ -Dguicey.hooks=alias ]: 

    alias1                    com.company.MyHook1
    alias2                    com.company.MyHook2
```                                          

!!! note
    By default, guicey register [diagnostic hook](diagnostic/diagnostic-tools.md#diagnostic-hook)
    to easily activate dignostic reports on compiled application:
    ```
    -Dguicey.hooks=diagnistic
    ```