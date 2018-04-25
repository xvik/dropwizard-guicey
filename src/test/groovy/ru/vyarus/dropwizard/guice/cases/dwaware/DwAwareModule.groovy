package ru.vyarus.dropwizard.guice.cases.dwaware

import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule
import ru.vyarus.dropwizard.guice.support.TestConfiguration

/**
 * @author Vyacheslav Rusakov 
 * @since 04.07.2015
 */
class DwAwareModule extends DropwizardAwareModule<TestConfiguration> {

    @Override
    protected void configure() {
        assert bootstrap() != null
        assert environment() != null
        assert configuration() != null
        assert appPackage() == this.class.package.name
        assert options() != null
    }
}
