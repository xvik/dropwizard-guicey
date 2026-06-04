# Ordering

!!! note
    Guicey always preserve items registration order, which may be changed only
    by using the explicit `@Order` annotation.

## General

Order is natural. For example, `@Order(10)` will be before `@Order(20)`.

When no annotation is present, class order is set to `#!java Integer.MAX_VALUE`, so
all classes without an order annotation *always go last*.

## Extensions order

!!! note
    Not all extensions support ordering: see the specific installer page or
    [installers report](diagnostic/installers-report.md).
    For example, [managed](../installers/managed.md), [servlets](../installers/servlet.md) and
    [filters](../installers/filter.md) installers support order.
    
The most common case for ordering is ordering managed objects. For example:

```java
@Order(20)
public class Managed1 implements Managed { ... }

@Order(10)
public class Managed2 implements Managed { ... }

public class Managed3 implements Managed { ... }
```

They will be ordered as: `Managed2`, `Managed1`, `Managed3`

!!! note
    Guicey remembers extensions registration order:
    
    ```java
    .extensions(Ext1.class, Ext2.class)
    ```

    So when no explicit ordering is defined (or for elements with the same order value)
    registration order will be preserved.

!!! tip
    You can use [diagnostic report](diagnostic/configuration-report.md) to see actual extensions order. 

## Installers order

All bundled [installers](installers.md) are ordered from 0 to ~110 with a gap of 10 between them to let you easily
put your installers between them (if required).

Use the `@Order` annotation to order a custom installer, otherwise it will go after all
default installers.

!!! tip
    You can use [installers report](diagnostic/installers-report.md) to see actual installers order.

## Bundles order

!!! attention
    Bundles can't be explicitly ordered.
    
Bundles are transitive and transitive registrations appear at the middle of bundle configuration,
so it is physically impossible to order bundles.

Still, there is an implicit order of bundle processing:

* Manually registered bundles (including transitive)
* Bundles lookup

But, again, don't count on this order because, for example, a bundle resolved through the lookup
mechanism could also be manually registered and so processed with manual bundles.

## Modules order

!!! attention
    Modules can't be explicitly ordered.

According to Guice: [modules should not contain conditional logic](https://github.com/google/guice/wiki/AvoidConditionalLogicInModules)
So modules should only register bindings, and order does not matter in that case.
