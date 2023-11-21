package ru.vyarus.dropwizard.guice.test.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Stream duplicates all writes into two streams. Used to write all output into console together with
 * aggregation into separate stream (to be able to inspect output as string).
 *
 * @author Vyacheslav Rusakov
 * @since 20.11.2023
 */
public class EchoStream extends OutputStream {
    private final OutputStream out;
    private final OutputStream collector;

    public EchoStream(final OutputStream out) {
        this(out, new ByteArrayOutputStream());
    }

    public EchoStream(final OutputStream out, final OutputStream collector) {
        this.out = out;
        this.collector = collector;
    }

    @Override
    public void write(final int i) throws IOException {
        out.write(i);
        collector.write(i);
    }

    @Override
    public String toString() {
        return collector.toString();
    }
}
