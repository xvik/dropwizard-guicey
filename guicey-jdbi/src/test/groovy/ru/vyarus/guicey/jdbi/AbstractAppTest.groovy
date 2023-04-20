package ru.vyarus.guicey.jdbi

import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.guicey.jdbi.support.SampleApp

/**
 * @author Vyacheslav Rusakov
 * @since 06.12.2016
 */
@TestGuiceyApp(value = SampleApp, config = 'src/test/resources/test-config.yml')
abstract class AbstractAppTest extends AbstractTest {
}
