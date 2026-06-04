# Spock 2

!!! note ""
    [Migration from Spock 1](spock.md#migration-to-spock-2)

There are no special extensions for [Spock 2](https://spockframework.org) (as there were for Spock 1),
instead I created an extra [integration library](https://github.com/xvik/spock-junit5),
so you can use existing [JUnit 5 extensions](junit5/setup.md) with Spock.

!!! note
    You are not limited to Guicey JUnit 5 extensions, you can use ([almost](https://github.com/xvik/spock-junit5#what-is-supported)) any JUnit 5 extensions.
    And you can use any other Spock extensions together with JUnit extensions.


## Setup

You will need the following dependencies (assuming the BOM is used for version management):

```groovy
testImplementation 'ru.vyarus:spock-junit5'
testImplementation 'org.spockframework:spock-core:2.3-groovy-4.0'
testImplementation 'io.dropwizard:dropwizard-testing'
testImplementation 'org.junit.jupiter:junit-jupiter-api'
```

!!! note
    In Gradle you need to explicitly [activate JUnit 5 support](https://docs.gradle.org/current/userguide/java_testing.html#using_junit5) with
    ```groovy
    test {
        useJUnitPlatform()
        ...
    }                    
    ```

## Usage

See the [JUnit 5 extensions docs](junit5/setup.md) for usage details (they are all used the same way).

!!! warning
    JUnit 5 extensions would not work with `@Shared` Spock fields! You can still use
    such fields directly, but don't expect JUnit 5 extensions to be able to work with such fields (they can't "see" them).

Here is a simple example:

```groovy
@TestDropwizardApp(App)
class MyTest extends Specification {
    
    @Inject MyService service
    
    def "Check something" (ClientSupport client) {
        
        when: "calling rest endpoint"
        def res = client.targetRest("foo/bar").request()
                .buildGet().invoke().readEntity(String)
        
        then: "result correct"
        res == "something"
        
        and: "service called"
        service.isCalled()
    }
}
```

!!! tip
    Note that [parameter injection](junit5/inject.md#parameter-injection) will also work in test and fixture (setup/cleanup) methods.

Overall, you get the best of both worlds: the same extensions as in JUnit 5 (and the ability to use all other JUnit extensions)
and Spock expressiveness for writing tests.

## Testing commands

!!! warning
    Command execution overrides System I/O and so can't run in parallel with other tests!

    Use [`@Isolated`](https://spockframework.org/spock/docs/2.0/all_in_one.html#_isolated_execution) 
    on such tests to prevent parallel execution with other tests

Command execution is usually a short-lived action, so it is not possible to
write an extension for it. A command could be tested only with a generic utility:

```java
def "Test command execution"() {
    
    when: "executing command"
    CommandResult result = TestSupport.buildCommandRunner(App)
            .run("cmd", "-p", "param")
    
    then: "success"
    result.successful
}
```

Read more details in the [JUnit 5 guide](junit5/command.md)

!!! note
    The same utility could be used to test [application startup failures](junit5/startup.md)

## Special cases

The JUnit 5 docs [describe](junit5/startup.md) the [system stubs](https://github.com/webcompere/system-stubs) library
usage. It is completely valid for Spock, I'll just show a few examples here on how to:

* Modify (and reset) environment variables
* Modify (and reset) system properties
* Validate system output (e.g. testing logs)
* Intercept system exit

```groovy
@ExtendWith(SystemStubsExtension)
class StartupErrorTest extends Specification {

    @SystemStub
    SystemExit exit
    @SystemStub
    SystemErr err

    def "Check app crash"() {

        when: "starting app"
        exit.execute(() -> {
            new App().run(['server'] as String[])
        });

        then: "error"
        exit.exitCode == 1
        err.text.contains("Error message text")
    }
}
```

```groovy
@ExtendWith(SystemStubsExtension)
class EnvironmentChangeTest extends Specification {

    @SystemStub
    EnvironmentVariables ENV
    @SystemStub
    SystemOut out
    @SystemStub
    SystemProperties propsReset

    def "Check variables mapping"() {

        setup:
        ENV.set("VAR", "1")
        System.setProperty("foo", "bar") // OR propsReset.set("foo", "bar") - both works the same

        when: 
        // something requiring custom env or property values
        
        then:
        // validate system output (e.g. logs correctness)
        out.text.contains("Some message assumed to be logged")
```

!!! note
    Use [test framework-agnostic utilities](general/general.md) to run application with configuration or to run
    application without web part (for faster test).
