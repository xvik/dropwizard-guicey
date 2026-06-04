# Guava EventBus integration

Example of [guicey-eventbus](../extras/eventbus.md) extension usage.

!!! abstract ""
    Example [source code](https://github.com/xvik/dropwizard-guicey/tree/master/examples/ext-eventbus)

The [EventBus extension](../extras/eventbus.md) is used for:

* automatic listener registration
* binding the EventBus instance in the Guice context (for publication)
* printing available listeners to the console

## Configuration

An additional dependency is required:

```groovy
implementation 'ru.vyarus.guicey:guicey-eventbus:{{ gradle.version }}'
```

!!! note
    guicey-eventbus version could be managed with [BOM](../extras/bom.md)

Register the EventBus bundle:

```java
GuiceBundle.builder()
      .bundles(new EventBusBundle())
```

## Event

Events are simple POJOs. Create event classes with properties you need (or without everything):

```java
public class FooEvent {
    private String something;
    
    public FooEvent(String something) {
        this.something = something;
    }
    
    public void getSomething() {
        return something;
    }
}
```

Use event hierarchies, if appropriate:

```java
public abstract class BaseEvent {}

public class FooEvent extends BaseEvent {}

public class BarEvent extends BaseEvent {}
```

!!! note ""
    For simplicity, properties are omitted.

## Publication

Inject the EventBus instance to enable publication:

```java
@Inject EventBus eventbus;

public void someAction() {
    ...
    eventbus.post(new FooEvent());
}
```

## Listening

Listener methods must be annotated with `@Subscribe` and contain only one parameter of the target event type:

```java
@Subscribe
public void onFooEvent(FooEvent event) {}

@Subscribe
// listen for all events of type (FooEvent, BarEvent)
public void onMultipleEvents(BaseEvent event) {}
```

!!! attention
    Listener methods will only be registered for "known" Guice beans. That means any extension
    or a manually declared Guice bean (using a module) or a bean created with Guice AOT (because it's declared
    as dependency for other bean) will be searched for listener methods.

See [a complete example](https://github.com/xvik/dropwizard-guicey/tree/master/examples/ext-eventbus)