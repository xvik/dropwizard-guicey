# Junit nested tests

Junit natively supports [nested tests](https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested).

Guicey extensions affects all nested tests below declaration (nesting level is not limited):

```java
@TestGuiceyApp(AutoScanApplication.class)
public class NestedPropagationTest {

    @Inject
    Environment environment;

    @Test
    void checkInjection() {
        Assertions.assertNotNull(environment);
    }

    @Nested
    class Inner {

        @Inject
        Environment env; // intentionally different name

        @Test
        void checkInjection() {
            Assertions.assertNotNull(env);
        }
    }
}
```

!!! note
    Nested tests will use exactly the same guice context as root test (application started only once).

Extension declared on nested test will affect all sub-tests:

```java
public class NestedTreeTest {

    @TestGuiceyApp(AutoScanApplication.class)
    @Nested
    class Level1 {

        @Inject
        Environment environment;

        @Test
        void checkExtensionApplied() {
            Assertions.assertNotNull(environment);
        }

        @Nested
        class Level2 {
            @Inject
            Environment env;

            @Test
            void checkExtensionApplied() {
                Assertions.assertNotNull(env);
            }

            @Nested
            class Level3 {

                @Inject
                Environment envr;

                @Test
                void checkExtensionApplied() {
                    Assertions.assertNotNull(envr);
                }
            }
        }
    }

    @Nested
    class NotAffected {
        @Inject
        Environment environment;

        @Test
        void extensionNotApplied() {
            Assertions.assertNull(environment);
        }
    }
}
```

This way nested tests allows you to use different extension configurations in one (root) class.

Note that extension declaration with `@RegisterExtension` on the root class field would also
be applied to nested tests. Even declaration in non-static field (start application for each method)
would also work.

## Use interfaces to share tests

This is just a tip on how to execute same test method in different environments.

```java
public class ClientSupportDwTest {

    interface ClientCallTest {
        // test to apply for multiple environments
        @Test
        default void callClient(ClientSupport client) {
            Assertions.assertEquals("main", client.targetApp("servlet")
                    .request().buildGet().invoke().readEntity(String.class));
        }
    }

    @TestDropwizardApp(App.class)
    @Nested
    class DefaultConfig implements ClientCallTest {

        @Test
        void testClient(ClientSupport client) {
            Assertions.assertEquals("http://localhost:8080/", client.basePathApp());
        }
    }

    @TestDropwizardApp(value = App.class, configOverride = {
            "server.applicationContextPath: /app",
            "server.adminContextPath: /admin",
    }, restMapping = "api")
    @Nested
    class ChangedDefaultConfig implements ClientCallTest {

        @Test
        void testClient(ClientSupport client) {
            Assertions.assertEquals("http://localhost:8080/app/", client.basePathApp());
        }
    }
}
```

Here test declared in `ClientCallTest` interface will be called for each nested test 
(one declaration - two executions in different environments).
 
