package ru.vyarus.dropwizard.guice.support.web.feature.abstr

import javax.servlet.Filter
import javax.servlet.annotation.WebFilter

/**
 * @author Vyacheslav Rusakov 
 * @since 13.10.2014
 */
@WebFilter("/sample")
abstract class AbstractFilter implements Filter {
}
