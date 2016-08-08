package ru.vyarus.dropwizard.guice.support.web.feature.abstr

import javax.servlet.ServletRequestListener
import javax.servlet.annotation.WebListener

/**
 * @author Vyacheslav Rusakov
 * @since 07.08.2016
 */
@WebListener
abstract class AbstractWebListener implements ServletRequestListener {
}
