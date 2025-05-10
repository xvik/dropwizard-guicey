# Debug

All declared setup objects and hooks could be listed with a (declaration) source reference (where possible)
in initialization order.

```java
public static class Test2 extends Base {

    @RegisterExtension
    static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(App.class)
            .setup(Ext1.class, Ext2.class)
            .setup(it -> null, new Ext3())
            .debug()
            .create();

    @EnableSetup
    static TestEnvironmentSetup ext1 = it -> null;
    @EnableSetup
    static TestEnvironmentSetup ext2 = it -> null;
```

```
Guicey test extensions (Test2.):

	Setup objects = 
		Ext1                           	@RegisterExtension.setup(class)                    at r.v.d.g.t.j.d.SetupObjectsLogTest.(SetupObjectsLogTest.java:102)
		Ext2                           	@RegisterExtension.setup(class)                    at r.v.d.g.t.j.d.SetupObjectsLogTest.(SetupObjectsLogTest.java:102)
		<lambda>                       	@RegisterExtension.setup(obj)                      at r.v.d.g.t.j.d.SetupObjectsLogTest.(SetupObjectsLogTest.java:103)
		Ext3                           	@RegisterExtension.setup(obj)                      at r.v.d.g.t.j.d.SetupObjectsLogTest.(SetupObjectsLogTest.java:103)
		<lambda>                       	@EnableSetup Base#base1                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Base#base1
		<lambda>                       	@EnableSetup Base#base2                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Base#base2
		<lambda>                       	@EnableSetup Test2#ext1                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Test2#ext1
		<lambda>                       	@EnableSetup Test2#ext2                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Test2#ext2
```

Also, applied configuration overrides and modifiers would be shown:

```
Configuration overrides (Test2.):
	                  foo = 2
	                  bar = 11

Configuration modifiers:
		<lambda>                       	@RegisterExtension.configModifiers(obj)            at r.v.d.g.t.j.d.ConfigOverrideLogTest.(ConfigOverrideLogTest.java:100)
		CfgModify1                     	@RegisterExtension.configModifiers(class)          at r.v.d.g.t.j.d.ConfigOverrideLogTest.(ConfigOverrideLogTest.java:101)
		<lambda>                       	@EnableSetup Test2#setup.configModifiers(obj)      at r.v.d.g.t.j.d.ConfigOverrideLogTest.(ConfigOverrideLogTest.java:107)
		CfgModify2                     	@EnableSetup Test2#setup.configModifiers(class)    at r.v.d.g.t.j.d.ConfigOverrideLogTest.(ConfigOverrideLogTest.java:108)
```

!!! important
    Configuration overrides printed **after** application startup because they are
    extracted from system properties (to guarantee exact used value), which is possible
    to analyze only after `DropwizardTestSupport#before()` call.

!!! note
    Configuration prefix for system properties is shown in brackets: `(Test1.)`.
    It simplifies investigation in case of concurrent tests.

Debug could be activated by annotation:

```java
@TestGuiceyApp(value = App.class, debug = true)
```

By builder:

```java
@RegisterExtension
TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(App)
        .debug()
        .create()
```

By setup object:

```java
@EnableSetup
static TestEnvironmentSetup db = ext -> {
            ext.debug();
        };
```

And using system property:

```
-Dguicey.extensions.debug=true
```

There is also a shortcut for enabling system property:

```java
TestSupport.debugExtensions()
```

## Startup performance

To simplify slow tests (slowness) investigations, guicey measures and prints extensions time.

For example, test with application started in beforeAll, with two test methods
(same app for both tests):

```java
@TestGuiceyApp(value = App.class, debug = true)
public class PerformanceLogTest {
    @Test
    public void test1() { ... }
    @Test
    public void test2() { ... }
}
```

```
\\\------------------------------------------------------------/ test instance = 1595d2b2 /
Guicey time after [Before each] of PerformanceLogTest#test1(): 1204 ms

	[Before all]                       : 1204 ms
		Guicey fields search               : 2.03 ms
		Guicey hooks registration          : 0.02 ms
		Guicey setup objects execution     : 1.92 ms
		DropwizardTestSupport creation     : 1.47 ms
		Application start                  : 1172 ms

	[Before each]                      : 0.46 ms
		Guice fields injection             : 0.19 ms


\\\------------------------------------------------------------/ test instance = 45554613 /
Guicey time after [Before each] of PerformanceLogTest#test2(): 1205 ms ( + 0.33 ms)

	[Before each]                      : 0.69 ms ( + 0.23 ms)
		Guice fields injection             : 0.36 ms ( + 0.17 ms)

	[After each]                       : 0.10 ms


\\\---------------------------------------------------------------------------------------------
Guicey time after [After all] of PerformanceLogTest: 1207 ms ( + 2.15 ms)

	[After each]                       : 0.11 ms ( + 0.01 ms)

	[After all]                        : 2.14 ms
		Application stop                   : 1.72 ms
```

There are three reports:

1. Before first test method (see guicey extension startup time)
2. Before the second test method (see guicey time for the second method only)
3. After all (cleanup time)

Only the first report shows all recorded times, next reports only mention time increase.
For example, the second report mentions only `Guice fields injection             : 0.36 ms ( + 0.17 ms)`
Meaning guicey perform fields injection just before the second test, spent 0.17 ms on it
(overall injection time for two injections is 0.36 ms)


## Extensions

It is recommended to use root extension debug option value in the [extensions](setup-object.md).
Current field-bases extensions print recognized fields report when debug is enabled.


```java
@TestGuiceyApp(value = App.class, stup = MySetup.class, debug = true)

public class MySetup implements TestEnvironmentSetup, TestExecutionListener {
    @Override
    public Object setup(TestExtension extension) throws Exception {
        extension.listen(this);
        
        if (extension.isDebug()) {
            System.out.println("Debug info: ...");
        }
    }

    @Override
    public void started(final EventContext context) throws Exception {
        if (context.isDebug()) {
            System.out.println("Debug info: ...");
        }
    }
```