# Guice injections

Any guice bean could be injected directly into a test field:

```groovy
@Inject
SomeBean bean
```

This will work even for not declared (in guice modules) beans (JIT injection will occur).

To better understand injection scopes look the following test:

```groovy
// one application instance started for all test methods
@TestGuiceyApp(AutoScanApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InjectionScopeTest {

    // new instance injected on each test
    @Inject
    TestBean bean;

    // the same context used for all tests (in class), so the same bean instance inserted before each test
    @Inject
    TestSingletonBean singletonBean;

    @Test
    @Order(1)
    public void testInjection() {
        bean.value = 5;
        singletonBean.value = 15;

        Assertions.assertEquals(5, bean.value);
        Assertions.assertEquals(15, singletonBean.value);

    }

    @Test
    @Order(2)
    public void testSharedState() {

        Assertions.assertEquals(0, bean.value);
        Assertions.assertEquals(15, singletonBean.value);
    }

    // bean is in prototype scope
    public static class TestBean {
        int value;
    }

    @Singleton
    public static class TestSingletonBean {
        int value;
    }
}
```


!!! note
    Guice AOP *will not work* on test methods (because test instances are not created by guice).

## Parameter injection

Any **declared** guice bean may be injected as test method parameter:

```java
@Test
public void testSomthing(DummyBean bean) 
```

(where `DummyBean` is manually declared in some module or requested as a dependency 
(JIT-instantiated) during injector creation).

For unknown beans injection (not declared and not used during startup) special annotation must be used:

```java
@Test
public void testSomthing(@Jit TestBean bean) 
```

!!! info
    Additional annotation required because you may use other junit extensions providing their own
    parameters, which guicey extension should not try to handle. That's why not annotated parameters
    verified with existing injector bindings.
    
Qualified and generified injections will also work:

```java
@Test
public void testSomthing(@Named("qual") SomeBean bean,
                         TestBean<String> generifiedBean,
                         Provider<OtherBean> provider) 
```    

Also, there are special objects available as parameters:

* `Application` or exact application class (`MyApplication`)
* `ObjectMapper`
* `ClientSupport` application web client helper
* `DropwizardTestSupport` test support object used internally
* `ExtensionContext` junit extension context

!!! note
    Parameter injection will work on test methods as well as lifecyle methods (beforeAll, afterEach etc.) 

Example:

```java
@TestDropwizardApp(AutoScanApplication.class)
public class ParametersInjectionDwTest {

    public ParametersInjectionDwTest(Environment env, DummyService service) {
        Preconditions.checkNotNull(env);
        Preconditions.checkNotNull(service);
    }

    @BeforeAll
    static void before(Application app, DummyService service) {
        Preconditions.checkNotNull(app);
        Preconditions.checkNotNull(service);
    }

    @BeforeEach
    void setUp(Application app, DummyService service) {
        Preconditions.checkNotNull(app);
        Preconditions.checkNotNull(service);
    }

    @AfterEach
    void tearDown(Application app, DummyService service) {
        Preconditions.checkNotNull(app);
        Preconditions.checkNotNull(service);
    }

    @AfterAll
    static void after(Application app, DummyService service) {
        Preconditions.checkNotNull(app);
        Preconditions.checkNotNull(service);
    }

    @Test
    void checkAllPossibleParams(Application app,
                                AutoScanApplication app2,
                                Configuration conf,
                                TestConfiguration conf2,
                                Environment env,
                                ObjectMapper mapper,
                                Injector injector,
                                ClientSupport client,
                                DropwizardTestSupport support,
                                DummyService service,
                                @Jit JitService jit) {
        assertNotNull(app);
        assertNotNull(app2);
        assertNotNull(conf);
        assertNotNull(conf2);
        assertNotNull(env);
        assertNotNull(mapper);
        assertNotNull(injector);
        assertNotNull(client);
        assertNotNull(support);
        assertNotNull(service);
        assertNotNull(jit);
        assertEquals(client.getPort(), 8080);
        assertEquals(client.getAdminPort(), 8081);
    }

    public static class JitService {

        private final DummyService service;

        @Inject
        public JitService(DummyService service) {
            this.service = service;
        }
    }
}
```

!!! tip
    `DropwizardTestSupport` and `ClientSupport` objects are also available with a static calls (in the same thread):
    
    ```java
    DropwizardTestSupport support = TestSupport.getContext();
    ClientSupport client = TestSupport.getContextClient();
    ```
