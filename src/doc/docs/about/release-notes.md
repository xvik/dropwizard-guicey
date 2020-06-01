# 5.1.0 Release Notes

The main release feature is Junit 5 support.

* [Junit5](#junit5)
* [Spock updates](#spock-updates)
* [Junit4 extensions deprecated](#junit4-extensions-deprecated)
 
## Junit 5

2 junit 5 extensions added:

* @TestDropwizardRule - full application start
* @TestGuiceyRule - guice-only tests (like old GuiceyAppRule)

Features:

* Guice beans could be injected into test fileds
* Guice beans could be injected as test (or lifecycle) method parameter
* 2 declaration types supported: as annotations on test class and field builer (vith `@RegisterExtension`)
* Hooks may be declared directly as test fields with lambdas
* Special `ClientSupport` object added to simplify web-related testing
* Support junit nested classes
* Support parallel tests

Example usage:

```java
@TestDropwizardApp(MyApp.class)
public class MyTest {
    
    @EnableHook
    static GuiceyConfigurationHook HOOK = builder -> builder.modules(new DebugModule());

    @Inject
    SomeBean bean;
    
    void testWebUrls(ClientSupport client) {
        Assertions.assertEquals("response string", 
                client.targetMain("servlet").request()
                        .buildGet()
                        .invoke()
                        .readEntity(String.class));
    }   
}
```

!!! note
    Thanks to vintage engine junit 5 may be used together with existing junit4 or spock tests.

## Spock updates

New junit 5 extensions was en evolution of existing spock extensions (junit 5 extension modle allow 
implementing thm the same way). Still, during development extensinos were improved and these improvements
were ported to spock.

Changes:

* New options in @UseDropwizardApp: randomPorts and restMapping
* Add support for hook declaration with test field:
    `@EnableHook static GuiceyConfigurationHook HOOK = { it.modules(new DebugModule()) }`
* @UseGuiceyHooks extension *deprecated* in favor of hooks declaration in fields
* Add `ClientSupport` object to simplify web tests:
    `@InjectClient ClientSupport client`
* Extensions no more rely on depcrecated juni4 rules, but use DropwizardTestSupport instead         

Old syntax remain for configOverride declaration (to preserve compatibility):

```groovy
@UseGuiceyApp(value = AutoScanApplication,
        configOverride = [
                @ConfigOverride(key = "foo", value = "2"),
                @ConfigOverride(key = "bar", value = "12")
        ])
```

It will be replaced with the new simplified junit5 syntax in the next breaking release.

Overall, juni5 and spock extensions are almost equivalent.

## Junit4 extensions deprecated

Existing junit4 rules were deprecated: GuiceyAppRule, StartupErrorRules   

Migration from junit 4 to junit 5:

* Instead of GuiceyAppRule use @TestGuiceyApp extension.
* Instead of DropwizardAppRule use @TestDropwizardApp extension.
* GuiceyHooksRule can be substituted with hooks declaration in extensions or as test fields
* There is no direct substitution for StartupErrorRule, but something similar could be achieved 
with 3rd party extensions

Also, with junit 5 vintage engine enabled, existing juni4 tests may be used together with 
new junit 5 rules.