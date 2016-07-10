package ru.vyarus.dropwizard.guice.module.context.info;

import ru.vyarus.dropwizard.guice.module.context.info.sign.DisableSupport;
import ru.vyarus.dropwizard.guice.module.context.info.sign.ScanSupport;

/**
 * Installer configuration information (all installers implement
 * {@link ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller}).
 *
 * @author Vyacheslav Rusakov
 * @since 09.07.2016
 */
public interface InstallerItemInfo extends ItemInfo, ScanSupport, DisableSupport {

}
