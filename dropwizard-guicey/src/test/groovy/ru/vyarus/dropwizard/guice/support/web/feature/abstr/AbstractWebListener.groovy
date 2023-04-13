package ru.vyarus.dropwizard.guice.support.web.feature.abstr

import jakarta.servlet.ServletRequestListener
import jakarta.servlet.annotation.WebListener

/**
 * @author Vyacheslav Rusakov
 * @since 07.08.2016
 */
@WebListener
abstract class AbstractWebListener implements ServletRequestListener {
}
