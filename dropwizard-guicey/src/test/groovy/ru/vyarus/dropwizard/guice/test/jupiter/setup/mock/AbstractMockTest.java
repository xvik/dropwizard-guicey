package ru.vyarus.dropwizard.guice.test.jupiter.setup.mock;

import ru.vyarus.dropwizard.guice.AbstractPlatformTest;

/**
 * @author Vyacheslav Rusakov
 * @since 18.02.2025
 */
public abstract class AbstractMockTest extends AbstractPlatformTest {

    @Override
    protected String clean(String out) {
        return out
                .replaceAll("@[\\da-z]{6,10}", "@11111111")
                .replaceAll("hashCode: \\d+", "hashCode: 11111111")
                .replaceAll("\\d+(\\.\\d+) ms", "11.11 ms");
    }
}
