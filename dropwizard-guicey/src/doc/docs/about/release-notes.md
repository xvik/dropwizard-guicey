# 7.0.0 Release Notes

* Update to dropwizard 4.0.0
* Upgrade to guice 7.0.0

## Dropwizard 4

See [dropwizard upgrade instructions](https://www.dropwizard.io/en/release-4.0.x/manual/upgrade-notes/upgrade-notes-4_0_x.html)

!!! tip
    If you're upgrading from dropwizard 2.1 it is recommended to perform step-by-step migration (due to many breaking changes):

    * guicey 5.8.1 - dropwizard 2.1, changed guicey project structure (same as in guicey 6)
    * guicey 6.0.1 - dropwizard 3 (changed core dropwizard packaged)
    * guicey 7.0.0 - dropwizard 4, guice 7

Now you'll have to use `jakarta.servlet` and `jakarta.validation` apis instead of `javax.*` (this might affect used 3rd party libraries).

## Guice 7

Guice 7 [drops javax.* support](https://github.com/google/guice/wiki/Guice700) and use `jakarta.inject`, `jakarta.servlet`, 
`jakarta.persistence` now.

See [migration notes](migration.md#dropwizard-40) for 3rd party libraries migration