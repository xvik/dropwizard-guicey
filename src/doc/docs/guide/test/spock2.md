# Spock 2

!!! note ""
    [Migration from spock 1](spock.md#migration-to-spock-2)

There is no special extensions for [Spock 2](http://spockframework.org) (like it was for spock 1),
instead I did an extra [integration library](https://github.com/xvik/spock-junit5),
so you can use existing [Junit 5 extensions](junit5.md) with spock.

!!! note
    You are not limited to guicey junit 5 extensions, you can use ([almost](https://github.com/xvik/spock-junit5#what-is-supported)) any junit 5 extensions.
    And you can use any other spock extensions together with junit extensions.


## Setup

You will need the following dependencies (assuming BOM used for versions management):

```groovy
testImplementation 'io.dropwizard:dropwizard-testing'
testImplementation 'org.junit.jupiter:junit-jupiter-api'
testImplementation 'ru.vyarus:spock-junit5'
testImplementation 'org.spockframework:spock-core:2.1-groovy-3.0'
```

## Usage

See [junit 5 extensions docs](junit5.md) for usage details (it's all used the same).

!!! warning
    Junit 5 extensions would not work with `@Shared` spock fields! You can still use
    such fields directly, but don't expect junit 5 extensions to be able to work with such fields (they can't "see" it).

Here is a simple example:

```groovy
@TestDropwizardApp(App)
class MyTest extends Specification {
    
    @Inject MyService service
    
    def "Check something" (ClientSupport client) {
        
        when: "calling rest endpoint"
        def res = client.targetRest("foo/bar").request()
                .buildGet().invoke().readEntity(String.class)
        
        then: "result correct"
        res == "something"
        
        and: "service called"
        service.isCalled()
    }
}
```

!!! tip
    Note that [parameter injection](junit5.md#parameter-injection) will also work in test and fixture (setup/cleanup) methods

Overall, you get best of both worlds: same extensions as in junit 5 (and ability to use all other junit extensions)
and spock expressiveness for writing tests.

## Special cases

Junit 5 doc [describes](junit5.md#dropwizard-startup-error)  [system stubs](https://github.com/webcompere/system-stubs) library
usage. It is completely valid for spock, I'll just show a few examples here on how to:

* Modify (and reset) environment variables
* Modify (and reset) system properties
* Validate system output (e.g. testing logs)
* Intercepting system exit

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
