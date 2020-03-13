# Modules

All additional guicey integartion modules are maintained as separate project: [dropwizard-guicey-ext](https://github.com/xvik/dropwizard-guicey-ext)

!!! note
    Module versions are based on guicey version: `$guiceyVersion-N`.
    For example, 5.0.0-1 means first release of extensions for guicey 5.0.0.
    
    This convention is commonly used for dropwizard extension modules.

Module | Description
-------|------------
[BOM](../extras/bom.md) | Maven BOM for modules and their dependencies
[Admin REST](../extras/admin-rest.md) | Admin context rest support.
[Lifecycle annotations](../extras/lifecycle-annotations.md) | `@PostConstruct`, `@PostStartup`, `@PreDestroy` support
[EventBus](../extras/eventbus.md) | Guava eventbus integration
[JDBI](../extras/jdbi.md) | JDBI integration (based on dropwizard-jdbi)
[JDBI3](../extras/jdbi3.md) | JDBI3 integration (based on dropwizard-jdbi3)
[SPA](../extras/spa.md) | HTML5 routing support for single page applications
[Server pages](../extras/gsp.md) | JSP-like templates support (based on dropwizard-views)
[Validation](../extras/validation.md) | use validation annotations on guice beans (same behaviour as rest)