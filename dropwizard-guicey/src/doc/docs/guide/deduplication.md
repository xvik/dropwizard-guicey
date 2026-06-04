# Configuration de-duplication

Guice modules, bundles, and Dropwizard bundles allow registration of multiple instances
of the same type. For example:

```java
.modules(new MyModule("one"), new MyModule("two"))
```

!!! note
    Before, Guice did not allow registration of multiple modules of the same type, but
    supporting multiple instances is more logical in the context of Dropwizard, because
    Dropwizard itself allows registration of multiple bundles of the same type.

## Duplicates

But in some cases it is desirable to avoid such registrations. For example, if two bundles
install the same common bundle, it would be installed twice:

```java
public class Feature1Bundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) throws Exception {
        bootstrap.bundles(new CommonBundle);
    }
}

public class Feature2Bundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) throws Exception {
        bootstrap.bundles(new CommonBundle);
    }
}

GuiceBundle.buider()
    .bundles(new Feature1Bundle(). new Feature2Bundle())
    ...
```

To work around such cases, a **de-duplication mechanism** was introduced: instances of the same
type are considered duplicate if they are equal.

## Equals method

In order to resolve the "common bundle/module problem", a bundle/module must only
properly implement the `equals` method:

```java
public class CommonBundle implements GuiceyBundle {
    ...

    @Override
    public boolean equals(final Object obj) {
        return obj != null && getClass().equals(obj.getClass());
    }
}
```

!!! tip
    Guicey provides base classes for such cases: `UniqueGuiceyBundle` for unique bundles
    and `UniqueModule` (or `UniqueDropwizardAwareModule`) for unique Guice modules. So the bundle above could be simplified to:

    ```java
    public class CommonBundle extends UniqueGuiceyBundle { ... }
    ```

Equals logic could be more complicated: for example, it may involve constructor parameters
comparison to treat as duplicates only instances with the same parameters.

## Unique items

When it is impossible to properly implement the `equals` method (for example, because the target bundle or module is
3rd-party), you can simply explicitly declare them as unique:

```java
GuiceBundle.builder()
    .uniqueItems(Some3rdPartyBundle.class,
                 Some3rdPartyModule.class)
```

Now only one instance of `Some3rdPartyBundle` and `Some3rdPartyModule` will be registered
and all other instances are considered duplicates.

## General unique logic

The [.uniqueItems()](#unique-items) method above is actually a shortcut for custom de-duplication
mechanism registration (the most common case).

But you can implement your own de-duplication logic and register it with:

```java
GuiceBundle.builder()
    ...
    .duplicateConfigDetector((List<Object> registered, Object newItem) -> {
         if (newItem isntanceof Some3rdPartyBundle) {
             // decide if item collide with provided registered instances (of the same type)
             return detectedDuplicate // instance that registered is duplicate to or null to accept item
         }
         // allow instance registration
         return null;
    })
```

!!! important
    This does not override [equals method](#equals-method) logic: custom de-duplication mechanism
    is called only after equals check.

!!! warning
    You can't use `.duplicateConfigDetector()` and `.uniqueItems()` at the same time - one would override another (depends on order).
    In case of an override, you will only see a warning in the logs.

## Legacy mode

Old Guicey "1 instance per class" behavior could be restored with the bundled detector:

```java
.duplicateConfigDetector(new LegacyModeDuplicatesDetector())
```

## Reporting

[Configuration diagnostic report](diagnostic/configuration-report.md) (`.printDiagnosticInfo()`)
shows all registered instances and ignored duplicates.

For example, if we have a module declared to be unique by constructor value:

```java
public class VMod extends AbstractModule {

    private int value;

    public VMod(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VMod && value.equals(obj.value);
    }

}
```

If modules are registered like this:

```java
 GuiceBundle.builder()
    .modules(new VMod(1), new VMod(1), new VMod(2), new VMod(2))
    .printDiagnosticInfo()
    .build()
```

The report would contain:

```text
GUICE MODULES =
        VMod                          (com.mycompany) *REG(2/4)

APPLICATION
    ├── module     VMod                          (com.mycompany)
    ├── module     -VMod                         (com.mycompany) *DUPLICATE
    ├── module     VMod#2                        (com.mycompany)
    ├── module     -VMod#2                       (com.mycompany) *DUPLICATE
```

There you can see that 2 of 4 registered modules of type VMod were registered.
Note that instances are numbered (#2) in order of registration (without duplicates),
so you can always see which bundle was considered the original (and see the registration order when
bundles of the same type are registered in different bundles).

## Limitations

### Guice modules

Transitive Guice modules are **not counted** during de-duplication.

For example,

```java
public class MyModule extends AbstractModule {}

public class SomeModule extends AbstractModule {
    @Override
    public void configure() {
        // transitive module
        install(new MyModule());
    }
}

GuiceBindle.builder()
    .modules(new OtherModule(), new MyModule())
    .uniqueItems(MyModule.class)
```

This **will not work** because Guicey is not aware of transitive modules (Guicey can only know the module tree at the class level,
but can't see exact instances).

But *Guice natively supports de-duplication of equal modules*, so if your module has a proper `equals` method,

```java
public class MyModule extends UniqueModule {}
```

Then Guice will perform de-duplication itself.

!!! warning
    Guice will perform de-duplication itself only if both `equals` and `hashCode` are properly implemented
    (like in `UniqueModule`)


!!! note
    Guice can also *de-duplicate bindings*: if bindings from different module instances
    are the same, then Guice will simply ignore duplicate bindings.

### Dropwizard bundles

Guicey **can see** transitive Dropwizard bundles and properly apply de-duplication logic.
For example,

```java
public class MyBundle implements ConfiguredBundle {}

public class OtherBundle implements ConfiguredBundle {
    @Override
    public void initialize(Bootstrap bootstrap) {
        // transitive bundle
        bootstrap.addBundle(new MyBundle());
    }
}

GuiceBindle.builder()
    .dropwizardBundles(new OtherBundle(), new MyBundle())
    .uniqueItems(MyBundle.class)
```

This **will work** because Guicey uses a special proxy to intercept transitive registrations
(so, essentially, transitive registrations are treated the same as direct ones).

!!! note
    This means that if you have a "common Dropwizard bundle" problem, then you can simply
    register it with Guicey and it will be able to properly de-duplicate it.

But Guicey *does not "see"* directly installed bundles (intentionally!). For example,

```java
bootstrap.addBundle(new MyBundle())
bootstrap.addBundle(GuiceBindle.builder()
                .dropwizardBundles(new OtherBundle())
                .uniqueItems(MyBundle.class)
                .build())
```

This **will not work** because Guicey sees only directly registered bundles: `OtherBundle` and transitive `MyBundle`,
and so `MyBundle` would be registered twice.
