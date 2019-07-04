package ru.vyarus.dropwizard.guice.module.context.info;

import ru.vyarus.dropwizard.guice.module.context.info.sign.ScanSupport;

/**
 * Dropwizard command info. Command may be only installed by classpath scan and is not registered in
 * guice context.
 *
 * @author Vyacheslav Rusakov
 * @since 27.07.2016
 */
public interface CommandItemInfo extends InstanceItemInfo, ScanSupport {

    /**
     * @return true if command is environment command, false otherwise
     */
    boolean isEnvironmentCommand();
}
