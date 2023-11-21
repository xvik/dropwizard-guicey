package ru.vyarus.dropwizard.guice.test.general;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.cli.Command;
import io.dropwizard.core.cli.ConfiguredCommand;
import io.dropwizard.core.cli.EnvironmentCommand;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.cmd.CommandResult;
import ru.vyarus.dropwizard.guice.test.cmd.CommandRunBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * @author Vyacheslav Rusakov
 * @since 20.11.2023
 */
public class CommandRunTest {

    @Test
    void testSimpleCommandRun() {
        CommandResult<TestConfiguration> res = TestSupport.buildCommandRunner(App.class)
                .run("simple", "-u", "user");

        Assertions.assertThat(res.isSuccess()).isTrue();
        Assertions.assertThat(res.getOutput().trim()).contains("Hello user");
        Assertions.assertThat(res.getErrorOutput()).isEmpty();
        Assertions.assertThat(res.getException()).isNull();
        Assertions.assertThat(res.getConfiguration()).isNull();
        Assertions.assertThat(res.getEnvironment()).isNull();
        Assertions.assertThat(res.getApplication()).isNotNull();
        Assertions.assertThat(res.getCommand()).isNotNull();
        Assertions.assertThat(res.getBootstrap()).isNotNull();
        Assertions.assertThat(res.getInjector()).isNull();


        res = TestSupport.buildCommandRunner(App.class)
                .run("simple");

        Assertions.assertThat(res.isSuccess()).isFalse();
        Assertions.assertThat(res.getOutput()).contains("usage: java -jar project.jar simple -u USER [-h]");
        Assertions.assertThat(res.getErrorOutput()).contains("usage: java -jar project.jar simple -u USER [-h]");
        Assertions.assertThat(res.getException()).isExactlyInstanceOf(ArgumentParserException.class);
        Assertions.assertThat(res.getConfiguration()).isNull();
    }

    @Test
    void testConfiguredCommandRun() {
        CommandResult<TestConfiguration> res = TestSupport.buildCommandRunner(App.class)
                .configOverride("foo: 12")
                .run("cfg");

        Assertions.assertThat(res.isSuccess()).isTrue();
        Assertions.assertThat(res.getOutput().trim()).endsWith("foo value: 12");
        Assertions.assertThat(res.getErrorOutput()).isEmpty();
        Assertions.assertThat(res.getException()).isNull();
        Assertions.assertThat(res.getConfiguration()).isNotNull();
        Assertions.assertThat(res.getConfiguration().foo).isEqualTo(12);
        Assertions.assertThat(res.getEnvironment()).isNull();
        Assertions.assertThat(res.getApplication()).isNotNull();
        Assertions.assertThat(res.getCommand()).isNotNull();
        Assertions.assertThat(res.getBootstrap()).isNotNull();
        Assertions.assertThat(res.getInjector()).isNull();


        // configuration from file
        res = TestSupport.buildCommandRunner(App.class)
                .config("src/test/resources/ru/vyarus/dropwizard/guice/config.yml")
                .configOverride("foo: 12")
                .run("cfg");

        Assertions.assertThat(res.isSuccess()).isTrue();
        Assertions.assertThat(res.getOutput().trim()).endsWith("foo value: 12");
        Assertions.assertThat(res.getErrorOutput()).isEmpty();
        Assertions.assertThat(res.getException()).isNull();
        Assertions.assertThat(res.getConfiguration()).isNotNull();
        Assertions.assertThat(res.getConfiguration().foo).isEqualTo(12);
        Assertions.assertThat(res.getConfiguration().bar).isEqualTo(3);
        Assertions.assertThat(res.getEnvironment()).isNull();
        Assertions.assertThat(res.getApplication()).isNotNull();
        Assertions.assertThat(res.getCommand()).isNotNull();
        Assertions.assertThat(res.getBootstrap()).isNotNull();
        Assertions.assertThat(res.getInjector()).isNull();


        // configuration from object
        TestConfiguration config = new TestConfiguration();
        config.foo = 12;
        config.bar = 3;
        res = TestSupport.buildCommandRunner(App.class)
                .config(config)
                .run("cfg");

        Assertions.assertThat(res.isSuccess()).isTrue();
        Assertions.assertThat(res.getOutput().trim()).endsWith("foo value: 12");
        Assertions.assertThat(res.getErrorOutput()).isEmpty();
        Assertions.assertThat(res.getException()).isNull();
        Assertions.assertThat(res.getConfiguration()).isNotNull();
        Assertions.assertThat(res.getConfiguration().foo).isEqualTo(12);
        Assertions.assertThat(res.getConfiguration().bar).isEqualTo(3);
        Assertions.assertThat(res.getEnvironment()).isNull();
        Assertions.assertThat(res.getApplication()).isNotNull();
        Assertions.assertThat(res.getCommand()).isNotNull();
        Assertions.assertThat(res.getBootstrap()).isNotNull();
        Assertions.assertThat(res.getInjector()).isNull();
    }

