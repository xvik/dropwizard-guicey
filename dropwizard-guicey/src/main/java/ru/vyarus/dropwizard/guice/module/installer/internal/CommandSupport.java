package ru.vyarus.dropwizard.guice.module.installer.internal;

import com.google.common.base.Stopwatch;
import com.google.inject.Injector;
import io.dropwizard.core.Application;
import io.dropwizard.core.cli.Command;
import io.dropwizard.core.cli.EnvironmentCommand;
import io.dropwizard.core.setup.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.stat.DetailStat;
import ru.vyarus.dropwizard.guice.module.context.stat.StatTimer;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsTracker;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClassVisitor;
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.InstanceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.CommandTime;

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
     * Scans classpath to find commands and register them.
     * Commands are instantiated using default constructor, but {@link io.dropwizard.core.cli.EnvironmentCommand}
     * must have constructor with {@link io.dropwizard.core.Application} argument.
     *
     * @param bootstrap bootstrap object
     * @param scanner   configured scanner instance
     * @param context   configuration context
     * @return list of installed commands
     */
    public static List<Command> registerCommands(final Bootstrap bootstrap, final ClasspathScanner scanner,
                                                 final ConfigurationContext context) {
        final StatTimer timer = context.stat().timer(CommandTime);
        final CommandClassVisitor visitor = new CommandClassVisitor(bootstrap, context.stat());
        scanner.scan(visitor);
        context.registerCommands(visitor.getCommands());
        timer.stop();
        return visitor.getCommandList();
    }

    /**
     * Inject dependencies into all registered environment commands. (only field and setter injection could be used)
     * There is no need to process other commands, because only environment commands will run bundles and so will
     * start the injector.
     *
     * @param commands registered commands
     * @param injector guice injector object
     */
    public static void initCommands(final List<Command> commands, final Injector injector,
                                    final StatsTracker tracker) {
        if (commands != null) {
            for (Command cmd : commands) {
                if (cmd instanceof EnvironmentCommand) {
                    final Stopwatch timer = tracker.detailTimer(DetailStat.Command, cmd.getClass());
                    injector.injectMembers(cmd);
                    timer.stop();
                }
            }
        }
    }

    /**
     * Search catch all {@link Command} derived classes.
     * Instantiate command with default constructor and {@link io.dropwizard.core.cli.EnvironmentCommand}
     * using constructor with {@link io.dropwizard.core.Application} argument.
     */
    private static class CommandClassVisitor implements ClassVisitor {
        private final Bootstrap bootstrap;
        private final StatsTracker stats;
        // sort commands to unify order on different environments
        private final Set<Class<Command>> commands = new TreeSet<>(Comparator.comparing(Class::getName));
        private final List<Command> commandList = new ArrayList<>();

        CommandClassVisitor(final Bootstrap bootstrap, final StatsTracker stats) {
            this.bootstrap = bootstrap;
            this.stats = stats;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void visit(final Class<?> type) {
            if (FeatureUtils.is(type, Command.class)) {
                try {
                    final Command cmd;
                    final Stopwatch timer = stats.detailTimer(DetailStat.Command, type);
                    if (EnvironmentCommand.class.isAssignableFrom(type)) {
                        cmd = (Command) InstanceUtils.create(type, Application.class, bootstrap.getApplication());
                    } else {
                        cmd = (Command) InstanceUtils.create(type);
                    }
                    timer.stop();
                    commands.add((Class<Command>) type);
                    commandList.add(cmd);
                    bootstrap.addCommand(cmd);
                    LOGGER.debug("Command registered: {}", type.getSimpleName());
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to instantiate command: "
                            + type.getSimpleName(), e);
                }
            }
        }

        /**
         * @return list of installed commands or empty list
         */
        public List<Class<Command>> getCommands() {
            return commands.isEmpty() ? Collections.emptyList() : new ArrayList<>(commands);
        }

        /**
         * @return list of registered command instances
         */
        public List<Command> getCommandList() {
            return commandList;
        }
    }
}
