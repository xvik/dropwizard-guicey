package ru.vyarus.guicey.jdbi.installer;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Stage;
import com.google.inject.multibindings.Multibinder;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;
import ru.vyarus.java.generics.resolver.GenericsResolver;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

/**
 * Recognize classes implementing JDBI's {@link ResultSetMapper} and register them. Register mappers as singletons.
 * Reports all installed mappers to console.
 * <p>
 * Mappers are normal guice beans and so may use constructor injection, aop etc.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.guicey.jdbi.module.MapperBinder for actual installation
 * @since 4.12.2016
 */
public class MapperInstaller implements FeatureInstaller, BindingInstaller {

    private final Reporter reporter = new Reporter(MapperInstaller.class, "jdbi mappers = ");

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, ResultSetMapper.class);
    }

    @Override
    public void bind(final Binder binder, final Class<?> type, final boolean lazy) {
        binder.bind(type).in(Singleton.class);
        register(binder, type);
    }

    @Override
    public <T> void manualBinding(final Binder binder, final Class<T> type, final Binding<T> binding) {
        register(binder, type);
    }

    @SuppressWarnings("unchecked")
    private void register(final Binder binder, final Class<?> type) {
        // just combine mappers in set and special bean, installed by module will bind it to dbi
        Multibinder.newSetBinder(binder, ResultSetMapper.class).addBinding()
                .to((Class<? extends ResultSetMapper>) type);
    }

    @Override
    public void extensionBound(final Stage stage, final Class<?> type) {
        if (stage != Stage.TOOL) {
            final String target = GenericsResolver.resolve(type).type(ResultSetMapper.class).genericAsString(0);
            reporter.line("%-20s (%s)", target, type.getName());
        }
    }

    @Override
    public void report() {
        reporter.report();
    }

    @Override
    public List<String> getRecognizableSigns() {
        return Collections.singletonList("implements " + ResultSetMapper.class.getSimpleName());
    }
}
