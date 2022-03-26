# 5.5.0 Release Notes

!!! summary ""
    [5.4.0 release notes](http://xvik.github.io/dropwizard-guicey/5.4.0/about/release-notes/)

* [Junit 4 and Spock 1](#junit-4-and-spock-1)
* [Spock 2](#junit-5) 
* [General test utils](#general-test-utils)
* [BOM](#bom)

## Junit 4 and Spock 1

Junit 4 rules and Spock 1 extensions were moved to external modules:

* [guicey-test-junit4](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-test-junit4)
* [guicey-test-spock](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-test-spock)

Both modules considered deprecated because they both rely on deprecated dropwizard junit 4 rule. 

Packages remain the same so all you need to continue using them is to add additional dependency.

```groovy
testImplementation 'ru.vyarus.guicey:guicey-test-junit4'
```

```groovy
testImplementation 'ru.vyarus.guicey:guicey-test-spock'
```

Spock 1 extensions move was required because it was blocking guicey CI tests on JDK 16 and above.
Now guicey use spock 2 for all its tests, and so now JDK 16 and 17 compatibility is completely checked.

Junit 4 rules was moved away to remove direct deprecation marks on rules (instead entire module now considered deprecated).
This should reduce the amount of compilation warnings.

There are also new migration guides:

* [Junit 4 to Junit 5](../guide/test/junit4.md#migrating-to-junit-5)
* [Spock 1 to Spock 2](../guide/test/spock.md#migration-to-spock-2)

## Spock 2

There is no special [Spock 2](https://spockframework.org/spock/docs/2.1/) extensions, like it was with Spock 1.
Instead, a new library was created [spock-junit5](https://github.com/xvik/spock-junit5) to support Junit 5 extensions in general for Spock 2.
You can read more about motivation in the [blog post](https://blog.vyarus.ru/using-junit-5-extensions-in-spock-2-tests)). 

Now only Junit 5 extensions need to be maintained and behaviour will be absolutely the same
in Junit 5 and Spock 2 (and so, in theory, it would not be a problem to move between frameworks).

See new [Spock 2 documentation](../guide/test/spock2.md) for more details.

All tests in guicey itself, guidey-ext and examples were migrated into Spock 2 (so JDK 16 and 17 CI builds now active).

## General test utils

By analogy with dropwizard `DropwizardTestSupport`, `GuiceyTestSupport` class was added.
It might be used for running applications without web context.

Might be useful in case when there is no provided extensions for your test framework.
Also, such classes are useful for startup errors validation (when assertions must be performed after application shutdown).

To simplify common use-cases, new `TestSupport` utility class was added with: 

* simple run methods to run complete or guice-only application
* methods for `DropwizardTestSupport`, `GuiceyTestSupport` and `ClientSupport` objects creation
* methods for accessing injector (like get injector, get bean, process injections on target object)

Overall, `TestSupport` class should provide access to all other test utilities (no need to remember others).

See new [general testing documentation](../guide/test/general.md).

It is also suggested now to use [system-stubs](https://github.com/webcompere/system-stubs) for:

* Changing (and reverting) system properties and environment variables
* Intercepting system exit (dropwizard startup error testing)
* Validating console output (e.g. test logs)

See specialized guides for [junit 5](../guide/test/junit5.md#dropwizard-startup-error)
and [spock 2](../guide/test/spock2.md#special-cases).

## BOM

Guicey BOM have to be changed:

- spock version removed in order to avoid problems downgrading spock version for spock1 module
- system-rules removed because it targets junit4 (ext module provides it)
- groovy libraries removed (newer groovy 2.x was required for spock1 to run on java 11)
- add spock-junit5 version 
