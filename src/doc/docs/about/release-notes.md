# 5.2.0 Release Notes

!!! summary ""
    [5.0.0 release notes](http://xvik.github.io/dropwizard-guicey/5.0.0/about/release-notes/)
    [5.1.0 release notes](http://xvik.github.io/dropwizard-guicey/5.1.0/about/release-notes/)

Mostly bugfixing release, but jersey extensions registration priority fix *may affect behaviour*.

* [General](#general)
* [Jersey extensions priority change](#jersey-extensions-priority-change)
* [New](#new)
* [Bugfixes](#bugfixes)

## General

Dropwizard updated to 2.0.16.

## Jersey extensions priority change

In raw dropwizard registered jersey extension (with environment.jersey().register(MyExceptionMapper.class))
is implicitly qualified as @Custom and always used in priority comparing to default dropwizard providers.

Before, guicey was registering provider extensions without this qualifier and so the default 
dropwizard providers were used in priority (as registered earlier).
For example, it was impossible to register `ExceptionMapper<Throwable>` because dropwizard already registered one.
Now your custom mapper will be used in priority and so it is possible to override default `ExceptionMapper<Throwable>`
(for example).

!!! warning 
    This COULD (unlikely, but still) change application behaviour: your custom provider could be called in more cases.
    But, as this behaviour is the default for raw dropwizard, the change considered as a bugfix.

### Legacy behaviour

In case of problems, you could revert to legacy guicey behaviour with the new option:

```java 
.option(InstallerOptions.PrioritizeJerseyExtensions, false)
```

Even when option disabled, extensions order could be modified with `@Priority` annotation or with
`@Custom` to gain the same effect as with enabled option (but only for some beans).

### @Priority annotation
 
Also, jersey `javax.annotation.Priority` annotation will now take effect for jersey extensions (previously it was ignored).
This could also change your application behaviour (if you are using these annotations for extensions
registered by guicey).

See `javax.ws.rs.Priorities` for jersey default priority constants. 
 
## New

Add guicey `ApplicationStoppedEvent`, fired after application stop (called by jersey lifecycle stopped event).
Suitable to perform actions after application shutdown (very rare case).

Note that existing `ApplicationShotdownEvent` is called just before application shutdown start
(and suitable for cleanup application logic). 

## Bugfixes

* [127](https://github.com/xvik/dropwizard-guicey/issues/127) - remove direct usage of logback to  unlock logger switching
* [87](https://github.com/xvik/dropwizard-guicey/issues/87) - fix config introspection infinite recursion for EnumMap fields
* [97](https://github.com/xvik/dropwizard-guicey/issues/97) - fix jersey @Priority annotation support 