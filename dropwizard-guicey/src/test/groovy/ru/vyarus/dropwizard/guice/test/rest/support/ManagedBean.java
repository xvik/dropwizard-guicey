package ru.vyarus.dropwizard.guice.test.rest.support;

import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
@Singleton
public class ManagedBean implements Managed {

    public int beforeCnt;
    public int afterCnt;

    @Override
    public void start() throws Exception {
        beforeCnt++;
    }

    @Override
    public void stop() throws Exception {
        afterCnt++;
    }
}
