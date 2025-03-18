package ru.vyarus.guicey.spa.filter;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Spa bundle shared state object.
 *
 * @author Vyacheslav Rusakov
 * @since 18.03.2025
 */
public class SpaBundleState {
    private final List<String> usedAssetNames = new ArrayList<>();

    /**
     * Spa bundle's asset name used for filter mapping and so must be unique for all registered bundles.
     *
     * @param assetName new asset name (for the registering bundle)
     */
    public void checkUnique(final String assetName) {
        // important because name used for filter mapping
        checkArgument(!usedAssetNames.contains(assetName),
                "SPA with name '%s' is already registered", assetName);
        usedAssetNames.add(assetName);
    }
}
