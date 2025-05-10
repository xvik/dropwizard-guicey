# Testing performance (bean tracking)

Tracker records guice bean methods execution:

1. Collect method call arguments and result for each call
2. Log slow methods execution
3. Collect metrics to show overall methods performance (stats)

!!! warning
    Trackers will not work for HK2 beans and for non guice-managed beans (bound by instance)

!!! note
    Initially, trackers were added as a simpler alternative for [mockito spy's
    clumsy result capturing](spies.md#method-result-capture). But, eventually, it evolved into a simple performance tracking
    tool
    (very raw, of course, but in many cases it would be enough).

## Setup

Not strictly required, but trackers provide type-safe search api using mockito, and so
you'll need mockito dependency **only if** you wish to use this api (version may be omitted if dropwizard BOM used):

```groovy
testImplementation 'org.mockito:mockito-core'
```

## Usage

Suppose we have a service:

```java
public static class Service {

    public String get(int id) {
        return "Hello " + id;
    }
}
```

And we want to very indirect service call (when service called by some other service):

```java
TrackersHook hook = TrackersHook();
final Tracker<Service> tracker = hook.track(Service.class)
        .trace(true)
        .add();
TestSupport.build(DefaultTestApp .class)
        .hooks(hook)
        .runCore(injector ->{
            Service service = injector.getInstance(Service.class);

            // call service
            Assertions.assertEquals("Hello 11",service.get(11));

            MethodTrack track = tracker.getLastTrack();
            Assertions.assertTrue(track.toString().contains("get(11) = \"Hello 11\""));
            // object arguments
            Assertions.assertArrayEquals(new Object[] {11},track.getRawArguments());
            // arguments in string form
            Assertions.assertArrayEquals(new String[] {"11"},track.getArguments());
            // raw result
            Assertions.assertEquals("1 call",track.getRawResult());
            // result in string form
            Assertions.assertEquals("1 call",track.getResult());
        });
```

In this example, trace was enabled (optional) and so each method call would be logged like this:

```
\\\---[Tracker<Service>] 0.41 ms      <@1b0e9707> .get(11) = "Hello 11"
```

## Configuration

Tracker registration call `final Tracker<Service> tracker = hook.track(Service.class)`
returns a configuration builder. Final registration appears only after `.add()` method call.

| Option                    | Description                                                                                               | Default   |
|---------------------------|-----------------------------------------------------------------------------------------------------------|-----------|
| trace                     | When enabled, all method calls are printed                                                                | false     |
| slowMethods               | Print warnings about methods executing longer than the specified threshold. Set to 0 to disable warnings. | 5 seconds |
| disableSlowMethodsLogging | Shortcut to disable tracking for slow methods (same as set 0).                                            |           |
| keepRawObjects            | Keep method call arguments and result objects (potentially mutable)                                       | true      |
| maxStringLength           | Max length for a `String` argument or result (cut long strings)                                           | 30        |

### Tracing

Tracing might be useful to see each tracked method call in console with parameters and execution time:

```
\\\---[Tracker<Service>] 0.41 ms      <@1b0e9707> .foo(1) = "1 call"
\\\---[Tracker<Service>] 0.02 ms      <@1b0e9707> .foo(2) = "2 call"
\\\---[Tracker<Service>] 0.12 ms      <@1b0e9707> .bar(1) = "1 bar"
```

It also prints service instance hash, to make obvious method calls on different instances.
Different instances could appear on prototype-scoped beans (default scope).

Enabled with:

```java
hook.track(Service .class)
        .trace(true)
```

!!! note
    Traces are logged with `System.out` to make sure messages are always visible in console.

### Slow methods

By default, tracker would log methods, executed longer than 5 seconds:

```
WARN  [2025-05-09 08:30:38,458] ru.vyarus.dropwizard.guice.test.track.Tracker: 
\\\---[Tracker<Service>] 7.07 ms      <@7634f2b> .foo() = "foo"
```

!!! note
    Slow methods are logged with **logger**, and not `System.out` as traces.

For example, to set slow method for 1 minute:

```java
hook.track(Service .class)
        .slowMethods(1, ChronoUnit.MINUTES)
```

To avoid logging slow methods (shortcut for setting 0 value):

```java
hook.track(Service .class)
        .disableSlowMethodsLogging()
```

### Keeping raw objects

By default, tracker stores all arguments and returned result objects.

Raw arguments could be used to examine complex objects just after the method call.
But, in case of multiple method calls, raw objects might not be actual. For example:

```java
public Service {
    public void foo(List<String> list) {
        list.add("foo" + list.size());
    }
}
```

Here method changes argument state and so, if we call method multiple times, stored arguments
would be useless (as all calls would reference the same list instance):

```java
List<String> test = new ArrayList<>();
service.foo(test);
service.foo(test);

// stored list useless as object was changed after the initial call
List<String> firstCallArg = tracker.getLastTracks(2).get(0).getRawArguments().get(0);
Assertions.assertEquals(2, firstCallArg.size());

// but string representation would still be useful:
String firstCallArgString = tracker.getLastTracks(2).get(0).getArguments().get(0);
Assertions.assertEquals("0[]", firstCallArg.size());

// second call argument string
String firstCallArgString = tracker.getLastTracks(2).get(1).getArguments().get(0);
Assertions.assertEquals("1['foo1']", firstCallArg.size());
```

In case of complex objects (pojo, for example), string representation would only contain
the type and instance hash: `Type@hash` (which is not informative, but the only universal short 
way to describe object).

If tracker used only for performance testing (to accumulate execution time from many runs),
it might make sense to avoid holding raw arguments:

```java
hook.track(Service .class)
        .keepRawObjects(false)
```

### Max length

Methods could consume or return large string, but using large stings for console 
output is not desired. All strings larger then configured size would be cut with "..." suffix:

```
\\\---[Tracker<Service>] 0.08 ms      <@66fb45e5> .baz("largelargelargelargelargelarge...")
```

Changing default:

```java
hook.track(Service .class)
        .maxStringLength(10)
```

## Tracked data

Each call stored as `MethodTrack` and contains raw arguments `getRawArguments()` (which might change over time
if mutable objects used) and string version `getArguments()` (can't change) and same for the result object.
Raw objects are mostly useful in case of immediate check after the method call.

Same for result: `getRawResult()` for raw object and `getResult()` for string version.

Also, there are quoted string versions: `getQuatedResult()` and `getQuatedArguments()`.
These methods are the same as string methods, but all strings are in quotes to clearly see
string bounds (quoted versions useful for console reporting)

Obtaining tracked data:

```java
// all recordings
List<MethodTrack> tracks = tracker.getTracks();
// last 2 calls (in execution order)
List<MethodTrack> tracks = tracker.getLastTracks(2);
// last call
MethodTrack track = tracker.getLastTrack();
```

### Searching

In the case of many recorded executions (for multiple methods), search could be used:

```java
// search by method (any argument value)
tracks = tracker.findTracks(mock -> when(
               mock.foo(Mockito.anyInt()))
         );

// search methods with argument condition ( > 1) 
tracks = tracker.findTracks(mock -> when(
        mock.foo(Mockito.intThat(argument -> argument > 1)))
        );

// search for methods with exact argument value  
tracks = tracker.findTracks(mock -> when(
        mock.foo(11))
        );
```

This method uses Mockito stubbing abilities for search criteria declaration:
easy to use and type-safe search.

### Reset data

Tracked data could be cleared at any time either on tracker: `tracker.clear()`
or using hook (for all trackers): `hook.resetTrackers()`

## Stats

Tracker could aggregate all executions of the same method:

```java
TrackerStats stats = tracker.getStats();
Assertions.assertEquals(1, stats.getMethods().size());

MethodSummary summary = stats.getMethods().get(0);
Assertions.assertEquals("foo", summary.getMethod().getName());
Assertions.assertEquals(Service.class, summary.getService());
Assertions.assertEquals(1, summary.getTracks());
Assertions.assertEquals(0, summary.getErrors());
Assertions.assertEquals(1, summary.getMetrics().getValues().length);
Assertions.assertTrue(summary.getMetrics().getMin() < 1000);
```

Tracker use dropwizard metrics, so stats provide common values like mean time, median time, 95 percentile, etc.

There is a default statistics report implementation, which might be used for console reporting:

```java
System.out.println(tracker.getStats().render());
```

```java
	[service]                                [method]                                           [calls]    [fails]    [min]      [max]      [median]   [75%]      [95%]     
	Service                                  foo(int)                                           2 (2)      0          0.009 ms   0.352 ms   0.352 ms   0.352 ms   0.352 ms
```

Here you can see that 2 instances were used for 2 success calls. Of course max time
would be too large (cold jvm), but with min value you can see more realistic time.
With a high number of executions percentile and mean values would become more realistic.

Here is an example of tracking `GuiceyConfigurationInfo` with activated `.printAllGuiceBindings()` report:

```
	[service]                                [method]                                           [calls]    [fails]    [min]      [max]      [median]   [75%]      [95%]     
	GuiceyConfigurationInfo                  getNormalModuleIds()                               1          0          1.076 ms   1.076 ms   1.076 ms   1.076 ms   1.076 ms  
	GuiceyConfigurationInfo                  getModulesDisabled()                               1          0          0.038 ms   0.038 ms   0.038 ms   0.038 ms   0.038 ms  
	GuiceyConfigurationInfo                  getOverridingModuleIds()                           1          0          0.034 ms   0.034 ms   0.034 ms   0.034 ms   0.034 ms  
	GuiceyConfigurationInfo                  getExtensionsDisabled()                            1          0          0.020 ms   0.020 ms   0.020 ms   0.020 ms   0.020 ms  
	GuiceyConfigurationInfo                  getOptions()                                       1          0          0.005 ms   0.005 ms   0.005 ms   0.005 ms   0.005 ms  
	GuiceyConfigurationInfo                  getData()                                          3          0          0.003 ms   0.006 ms   0.004 ms   0.006 ms   0.006 ms  

```

!!! note
    Methods sorted by slowness 

You can also collect stats for multiple trackers:

```java
TrackerStats overall = new TrackerStats(tracker1, tracker2);
System.out.println(overall.render());
```

## Tracker object access

If required, existing tracker object could be obtained directly from hook:

```java
Tracker<Service> tracker = hook.getTracker(Service.class);
```

This might be useful, for example, to obtain multiple trackers and print overall stats.
But, in the majority of cases, tracker instance, created on registration would be enough.