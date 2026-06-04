# Configuration hooks

Guicey provides a special mechanism for external configuration:

```java
public class MyHook implements GuiceyConfigurationHook {
    @Override
    public void configure(GuiceBundle.Builder builder) throws Exception {
        builder.bundles(new AdditinoalBundle());
    }
}
```

A hook implementation receives *the same builder instance* as used in `GuiceBundle`
and so it is able to change everything (for example, `GuiceyBundle` abilities are limited).

!!! note
    Hooks are intended to be used in tests (e.g. to activate some diagnostic tools or disable application parts)
    and to activate diagnostic/tracking tools on a compiled application.

## Registration

When a hook is implemented as a separate class, it can be registered directly:

```java
new MyHook().register()
```

For lambda hook registration, use:

```java
ConfigurationHooksSupport.register(builder -> {
    // do modifications
})
```

## Lifecycle

All hooks are executed just before Guice bundle builder finalization (when you call the last `.build()`
method of `GuiceBundle`). Hooks registered after this moment will simply never be used.

## Tests

!!! note
    For hook usage in tests, there is a [special test support](test/overview.md) (Spock and JUnit).

In the context of tests, the most important hook modifications are:

* Change options
* Disable any bundle, installer, extension, module
* Register a disable predicate (to disable features by package, registration source, etc.)
* Override Guice bindings
* Register additional bundles, extensions, and modules (usually test-specific; for example, Guicey tests register
an additional Guice module with restricted Guice options (disableCircularProxies, requireExplicitBindings, requireExactBindingAnnotations))

## Diagnostic

Hooks can be activated on a compiled application with a system property:

```bash
-Dguicey.hooks=com.company.MyHook1,com.company.MyHook2
```

To simplify usage, you can register a hook alias:

```java
GuiceBindle.builder()
    .hookAlias("alias1", MyHook1.class)
    .hookAlias("alias2", MyHook2.class)
```

Now, hooks can be activated as:

```bash
-Dguicey.hooks=alias1,alias2
```

Moreover, you will always see these aliases in application startup logs (to avoid forgetting about these capabilities):

```text
INFO  [2019-09-16 16:26:35,229] ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport: Available hook aliases [ -Dguicey.hooks=alias ]:

    alias1                    com.company.MyHook1
    alias2                    com.company.MyHook2
```

!!! note
    By default, Guicey registers the [diagnostic hook](diagnostic/diagnostic-tools.md#diagnostic-hook)
    to easily activate diagnostic reports on a compiled application:
    ```bash
    -Dguicey.hooks=diagnistic
    ```