    @Test
    void testEnvironmentCommandRun() {
        CommandResult<TestConfiguration> res = TestSupport.buildCommandRunner(App.class)
                .configOverride("foo: 11")
                .run("env");

        Assertions.assertThat(res.isSuccess()).isTrue();
        Assertions.assertThat(res.getOutput().trim()).contains("foo value: 11");
        Assertions.assertThat(res.getOutput().trim()).contains("service 11");
        Assertions.assertThat(res.getErrorOutput()).isEmpty();
        Assertions.assertThat(res.getException()).isNull();
        Assertions.assertThat(res.getConfiguration()).isNotNull();
        Assertions.assertThat(res.getConfiguration().foo).isEqualTo(11);
        Assertions.assertThat(res.getEnvironment()).isNotNull();
        Assertions.assertThat(res.getApplication()).isNotNull();
        Assertions.assertThat(res.getCommand()).isNotNull();
        Assertions.assertThat(res.getBootstrap()).isNotNull();
        Assertions.assertThat(res.getInjector()).isNotNull();
        Assertions.assertThat(res.getInjector().getInstance(FooService.class).isCalled()).isTrue();
    }

    @Test
    void testRunWithInput() {
        CommandResult<TestConfiguration> res = TestSupport.buildCommandRunner(App.class)
                .consoleInputs("one", "two")
                .run("input");

        Assertions.assertThat(res.isSuccess()).isTrue();
        Assertions.assertThat(res.getOutput().trim()).isEqualTo("user input: one two");
        Assertions.assertThat(res.getErrorOutput()).isEmpty();
        Assertions.assertThat(res.getException()).isNull();
        Assertions.assertThat(res.getConfiguration()).isNull();
        Assertions.assertThat(res.getEnvironment()).isNull();
        Assertions.assertThat(res.getApplication()).isNotNull();
        Assertions.assertThat(res.getCommand()).isNotNull();
        Assertions.assertThat(res.getBootstrap()).isNotNull();
        Assertions.assertThat(res.getInjector()).isNull();


        res = TestSupport.buildCommandRunner(App.class)
                // incomplete input
                .consoleInputs("one")
                .run("input");

        Assertions.assertThat(res.isSuccess()).isFalse();
        Assertions.assertThat(res.getOutput()).contains("Console input (2) not provided");
        Assertions.assertThat(res.getErrorOutput()).contains("Console input (2) not provided");
        Assertions.assertThat(res.getException()).isExactlyInstanceOf(IllegalStateException.class);
        Assertions.assertThat(res.getConfiguration()).isNull();
    }

    @Test
    void testObjectAndConfigUsedTogether() {
        Assertions.assertThatThrownBy(() -> TestSupport.buildCommandRunner(App.class)
                .config(new TestConfiguration())
                .config("src/test/resources/ru/vyarus/dropwizard/guice/config.yml")
                .run("env")).hasMessage("Configuration object can't be used together with yaml configuration");
    }

    @Test
    void testConfigObjectWithConfigProvider() {
        Assertions.assertThatThrownBy(() -> TestSupport.buildCommandRunner(App.class)
                .config(new TestConfiguration())
                .configSourceProvider(new ResourceConfigurationSourceProvider())
                .run("env")).hasMessage("Configuration object can't be used together with yaml configuration");
    }

    @Test
    void testCommandRunFail() {
        CommandResult<TestConfiguration> res = TestSupport.buildCommandRunner(App.class)
                .run("err");

        Assertions.assertThat(res.isSuccess()).isFalse();
        Assertions.assertThat(res.getOutput()).contains("Error in command");
        Assertions.assertThat(res.getErrorOutput()).contains("Error in command");
        Assertions.assertThat(res.getException()).isExactlyInstanceOf(IllegalStateException.class);
        Assertions.assertThat(res.getException().getMessage()).isEqualTo("Error in command");
        Assertions.assertThat(res.getConfiguration()).isNull();
    }

    @Test
    void testNoArgsRun() {
        CommandResult<TestConfiguration> res = TestSupport.buildCommandRunner(App.class)
                .run();

        Assertions.assertThat(res.isSuccess()).isTrue();
        Assertions.assertThat(res.getOutput()).contains("usage: java -jar project.jar");
        Assertions.assertThat(res.getErrorOutput()).isEmpty();
        Assertions.assertThat(res.getException()).isNull();
        Assertions.assertThat(res.getConfiguration()).isNull();
    }


