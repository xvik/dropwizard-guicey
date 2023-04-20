package ru.vyarus.guicey.gsp.app.asset.servlet;

import io.dropwizard.servlets.assets.AssetServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guicey.gsp.app.asset.AssetLookup;

import javax.annotation.Nullable;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Customized dropwizard {@link AssetServlet} which is able to search assets in multiple classpath locations.
 *
 * @author Vyacheslav Rusakov
 * @since 04.12.2018
 */
public class AssetResolutionServlet extends AssetServlet {
    private static final long serialVersionUID = 6393345594784987909L;

    private final transient Logger logger = LoggerFactory.getLogger(AssetResolutionServlet.class);

    private final AssetLookup assets;

    public AssetResolutionServlet(final AssetLookup assets,
                                  final String uriPath,
                                  @Nullable final String indexFile,
                                  @Nullable final Charset defaultCharset) {
        // asset servlet will work with single (main) assets location
        // main assets location placed last for overrides (.extendApp())
        //resourceLocations.get(resourceLocations.size() - 1)
        super(assets.getPrimaryLocation(), uriPath, indexFile, defaultCharset);
        this.assets = assets;
    }

    @Override
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    protected URL getResourceURL(final String absolutePath) {
        // do lookup even if directory requested: assets servlet will detect it and ask for index file
        final URL res = assets.lookupUrl(absolutePath);
        if (res == null) {
            if (logger.isInfoEnabled()) {
                final String err = String.format("Asset '%s' not found in locations: %s",
                        assets.getRelativePath(absolutePath), assets.getMatchingLocations(absolutePath));
                // logged here to provide additional diagnostic info
                logger.info(err);
            }
            // mimic super method behaviour
            throw new IllegalArgumentException("resource " + absolutePath + " not found");
        }
        return res;
    }
}
