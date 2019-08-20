package ru.vyarus.dropwizard.guice.module.context.debug.report.guice.model;

import com.google.inject.spi.*;

/**
 * Guice binding declaration type. Note that type could be different, depending if it's element taken with SPI
 * from guice module or binding from injector.
 *
 * @author Vyacheslav Rusakov
 * @since 13.08.2019
 */
public enum DeclarationType {

    /**
     * Scope declaration ({@code bindScope(...)}). Appear only on module analysis.
     */
    Scope(ScopeBinding.class, false),
    /**
     * Instance binding ({@code bing(Smth.class).toInstance(obj)}).
     */
    Instance(InstanceBinding.class, true),
    /**
     * Bound provider instance ({@code bind(Smth.class).toProvider(obj)}).
     */
    InstanceProvider(ProviderInstanceBinding.class, true),
    /**
     * Linked binding declaration ({@code bind(Smth.class).to(Other.class),}, where Other may be alreadt declared
     * beforeas separate binding).
     */
    LinkedKey(LinkedKeyBinding.class, true),
    /**
     * Provider by key ({@code bind(Smth.class).toProvider(DmthProv.class)}).
     */
    KeyProvider(ProviderKeyBinding.class, true),
    /**
     * Untargetted binding ({@code bind(Smth.class)}). Appear only for module elements analysis.
     * When queried binding from injector these bindings would be {@link ConstructorBinding}
     * (and detected as {@link DeclarationType#Binding} below).
     */
    Untargetted(UntargettedBinding.class, true),
    /**
     * Aop interceptor registration ({@code bindInterceptor(..)}). Appear only for module elements analysis.
     */
    Aop(InterceptorBinding.class, false),
    /**
     * {@link TypeListener} registration ({@code bindListener(..)}). Appear only for module elements analysis.
     */
    TypeListener(TypeListenerBinding.class, false),
    /**
     * {@link ProvisionListener} registration ({@code bindListener(..)}). Appear only for module elements analysis.
     */
    ProvisionListener(ProvisionListenerBinding.class, false),

    // types below appear only for real bindings analysis (from injector) and never during module elements analysis

    /**
     * Synthetic provider binding appearing from declaration like {@code bind(..).toProvider(..)} (for right part).
     * This type is declared only to be able to ignore it as only direct bindings are reported.
     */
    ProviderBinding(com.google.inject.spi.ProviderBinding.class, true),
    /**
     * Used for {@link #Untargetted} bindings or bindings of other types.
     */
    Binding(com.google.inject.Binding.class, true);


    private final Class<?> type;
    private final boolean runtimeBinding;

    DeclarationType(final Class<?> type, final boolean runtimeBinding) {
        this.type = type;
        this.runtimeBinding = runtimeBinding;
    }

    /**
     * @return true for actual bindings in injector, false for configuration time elements (scopes, listeners).
     */
    public boolean isRuntimeBinding() {
        return runtimeBinding;
    }

    /**
     * @param type guice element
     * @return detected type or null
     */
    public static DeclarationType detect(final Class<? extends Element> type) {
        for (DeclarationType dec : values()) {
            if (dec.type.isAssignableFrom(type)) {
                return dec;
            }
        }
        return null;
    }
}
