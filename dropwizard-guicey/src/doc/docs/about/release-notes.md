# 6.0.0 Release Notes

* Update to dropwizard 3.0.0
* Extensions merged into core guicey repository

## Dropwizard 3

See [dropwizard upgrade instructions](https://www.dropwizard.io/en/release-3.0.x/manual/upgrade-notes/upgrade-notes-3_0_x.html)

**Java 8 support was dropped**.  
As a consequence, `guicey-jdbi` and `guicey-jdbi3-java8` modules were removed.

Many core dropwizard packages were changed.

## Extensions merged into core guicey repository

Extensions were merged into core guicey repository in order to unify versioning (and simplify releases).
Maven coordinates for modules stayed the same.

Dropwizard-guicey POM does not contain dependencyManagement section anymore and so can't
be used as a BOM. Instead, use `ru.vyarus.guicey:guicey-bom` - it contains everything now.

Also, dropwizard-guicey POM was simplified: all exclusions were moved directly into dependencies section 
instead relying on dependencyManagement.

Examples repository was also merged [inside main repo](https://github.com/xvik/dropwizard-guicey/tree/dw-3/examples)

!!! note
    Guicey 5.x was also migrated to the same project structure so you can move first to guicey 5.8.0 and then to 6.0
