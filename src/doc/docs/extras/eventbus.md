# Guava EventBus integration

!!! summary ""
    [Extensions project](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-eventbus) module


Integrates [Guava EventBus](https://github.com/google/guava/wiki/EventBusExplained) with guice.
 
Features:

* EventBus available for injection (to publish events)
* Automatic registration of listener methods (annotated with `@Subscribe`)
* Console reporting of registered listeners
 
## Setup

[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus.guicey/guicey-eventbus.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus.guicey/guicey-eventbus)

Remove `version` in dependency declaration below if you using [the BOM extensions](bom.md). 

Maven:

```xml
<dependency>
  <groupId>ru.vyarus.guicey</groupId>
  <artifactId>guicey-eventbus</artifactId>
  <version>{{ gradle.ext }}</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus.guicey:guicey-eventbus:{{ gradle.ext }}'
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
    private EventBus eventbus;    
    
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

After server start you should see all registered event listeners in the log:

```
INFO  [2016-12-01 12:31:02,819] ru.vyarus.guicey.eventbus.report.EventsReporter: EventBus subscribers = 

    MyEvent
        com.foo.something.SomeOtherService        

```

!!! note 
    Only subscriptions of beans registered at the time of injector startup will be shown.
    For example, if MyBean has a subscription method but a binding for it is not declared (and no-one depends on it),
    a JIT binding will be created later in time (when bean will be actually used) and will not be reflected in the logs. 

### Consuming multiple events
  
Note that you can build event hierarchies and subscribe to some base event to receive any derived event.   

To receive all events use:

```java
@Subscribe
public void onEvent(Object event){    
}
```
  
## Event bus 

By default, events will be handled synchronously (`bus.push()` waits while all subscribers process).
 
If you want events to be async use custom eventbus:
 
```java
new EventBusBundle(
        new AsyncEventBus(someExecutor)
)
``` 

By default, event listeners are not considered thread safe and no parallel events processing (for single method) 
will be performed. To mark subscriber as thread safe use `@AllowConcurrentEvents`:

```java
@Subscribe
@AllowConcurrentEvents
public void onEvent(MyEvent event)      
```

If a listener method fails to process an event (throws an exception), then other listeners will still be processed
and the exception will be logged. If you want to change this behaviour, set a custom exception 
handler by creating a custom eventbus instance:

```java
new EventBusBundle(
        new EventBus(customExceptionHandler)
)
```

## Listeners recognition

The guice type listener is used to intercept _all_ bean instances and thus looks at every method in the 
class hierarchy; however, only beans that actually have `@Subscribe`rs will be registered with the event bus. 
This process is fast and usually causes no issues. If needed, you can reduce the scope with a 
custom class matcher:
 
```java
new EventBusBundle()
    .withMatcher(Matchers.inSubpackage("some.package"))
```

If you want maximum performance, then you can add a marker annotation (e.g. `@HasEvents`) and reduce
scope to just annotated classes:

```java
new EventBusBundle()
    .withMatcher(Matchers.annotatedWith(HasEvents.class))
```


## Console reporting

You can switch off console reporting (for example, if you have too many listeners):

```java
new EventBusBundle().noReport()
```

!!! note
    Reporting has to use reflection to get subscribers list. If this fails with a newer guava version
    (not yet supported), then simply disable reporting and everything will work as expected.

## Subscribers info bean
`EventSubscribersInfo` is a registered (available for injection) bean that provides active listeners
and used event types. As described above, it uses reflection internally to access the eventbus listeners map. 
It may be useful for testing. 
