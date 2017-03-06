# Guava EventBus integration

!!! summary ""
    [Extensions project](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-eventbus) module


Integrates [Guava EventBus](https://github.com/google/guava/wiki/EventBusExplained) with guice.
 
Features:

* EventBus available for injection (to publish events)
* Automatic registration of listener methods (annotated with `@Subscribe`)
* Console reporting of registered listeners
 
## Setup

[![JCenter](https://img.shields.io/bintray/v/vyarus/xvik/dropwizard-guicey-ext.svg?label=jcenter)](https://bintray.com/vyarus/xvik/dropwizard-guicey-ext/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus.guicey/guicey-eventbus.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus.guicey/guicey-eventbus)

Avoid version in dependency declaration below if you use [extensions BOM](bom.md). 

Maven:

```xml
<dependency>
  <groupId>ru.vyarus.guicey</groupId>
  <artifactId>guicey-eventbus</artifactId>
  <version>0.2.1</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus.guicey:guicey-eventbus:0.2.1'
```

See the most recent version in the badge above.

## Usage

Register bundle:

```java
GuiceBundle.builder()        
        .bundles(new EventBusBundle())
        ...
```

Create event:

```java
public class MyEvent {
    // some state
}
```

Inject `EventBus` to publish new events.

```java
public class SomeService {
    @Inject
    private EVentBus eventbus;    
    
    public void inSomeMethod() {
        evetbus.post(new MyEvent());
    }
}
```

Listen for event:

```java
public class SomeOtherService {
    
    @Subscribe
    public void onEvent(MyEvent event) {
         // handle event   
    }
}
```

After server start you should see all registered event listeners in log:

```
INFO  [2016-12-01 12:31:02,819] ru.vyarus.guicey.eventbus.report.EventsReporter: EventBus subscribers = 

    MyEvent
        com.foo.something.SomeOtherService        

```

!!! note 
    Only subscriptions of beans registered at the time of injector startup will be shown.
    For example, if MyBean has subscription method but binding for it not declared (and noone depends on it)
    then JIT binding will be created only somewhere later in time (when bean will be actually used) and 
    so listener registration happen after server startup and will not be shown in console report.

### Consuming multiple events
  
Note that you can build event hierarchies and subscribe to some base event to receive any derived event.   

To receive all events use:

```java
@Subscribe
public void onEvent(Object event){    
}
```
  
## Event bus 

By default, events will be handled synchronously (`bus.push()` waits while all subscribers processed).
 
If you want events to be async use custom eventbus:
 
```java
new EventBusBundle(
        new AsyncEventBus(someExecutor)
)
``` 

By default, event listeners considered not thread safe and so no parallel events processing (for single method) 
will be performed. To mark subscriber as thread safe use `@AllowConcurrentEvents`:

```java
@Subscribe
@AllowConcurrentEvents
public void onEvent(MyEvent event)      
```

If listener method will fail to process event (throw exception) then other listeners will still be processed
and failed listener exception will be logged. If you want to change this behaviour set custom exception 
handler by creating custom eventbus instance:

```java
new EventBusBundle(
        new EventBus(customExceptionHandler)
)
```

## Listeners recognition

Guice type listener used to intercept all beans instances. Each bean instance is registered in eventbus: 
it's valid behaviour for eventbus and only beans with actual listener methods will be registered.

But, it means that each bean class is checked: every method in class hierarchy. This is very fast and
does not make problems for most of the cases. But, if you want, you can reduce the scope for checking by
specifying custom class matcher:
 
```java
new EventBusBundle()
    .withMatcher(Matchers.inSubpackage("some.package"))
```

This will only check beans in class and subpackages.

If you want maximum performance, then you can add extra marker annotation (e.g. `@HasEvents`) and reduce
scope to just annotated classes:

```java
new EventBusBundle()
    .withMatcher(Matchers.annotatedWith(HasEvents.class))
```


## Console reporting

You can switch off console reporting (for example, if you have too much listeners):

```java
new EventBusBundle().noReport()
```

Important moment: reporting has to use reflection to get subscribers list. If reflection will fail with newer guava version
(not yet supported), then simply disable reporting and everything will work.

## Subscribers info bean

Special guice bean registered and available for injection: `EventSubscribersInfo`.
With it you can get active listeners and used event types. Reporting use it for console report.
It may be useful for unit tests.

As described above, internally it use reflection to access eventbus listeners map. 
