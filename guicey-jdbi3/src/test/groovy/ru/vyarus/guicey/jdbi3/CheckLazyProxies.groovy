package ru.vyarus.guicey.jdbi3

import com.google.inject.name.Named
import ru.vyarus.guicey.jdbi3.installer.repository.sql.SqlObjectProvider

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 23.06.2020
 */
class CheckLazyProxies extends AbstractAppTest {

    @Inject @Named("jdbi3.proxies")
    Set<SqlObjectProvider> proxies

    def "Check proxies not initialized"() {

        expect: "no initialized proxies"
        proxies.find {it.initialized} == null
    }
}
