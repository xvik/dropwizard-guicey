# 5.8.0 Release Notes

* Update to dropwizard 2.1.6
* Extensions merged into core guicey repository

This version brings the same project structure as in guicey 6.x (for dropwizard 3) which 
could slightly simplify migration to dropwizard 3.

The same structure is required to keep 2.1, 3 and 4 branhces in sync


## Extensions merged into core guicey repository

Extensions were merged into core guicey repository in order to unify versioning (and simplify releases).
Maven coordinates for modules stayed the same.

Dropwizard-guicey POM does not contain dependencyManagement section anymore and so can't
be used as a BOM. Instead, use `ru.vyarus.guicey:guicey-bom` - it contains everything now.

Also, dropwizard-guicey POM was simplified: all exclusions were moved directly into dependencies section
instead relying on dependencyManagement.

`dopwizard-flyway` module was removed from BOM (in order to minimize external dependencies). 

Examples repository was also merged [inside main repo](https://github.com/xvik/dropwizard-guicey/tree/master/examples)