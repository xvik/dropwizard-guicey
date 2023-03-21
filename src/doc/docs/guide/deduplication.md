# Configuration de-duplication

Guice modules, bundles and dropwizard bundles allow registration of multiple instance
of the same type. For example:

```java
.modules(new MyModule("one"), new MyModule("two"))
```

!!! note
    Before, guice did not allow to register multiple modules of the same type, but
    multiple instances support is more logical in context of dropwizard, because 
    dropwizard itself allows registration of multiple bundles of the same type.
    
## Duplicates

But in some cases it is desirable to avoid such registrations. For example, if two bundles 
install the same common bundle it would be installed twice:

```java
public class Feature1Bundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.bundles(new CommonBundle);
    }
}

public class Feature2Bundle implements GuiceyBundle {
    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        bootstrap.bundles(new CommonBundle);
    }
}

GuiceBundle.buider()
    .bundles(new Feature1Bundle(). new Feature2Bundle())
    ...
```

To work around such cases *deduplication mechanism** was introduced: instances of the same 
type are considered duplicate if they are equal. 

## Equals method

In order to resolve "common bundle/module problem" bundle/module must only 
properly implement equals method:

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
    Guicey provide base classes for such cases: `UniqueGuiceyBundle` for unique bundles 
    and `UniqueModule` (or `UniqueDropwizardAwareModule`) for unique guice modules. So bundle above could be simplified to:
    
    ```java
    public class CommonBundle extends UniqueGuiceyBundle { ... }
    ```
    
Equals logic could be more complicated: for example, it may involve constructor parameters
comparison to treat as duplicates only instances with the same parameters.    

## Unique items

When it is impossible to properly implement equals method (for example, because target bundle or module is
3rd party) you can simply explicitly declare them as unique:

```java
GuiceBundle.builder()
    .uniqueItems(Some3rdPartyBundle.class, 
                 Some3rdPartyModule.class)
```  

Now only one instance of `Some3rdPartyBundle` and `Some3rdPartyModule` will be registered
and all other instances considered as duplicate.

## General unique logic

[.uniqueItems()](#unique-items) method above is actually a shortcut for custom deduplication
mechanism registration (most common case).

But you can implement your own deduplication logic and register with: 

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
    In case of override you will only see warning in logs.

## Legacy mode

Old guicey "1 instance per class" behaviour could be recovered with bundled detector:

```java
.duplicateConfigDetector(new LegacyModeDuplicatesDetector())
```           

## Reporting

[Configuration diagnostic report](diagnostic/configuration-report.md) (`.printDiagnosticInfo()`) 
shows all registered instances and ignored duplicates.

For example, if we have module declared to be unique by constructor value:

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

Report would contain:

```
GUICE MODULES =
        VMod                          (com.mycompany) *REG(2/4) 

APPLICATION
    ├── module     VMod                          (com.mycompany)
    ├── module     -VMod                         (com.mycompany) *DUPLICATE
    ├── module     VMod#2                        (com.mycompany)
    ├── module     -VMod#2                       (com.mycompany) *DUPLICATE
```         

Where you can see that 2 of 4 registered modules of type VMod were registered.
Note that instances are numbered (#2) in order of registration (without duplicates) 
so you can always see what bundle were considered as original (and see registration order when
bundles of the same type are registered in different bundles). 

## Limitations

### Guice modules

Transitive guice modules are **not counted** during de-duplication.

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

This **will not work** because guicey is not aware of transitive modules (guicey can only know modules tree on class level, 
but can't see exact instances).

BUT *guice natively support de-duplication of equal modules*, so if your module have proper equals

```java
public class MyModule extends UniqueModule {}
```

Then guice will perform de-duplication itself.

!!! warning
    Guice will perform de-duplication itself only if both `equals` and `hashCode` properly implemented
    (like in `UniqueModule`)  


!!! note
    Guice can also *de-duplicate bindings*: if bindings from different module instances
    are the same then guice will simply ignore duplicate bindings. 

### Dropwizard bundles

Guicey **can see** transitive dropwizard bundles and properly apply de-duplication logic.
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

This **will work** because guicey use special proxy to intercept transitive registrations
(so, essentially, transitive registrations are treated the same as direct)

!!! note
    This means that if you have "common dropwizard bundle" problem, then you can simply 
    register it with guicey and it will be able to properly de-duplicate it.

BUT guicey *does not "see"* directly installed bundles (intentionally!). For example,

```java
bootstrap.addBundle(new MyBundle())
bootstrap.addBundle(GuiceBindle.builder()
                .dropwizardBundles(new OtherBundle())
                .uniqueItems(MyBundle.class)
                .build())
```

This **will not work** because guicey see only directly registered bundles: `OtherBundle` and transitive `MyBundle`,
and so `MyBundle` would be registered twice.  
