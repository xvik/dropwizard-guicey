package ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ru.vyarus.dropwizard.guice.debug.report.jersey.util.ProviderRenderUtil;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import java.util.Collection;

/**
 * Special reporter to build detail providers report.
 * <p>
 * Note that reporter duplicate extension types detection logic, but with a bit different set of types.
 * It is assumed that installer itself is also able to recognize types correctly (and so match produced report).
 *
 * @author Vyacheslav Rusakov
 * @since 12.10.2014
 */
public class ProviderReporter extends Reporter {

    private final Multimap<Class, String> prerender = HashMultimap.create();

    /**
     * Create reporter.
     */
    public ProviderReporter() {
        super(JerseyProviderInstaller.class, "providers = ");
    }

    /**
     * @param provider    provider type
     * @param isHkManaged true for hk managed bean
     * @param isLazy      true for lazy bean
     * @return reporter itself
     */
    public ProviderReporter provider(final Class<?> provider, final boolean isHkManaged, final boolean isLazy) {
        // recognize all extension types and render lines accordingly
        for (Class ext : ProviderRenderUtil.detectProviderTypes(provider)) {
            // could be Object if no types detected ("Other" section)
            prerender.put(ext, ProviderRenderUtil.render(ext, provider, isHkManaged, isLazy));
        }
        return this;
    }

    @Override
    public void report() {
        for (Class cls : prerender.keySet()) {
            reportGroup(ProviderRenderUtil.getTypeName(cls), prerender.get(cls));
        }
        super.report();
    }

    private void printAll(final Collection<String> lines) {
        for (String line : lines) {
            line(TAB + line);
        }
    }

    private void reportGroup(final String title, final Collection<String> items) {
        if (!items.isEmpty()) {
            separate();
            line(title);
            printAll(items);
        }
    }
}
