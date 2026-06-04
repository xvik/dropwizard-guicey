# JUnit 5

!!! note ""
    JUnit 5 [user guide](https://junit.org/junit5/docs/current/user-guide/) | [Migration from JUnit 4](../junit4.md#migrating-to-junit-5)

## Setup

You will need the following dependencies (assuming BOM used for versions management):

```groovy
testImplementation 'io.dropwizard:dropwizard-testing'
testImplementation 'org.junit.jupiter:junit-jupiter-api'
testRuntimeOnly 'org.junit.jupiter:junit-jupiter'
```

!!! tip
    If you already have JUnit 4 or Spock tests, you can activate the [vintage engine](https://junit.org/junit5/docs/current/user-guide/#migrating-from-junit4)
    so all tests could work  **together** with JUnit 5:
    ```groovy
    testRuntimeOnly 'org.junit.vintage:junit-vintage-engine'
    ```

!!! note
    In Gradle you need to explicitly [activate JUnit 5 support](https://docs.gradle.org/current/userguide/java_testing.html#using_junit5) with
    ```groovy
    test {
        useJUnitPlatform()
        ...
    }                    
    ```

!!! warning
    JUnit 5 annotations are **different** from JUnit 4, so if you have both JUnit 5 and JUnit 4
    make sure the correct classes (annotations) are used for JUnit 5 tests:
    ```java
    import org.junit.jupiter.api.Assertions;
    import org.junit.jupiter.api.Test;
    ```    

## Dropwizard extensions compatibility

Guicey extensions *could be used together with Dropwizard
extensions*. They could be used to start multiple Dropwizard applications.

For example:

```java
// run app (injector only)
@TestGuiceyApp(App.class)
// activate dropwizard extensions
@ExtendWith(DropwizardExtensionsSupport.class)
public class ClientSupportGuiceyTest {

    // Use dropwizard extension to start a separate server
    // It might be the same application or different 
    // (application instances would be different in any case)
    static DropwizardAppExtension app = new DropwizardAppExtension(App.class);

    @Test
    void testLimitedClient(ClientSupport client) {
        Assertions.assertEquals(200, client.target("http://localhost:8080/dummy/")
                .request().buildGet().invoke().getStatus());
    }
}
```

!!! info
    There is a difference in extensions implementation.

    Dropwizard extensions work as:
    the JUnit extension `@ExtendWith(DropwizardExtensionsSupport.class)` looks for fields
    implementing `DropwizardExtension` (like `DropwizardAppExtension`) and starts/stops them according to the test lifecycle.

    Guicey extensions are implemented as separate JUnit extensions (only some annotated fields are manually searched:
    hooks, setup objects, and special extensions).
    Also, Guicey extensions implement JUnit parameter injection (for test and lifecycle methods).




