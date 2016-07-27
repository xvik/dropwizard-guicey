package ru.vyarus.dropwizard.guice.module.context.info.impl;

import io.dropwizard.cli.EnvironmentCommand;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.info.CommandItemInfo;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;

/**
 * Command item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 27.07.2016
 */
public class CommandItemInfoImpl extends ItemInfoImpl implements CommandItemInfo {

    public CommandItemInfoImpl(final Class<?> type) {
        super(ConfigItem.Command, type);
    }

    @Override
    public boolean isFromScan() {
        return getRegisteredBy().contains(ClasspathScanner.class);
    }

    @Override
    public boolean isEnvironmentCommand() {
        return EnvironmentCommand.class.isAssignableFrom(getType());
    }
}
