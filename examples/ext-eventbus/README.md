### Guava EventBus integration example

Use [eventbus guicey extension](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-eventbus) to:
* automatically register listeners
* bind eventbus (for publication)
* print available listeners to console

Installation:

```java
GuiceBundle.builder()
      .bundles(new EventBusBundle())
```

[EventListener](src/main/java/ru/vyarus/dropwizard/guice/examples/service/EventListener.java) listens for 
`FooEvent`, `BarEvent` and `BaseEvent` - base class for both events.

Note that listeners inside `EventListener` registered because it's implicitly registered as guice bean by
injection in `SampleResource`. 

[SampleResource](src/main/java/ru/vyarus/dropwizard/guice/examples/resource/SampleResource.java) 
used to trigger both events and receive overall calls stats.