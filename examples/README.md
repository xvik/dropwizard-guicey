# dropwizard-guicey examples
[![Examples CI](https://github.com/xvik/dropwizard-guicey/actions/workflows/examples-CI.yml/badge.svg)](https://github.com/xvik/dropwizard-guicey/actions/workflows/examples-CI.yml)

### About

NOTE: Older examples (before guicey 5, dropwizard 2.1) were published into the [separate repository
 ](https://github.com/xvik/dropwizard-guicey-examples/) (see branches)

If you can't find answer for your problem in provided examples, please request new sample by 
[creating new issue](https://github.com/xvik/dropwizard-guicey/issues).



### Guicey core examples

* [Getting started](core-getting-started) - example application from getting started documentation chapter
* [Extensions](core-extensions) - ways of extensions declaration 
* [Servlets and filters](core-servlets) - servlets and filters registration example
* [Sub resources](core-rest-sub-resource) - sub resource usage example
* [Plug-n-play bundle](core-bundle-plug-n-play) - example of bundle, activated after its appearance in classpath
* [Default installers re-configuration](core-installers-reset) - using only subset of default installers
* [Custom installer implementation](core-installer-custom) - manual extension declaration example

### Guicey ext modules examples

* [JDBI](ext-jdbi) - JDBI ext module example (deprecated) 
* [JDBI3](ext-jdbi3) - JDBI3 ext module example
* [EventBus](ext-eventbus) - guava eventbus ext module example
* [SPA HTML5 routes](ext-spa) - SPA ext module example: HTML5 routes correct handling on server (for single page application)
* [Server pages exmaple](ext-gsp) - GSP example: server side templates and assets management
* [Server pages SPA exmaple](ext-gsp-spa) - use GSP templates for SPA index page

### Other integrations

* [Auth](integration-auth) - dropwizard-auth integration example
* [Hibernate](integration-hibernate) - dropwizard-hibernate integration example
* [Guice-validator](integration-guice-validator) - guice-validator integration example
* [Dropwizard-jobs](integration-dropwizard-jobs) - dropwizard-jobs integration example

### Maven samples

* [Simple](maven-simple) - dropwizard-guicey declared with direct dependency
* [BOM](maven-bom) - dropwizard-guicey declared with guicey BOM 