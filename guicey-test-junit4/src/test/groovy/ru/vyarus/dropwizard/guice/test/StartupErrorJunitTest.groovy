package ru.vyarus.dropwizard.guice.test

import org.junit.Assert
import org.junit.Rule
import org.junit.Test

/**
 * @author Vyacheslav Rusakov
 * @since 08.05.2017
 */
class StartupErrorJunitTest {

    @Rule
    public StartupErrorRule rule = StartupErrorRule.create({ out, err ->
        Assert.assertTrue(out.contains('sample'))
        Assert.assertTrue(err.contains('errrorrrr'))
    })

    @Test
    public void checkFail() throws Exception {
        System.out.println 'sample'
        System.err.println 'errrorrrr'
        System.exit(1)
    }
}
