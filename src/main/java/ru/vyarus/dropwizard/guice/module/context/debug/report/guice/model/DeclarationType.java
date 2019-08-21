package ru.vyarus.dropwizard.guice.module.context.debug.report.guice.model;

import com.google.inject.servlet.InstanceFilterBinding;
import com.google.inject.servlet.InstanceServletBinding;
import com.google.inject.servlet.LinkedFilterBinding;
import com.google.inject.servlet.LinkedServletBinding;
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
    ProviderInstance(ProviderInstanceBinding.class, true),
    /**
     * Linked binding declaration ({@code bind(Smth.class).to(Other.class),}, where Other may be alreadt declared
     * beforeas separate binding).
     */
    LinkedKey(LinkedKeyBinding.class, true),
    /**
     * Provider by key ({@code bind(Smth.class).toProvider(DmthProv.class)}).
     */
    ProviderKey(ProviderKeyBinding.class, true),
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
    /**
     * {@link TypeConverter} registration. Appear only for module elements analysis.
     */
    TypeConverter(TypeConverterBinding.class, false),
    /**
     * Provider method in module (annotated with {@link com.google.inject.Provides}).
     */
    ProviderMethod(ModuleAnnotatedMethodScannerBinding.class, true),
    /**
     * Exposed binding from private module.
     * NOTE: only first level of exposed bindings are shown and so if private module use private module inside
     * of it - second level exposures will not be visible in report! (logic: can't use - no need to see).
     * Anyway, even on lower levels bindings will be marked as exposed (with a marker at the end).
     */
    Exposed(ExposedBinding.class, true),


    // EXTENSIONS (only servlets and multibindings extensions supported)

    /**
     * Http filter registration by class. This is not real binding, it could only be revealed with extensions target
     * visitor. Real binding in injector is ignored (it's marked as internal).
     */
    FilterKey(LinkedFilterBinding.class, false),
    /**
     * Http filter registration by instance. This is not real binding, it could only be revealed with extensions target
     * visitor. Real binding in injector is ignored (it's marked as internal).
     */
    FilterInstance(InstanceFilterBinding.class, false),
    /**
     * Http servlet registration by class. This is not real binding, it could only be revealed with extensions target
     * visitor. Real binding in injector is ignored (it's marked as internal).
     */
    ServletKey(LinkedServletBinding.class, false),
    /**
     * Http servlet registration by instance. This is not real binding, it could only be revealed with extensions target
     * visitor. Real binding in injector is ignored (it's marked as internal).
     */
    ServletInstance(InstanceServletBinding.class, false),

    // INJECTOR TYPES (appear only for bindings from injector and never appeared during module elements analysis)

    // com.google.inject.spi.ProviderBinding is intentionally skipped as not useful for report!

    /**
     * Used for all types, instantiated by guice (mostly right sides). Appear instead of {@link #Untargetted}
     * bindings for bindings requested from injector.
     */
    Binding(ConstructorBinding.class, true),
    /**
     * Dynamic binding, constructed from bound string constant (using {@link TypeConverter}).
     */
    ConvertedConstant(ConvertedConstantBinding.class, true);


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
     * @return binding type
     */
    public Class<?> getType() {
        return type;
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
