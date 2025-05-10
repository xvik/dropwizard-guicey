# Testing logs

Guicey provide `RecordLogsHook` for capturing logged messages.

!!! important
    Works only with logback (default dropwizard logger).

For example, suppose some service logs some technical hint at some condition:

```java
public class Service {
    private final Logger logger = LoggerFactory.getLogger(Service.class);
    
    public void foo() {
        ...
        if (someCondition) {
            logger.info("Some technical note");
        }
    }
}
```

Testing that technical note actually logged:

```java
public class LogsTest {
    
    @Test
    public void testLog() {
        RecordedLogHook hook = new RecordedLogHook();
        // start recorder registration
        RecordedLogs logs = hook.record()
                // listen only for one logger
                .logger(Service.class)
                // start recording all logger messages of level INFO and above
                .start(Level.INFO);
        
        // run application with hook
        TestsSupport.build(MyApp.class)
                .hooks(hook)
                .runCore(injector -> {
                    // run method foo (assume log message must appear)
                    injector.getInstance(Service.class).foo();
                });
        
        // one log recorded
        Assertions.assertEquals(1, logs.count());
        // log message appears
        Assertions.assertEquals(1, logs.containing("Some technical note").count());
        // alternative: last message was a technical hint
        Assertions.assertEquals("Some technical note", logs.lastMessage());
    }
}
```

!!! warning
    Such tests could not be run in parallel because logger configuration is global

## Registration

You can register as many recorders as you like. Each recorder could listen one or more
loggers.

To listen all warnings (root logger):

```java
hook.register().start(Level.WARN);
```

To listen all loggers in package:

```java
hook.register().loggers("com.my.package").start(Level.WARN);
```

To listen exact class and package:

```java
hook.register()
        .loggers(SomeClass.class)
        .loggers("com.my.package")
        .start(Level.INFO);
```

## Implementation details

Each recorder registration leads to logging appender registration for a target logger
(or multiple loggers).

If the current logger configuration is higher than required, then **logger would be 
re-configured**. For example, if default logger level is `INFO` and recorder requires `TRACE` messages,
then it would change logger configuration to receive required messages.

!!! tip
    Recorder might be used just to enable required logs, without application 
    logging configuration. This is very useful in tests (to enable `DEBUG` or `TRACE` 
    messages for exact service (or package)): `hook.register().loggers(MyClass.class).start(Level.TRACE)`

During application startup **dropwizard resets loggers two times** and hook would
re-attach appenders to compensate it. You should be able to record all messages from application startup,
except logs from dropwizard bundles, registered BEFORE `GuiceBundle`.

If required, actual recorder object is accessible with `RecordedLog#getRecorder()`:
it provides `attach()` and `destroy()` methods (for attaching and detaching appender).
The hook would call these methods automatically.

## Querying

`RecordedLogs` used to query recorded logs. Root object always contains all recorded events
(for configured loggers).

Recorded logs are accessible in form of raw *event* (`ILoggingEvent`) or pure string *message* 
(formatted messages with arguments).

| Method            | Description                                     | Example                                                                   |
|-------------------|-------------------------------------------------|---------------------------------------------------------------------------|
| `count()`         | Recorded logs count                             | `assertEquals(1, logs.count())`                                           |
| `empty()`         | Events recorded                                 | `assertFalse(logs.empty())`                                               |
| `events()`        | All recorded events                             | `List<ILoggingEvent> events = logs.events()`                              |
| `messages()`      | Messages of all recorded messages               | `List<String> messages = logs.messages()`                                 |
| `has(loggerName)` | Checks if messages from target logger available | `assertTrue(logs.has(Service.class))`, `assertTrue(logs.has("com.some"))` |
| `has(level)`      | Checks if messages of level available           | `assertTrue(logs.has(Level.WARN))`                                        |
| `lastEvent()`     | Last recorded event or null                     | `assertEquals(Level.WARN, logs.lastEvent().getLevel())`                   |
| `lastMessage()`   | Message of the last recorded event or null      | `assertEquals("Something", logs.lastMessage())`                           |

Also, logs could be filtered:

| Filter               | Description                                    | Example                                                      |
|----------------------|------------------------------------------------|--------------------------------------------------------------|
| `level(level)`       | Select events with level                       | `logs.level(Level.WARN)`                                     |
| `logger(loggerName)` | Select events of required loggers              | `logs.logger(Service.class)`, `logs.logger("com.some")`     |
| `containing(String)` | Events where messages contains provided string | `logs.containing("Substring")`                               |
| `matching(regex)`    | Events where messages match provided regex     | `logs.matching("something \\d+")`                            |
| `select(predicate)`  | General events matching predicate              | `logs.select(event -> event.getLevel().equals(Level.TRACE))` |

Filters return another matcher object where all verification and filter methods above could be called
(multiple filters could be applied consequently).

For example, verify count of all messages containing string:

```java
assertEquals(1, logs.containing("Something").count());
```

Or filtering by logger and level (if recorder records multiple loggers):

```java
assertEquals(12, logs.logger("com.some.package").level(Level.WARN).count())
```

## Clear recordings

Recorded logs could be cleared at any time (to simplify exact method logs matching):

```java
// clear logs, recorded during application startup
logs.clear();
// call method
service.foo();
// verify logs appeared during method call
assertEquals(1, logs.containing("Something").count());

// clear again to check logs of another method
logs.clear();
service.boo();
...
```

## Hook methods

Logs from all recorders could be cleared with hook:

```java
hook.clearLogs()
```

To detach all registered appenders:

```java
hook.destroy()
```

!!! note
    Dropwizard resets loggers during startup so manual detach should not be required
    (to avoid keeping stale appenders between tests).