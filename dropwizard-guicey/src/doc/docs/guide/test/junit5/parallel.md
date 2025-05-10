## Parallel execution
    
Junit [parallel tests execution](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution)
could be activated with properties file `junit-platform.properties` located at test resources root:

```properties
junit.jupiter.execution.parallel.enabled = true
junit.jupiter.execution.parallel.mode.default = concurrent
```

!!! note
    In order to avoid config overriding collisions (because all overrides eventually stored to system properties)
    guicey generates unique property prefixes in each test.

To avoid port collisions in dropwizard tests use [randomPorts option](#random-ports).