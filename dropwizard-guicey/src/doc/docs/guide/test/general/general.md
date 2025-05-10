# General test tools

!!! note "Junit 5"
    If you're going to use junit 5, go straight to [junit 5 section](../junit5/setup.md):
    all required general tools usage scenarios are described there.

Test framework-agnostic tools. 
Useful when:

 - There are no extensions for your test framework
 - Assertions must be performed after test app shutdown (or before startup)
 - Commands testing

Test utils:

 - `TestSupport` - root utilities class, providing easy access to other helpers
 - `DropwizardTestSupport` - [dropwizard native support](https://www.dropwizard.io/en/release-4.0.x/manual/testing.html#non-junit) for full integration tests
 - `GuiceyTestSupport` - guice context-only integration tests (without starting web part)
 - `CommandTestSupport` - general commands tests 
 - `ClientSupport` - web client helper (useful for calling application urls)

!!! important
    `TestSupport` assumed to be used as a universal shortcut: everything could be created/executed through it
    so just type `TestSupport.` and look available methods - *no need to remember other classes*. 

Additional features implemented with hooks:

- [StubsHook](stubs.md) - stubs support
- [MocksHook](mocks.md) - mocks support
- [SpiesHook](spies.md) - spies support
- [RestStubsHook](rest.md) - lightweight REST testing
- [RecordLogsHook](logs.md) - logs testing
- [TrackersHook](tracks.md) - guice bean calls recording and performance testing