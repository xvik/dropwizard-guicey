package ru.vyarus.dropwizard.guice.module.autoconfig.util;

import com.google.inject.Injector;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.autoconfig.scanner.ClassVisitor;
import ru.vyarus.dropwizard.guice.module.autoconfig.scanner.ClasspathScanner;

import java.util.List;

/**
 * Provides support for commands injection support and classpath scanning resolution of commands.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
public final class CommandSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandSupport.class);

    private CommandSupport() {
    }

    /**
     * Scans classpath to find commands and register them. Default constructor used to instantiate command.
     *
     * @param bootstrap bootstrap object
     * @param scanner   configured scanner instance
     */
    public static void registerCommands(final Bootstrap bootstrap, final ClasspathScanner scanner) {
        scanner.scan(new ClassVisitor() {
            @Override
            public void visit(final Class<?> type) {
                if (FeatureUtils.is(type, Command.class)) {
                    try {
                        final Command cmd = (Command) type.newInstance();
                        bootstrap.addCommand(cmd);
                        LOGGER.debug("Command registered: {}", type.getSimpleName());
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to instantiate command: {}"
                                + type.getSimpleName(), e);
                    }
                }
            }
        });
    }

    /**
     * Inject dependencies into all registered commands. (only field and setter injection could be used)
     *
     * @param commands registered commands
     * @param injector guice injector object
     */
    public static void initCommands(final List<Command> commands, final Injector injector) {
        for (Command cmd : commands) {
            injector.injectMembers(cmd);
        }
    }
}
