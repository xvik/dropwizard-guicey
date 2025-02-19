package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import ru.vyarus.dropwizard.guice.AbstractPlatformTest;

/**
 * @author Vyacheslav Rusakov
 * @since 14.02.2025
 */
public abstract class AbstractTrackerTest extends AbstractPlatformTest {

    @Override
    protected String clean(String out) {
        return out
                .replaceAll("@[\\da-z]{6,10}", "@11111111")
                .replaceAll("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d+]", "[2025-22-22 11:11:11]")
                .replaceAll("\\d+\\.\\d+ ms {4,}", "11.11 ms      ")
                .replaceAll("\\d+\\.\\d+ ms", "11.11 ms");
    }
}
