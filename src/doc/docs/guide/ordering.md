# Ordering

Guicey `@Order` annotation should be used to order extensions and installers.

## General

Order is natural. For example, `@Order(10)` will be before `@Order(20)`.

When no annotation present, class order set to `#!java Integer.MAX_VALUE`, so
all classes without order annotation are always goes last.

## Extensions order

!!! note
    Not all extensions supports ordering: look specific installer page for details.
    For example, managed, lifecycle, servlets and filters installers support order.
    
!!! tip
    Installers supporting ordering implement [`Ordered`](installers.md#ordering) interface.

The most common case for ordering is ordering managed objects. For example:

```java
@Order(20)
public class Managed1 implements Managed { ... }

@Order(10)
public class Managed2 implements Managed { ... }

public class Managed3 implements Managed { ... }
```

Will be ordered as: `Managed2`, `Managed1`, `Managed3`

!!! note
    Guicey remembers extensions registration order:
    ```java
    .extensions(Ext1.class, Ext2.class)
    ```
    So when no explicit ordering defined (or for elements with the same order value)
    registration order will be preserved.

!!! tip
    Console reporters for most extensions report extensions in correct order.
    You can use diagnostic reporting to be sure about actual extensions order. 

## Installers order

All bundled [installers](installers.md) are ordered from 0 to ~110 with gap 10 between them to let you easily
put your installers between (if required).

Use `@Order` annotation to order custom installer, otherwise it will go after all
default installers.

## Bundles order

!!! attention
    Guicey bundles does not support ordering.
    
It makes no sense to order [guicey bundles](bundles.md) because they simply register other extensions and installers.
You can always order installers and extensions registered by bundles.

Moreover, bundles are transitive, so it would be extremely hard to understand actual order:
for example, when bundle registered both transitively and manually.

There are implicit order of bundle processing:

* Manually registered bundles (including transitive)
* Dropwizard bundles (when recognition enabled)
* Bundles lookup

But, again, don't count on this order because, for example, bundle resolved through lookup
mechanism could be also manually registered and so installed as manual bundle.

## Modules order

!!! attention
    Guicey does not support modules ordering.
    
It makes no sense to order guice modules because they simply register bindings.
According to guice guice: [modules should not contain conditional logic](https://github.com/google/guice/wiki/AvoidConditionalLogicInModules)

So all that modules should do is registering bindings and order does not matter in that case.

Modules, registered directly in guice bundle, must be executed before modules, registered in bundles 
(because registration order is preserved).