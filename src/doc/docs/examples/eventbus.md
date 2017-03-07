# Guava EventBus integration

Example of [guicey-eventbus](../extras/eventbus.md) extension usage.

!!! note ""
    Example [source code](https://github.com/xvik/dropwizard-guicey-examples/tree/master/eventbus)
    
[Eventbus extension](../extras/eventbus.md) used for:

* automatic listeners registration
* bind eventbus instance in guice context (for publication)
* print available listeners to console

## Configuration

Additional dependency required:

```groovy
compile 'ru.vyarus.guicey:guicey-eventbus:0.2.1'
```

!!! note
    guicey-eventbus version could be managed with [BOM](../extras/bom.md)

Register eventbus bundle:

```java
GuiceBundle.builder()
      .bundles(new EventBusBundle())
```

## Event

Events are simple POJO. Create event classes with properties you need (or without everything):

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
    For simplicity using events without properties.

## Publication

Inject eventbus instance for publication:

```java
@Inject EventBus eventbus;

public void someAction() {
    ...
    eventbus.post(new FooEvent());
}
```

## Listening

Listener method must be annotated with `@Subscribe` and contain only one parameter (target event type):

```java
@Subscribe
public void onFooEvent(FooEvent event) {}

@Subscribe
// listen for all events of type (FooEvent, BarEvent)
public void onMultipleEvents(BaseEvent event) {}
```

!!! attention
    Listener method will be registered only for "known" guice beans. That means any extension
    or manually declared guice bean (using module) or bean created with guice AOT (because it's declared
    as dependency for other bean) will be searched for listener methods.
         
See [complete example](https://github.com/xvik/dropwizard-guicey-examples/tree/master/eventbus)         