# Testing logs

!!! important
    Works only with logback (default dropwizard logger).

`@RecordLogs` extension could record log messages for one or multiple classes.

For example, for service:

```java
public class Service {
    private final Logger logger = LoggerFactory.getLogger(Service.class);
    
    public void foo() {
        ...
        if (someCondition) {
            logger.debug("Some technical note");
        }
    }
}
```

Testing log appears: 

```java
@Isolated
@TestGucieyApp(App.class)
public class Test {
    
    @RecordLogs(value = Service.class, level = Level.DEBUG)
    RecordedLogs logs;
    
    @Inject
    Service service;
    
    @Test
    public void test() {
        // here some actions with service, involving logging
        service.foo();

        Assertions.assertEquals(1, logs.count());
        Assertions.assertTrue(logs.has(Level.DEBUG));
        Assertions.assertEquals(1, logs.containing("Some technical note").count());
        // alternative: last message was a technical hint
        Assertions.assertEquals("Some technical note", logs.lastMessage());
    }
}
```

Logs could be collected for any custom logger name or entire package:

```java
@RecordLogs(loggers = "ru.vyarus.dropwizard.guice.test", level = Level.TRACE)
RecordedLogs logs;
```

!!! warning
    Such tests could not be run in parallel because logger configuration is global 
    (use `@Isolated` to prevent parallel execution)

## Registration

You can register as many recorders as you like. Each recorder could listen one or more
loggers.

To listen all warnings (root logger):

```java
@RecordLogs(level = Level.WARN)
RecordedLogs logs;
```

To listen all loggers in package:

```java
@RecordLogs(loggers = "com.my.package", level = Level.WARN)
RecordedLogs logs;
```

To listen exact class and package:

```java
@RecordLogs(value = SomeClass.class, loggers = "com.my.package", level = Level.INFO)
RecordedLogs logs;
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
    messages for exact service (or package)): 
    ```java
    @RecordLogs(value = MyClass.class, level = Level.TRACE) 
    RecordedLogs logs;
    ```

During application startup **dropwizard resets loggers two times** and hook would
re-attach appenders to compensate it. You should be able to record all messages from application startup,
except logs from dropwizard bundles, registered BEFORE `GuiceBundle`.

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

By default, recorded logs cleared after each test method. 
This could be disabled with `autoReset` option:

```java
@RecordLogs(value = MyClass.class, level = Level.INFO, autoReset = false) 
RecordedLogs logs;
```

## Debug

When extension debug is active:

```java
@TestGucieyApp(value = App.class, debug = true)
public class Test 
```

All recognized log recorder fields would be logged:

```
Applied log recorders (@RecordLogs) on Test

	#logs                          DEBUG  Service
```