    @Test
    void testCommandRunOptions() {
        System.setProperty("custom.foo", "11");
        final List<String> track = new ArrayList<>();
        CommandResult<TestConfiguration> res = TestSupport.buildCommandRunner(App.class)
                .propertyPrefix("custom")
                .listen(new CommandRunBuilder.CommandListener<>() {
                    @Override
                    public void setup(String[] args) {
                        Preconditions.checkState(args.length == 1 && "env".equals(args[0]));
                        track.add("setup");
                    }

                    @Override
                    public void cleanup(CommandResult<TestConfiguration> result) {
                        Preconditions.checkNotNull(result);
                        Preconditions.checkState(result.getInjector().getInstance(FooService.class).isCalled());
                        track.add("cleanup");
                    }
                })
                .hooks(builder -> track.add("hook"))
                .run("env");

        Assertions.assertThatList(track).isEqualTo(Arrays.asList("setup", "hook", "cleanup"));
        Assertions.assertThat(res.isSuccess()).isTrue();
        Assertions.assertThat(res.getOutput().trim()).contains("foo value: 11");
        Assertions.assertThat(res.getOutput().trim()).contains("service 11");
        Assertions.assertThat(res.getErrorOutput()).isEmpty();
        Assertions.assertThat(res.getException()).isNull();
        Assertions.assertThat(res.getConfiguration()).isNotNull();
        Assertions.assertThat(res.getConfiguration().foo).isEqualTo(11);
        Assertions.assertThat(res.getEnvironment()).isNotNull();
        Assertions.assertThat(res.getApplication()).isNotNull();
        Assertions.assertThat(res.getCommand()).isNotNull();
        Assertions.assertThat(res.getBootstrap()).isNotNull();
        Assertions.assertThat(res.getInjector()).isNotNull();
        Assertions.assertThat(res.getInjector().getInstance(FooService.class).isCalled()).isTrue();
    }

    public static class App extends Application<TestConfiguration> {
        @Override
        public void initialize(Bootstrap<TestConfiguration> bootstrap) {
            bootstrap.addCommand(new SimpleCommand());
            bootstrap.addCommand(new ConfCommand());
            bootstrap.addCommand(new EnvCommand(this));
            bootstrap.addCommand(new InputCommand());
            bootstrap.addCommand(new ThrowingCommand());
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(TestConfiguration configuration, Environment environment) throws Exception {

        }
    }

    public static class SimpleCommand extends Command {

        public SimpleCommand() {
            super("simple", "Simple command");
        }

        @Override
        public void configure(Subparser subparser) {
            subparser.addArgument("-u", "--user")
                    .dest("user")
                    .type(String.class)
                    .required(true)
                    .help("The user of the program");
        }

        @Override
        public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
            System.out.println("Hello " + namespace.getString("user"));
        }
    }

    public static class ConfCommand extends ConfiguredCommand<TestConfiguration> {
        public ConfCommand() {
            super("cfg", "Configured command");
        }

        @Override
        protected void run(Bootstrap<TestConfiguration> bootstrap, Namespace namespace, TestConfiguration configuration) throws Exception {
            System.out.println("foo value: " + configuration.foo);
        }
    }

    public static class EnvCommand extends EnvironmentCommand<TestConfiguration> {

        @Inject
        FooService service;

        public EnvCommand(Application<TestConfiguration> application) {
            super(application, "env", "Environment command");
        }

        @Override
        protected void run(Environment environment, Namespace namespace, TestConfiguration configuration) throws Exception {
            System.out.println("foo value: " + configuration.foo + "; " + service.something());
        }
    }

    @Singleton
    public static class FooService {

        @Inject
        private TestConfiguration conf;
        private boolean called;


        public String something() {
            this.called = true;
            return "service " + conf.foo;
        }

        public boolean isCalled() {
            return called;
        }
    }

    public static class InputCommand extends Command {

        public InputCommand() {
            super("input", "command with user input");
        }

        @Override
        public void configure(Subparser subparser) {
        }

        @Override
        public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
            Scanner in = new Scanner(System.in);

            String line = in.nextLine();
            String line2 = in.nextLine();
            System.out.println("user input: " + line + " " + line2);
        }
    }

    public static class ThrowingCommand extends Command {

        public ThrowingCommand() {
            super("err", "Command with error");
        }

        @Override
        public void configure(Subparser subparser) {
        }

        @Override
        public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
            throw new IllegalStateException("Error in command");
        }
    }
}
