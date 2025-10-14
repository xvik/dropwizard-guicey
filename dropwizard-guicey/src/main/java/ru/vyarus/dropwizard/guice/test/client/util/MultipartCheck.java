package ru.vyarus.dropwizard.guice.test.client.util;

import com.google.common.base.Preconditions;

import java.util.Optional;

/**
 * Utility to check multipart jar presence.
 * <p>
 * Note: this can't be merged with {@link ru.vyarus.dropwizard.guice.test.client.builder.util.conf.MultipartSupport} as
 * this class should not use any multipart classes directly.
 *
 * @author Vyacheslav Rusakov
 * @since 09.10.2025
 */
public final class MultipartCheck {

    private static final String MULTIPART_FEATURE = "org.glassfish.jersey.media.multipart.MultiPartFeature";

    private MultipartCheck() {
    }

    /**
     * @return true if the multipart jar is available
     */
    public static boolean isEnabled() {
        return getMultipartFeatureClass().isPresent();
    }

    /**
     * @return multipart feature class or null if jar not available
     */
    public static Optional<Class<?>> getMultipartFeatureClass() {
        try {
            return Optional.of(Class.forName(MULTIPART_FEATURE));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * @throws IllegalStateException if the multipart jar is not available
     */
    public static void requireEnabled() {
        Preconditions.checkState(isEnabled(), "Multipart feature is not enabled. Either add "
                + "'io.dropwizard:dropwizard-forms' or 'org.glassfish.jersey.media:jersey-media-multipart'.");
    }
}
