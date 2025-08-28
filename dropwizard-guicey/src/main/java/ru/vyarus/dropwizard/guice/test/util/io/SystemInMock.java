package ru.vyarus.dropwizard.guice.test.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * System in mock implementation. Should be used to substitute system input stream in order to mock user input in
 * tests. {@code SystemInMock in = new SystemInMock(); System.setIn(in); in.provideText(..);}.
 * <p>
 * Error would be thrown in case of not enough user inputs provided.
 * <p>
 * Original code taken from <a href="https://github.com/stefanbirkner/system-rules">System rules library</a> (see
 * TextFromStandardInputStream) with some modifications.
 *
 * @author Stefan Birkner
 * @since 20.11.2023
 */
public class SystemInMock extends InputStream {
    private StringReader currentReader;
    // with IOException, Scanner would intercept this exception and throw its own, whereas runtime exception
    // would pass through (more suitable)
    private final Supplier<RuntimeException> exception = () -> new IllegalStateException(
            "Console input (" + getReads() + ") not provided");
    private int reads;

    /**
     * Create system in substitutor.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public SystemInMock() {
        provideText();
    }

    /**
     * @param lines mock system in source
     */
    public void provideText(final String... lines) {
        final String separator = System.getProperty("line.separator");
        // all lines must end with a new line
        currentReader = new StringReader(String.join(separator, lines) + separator);
    }

    @Override
    public int read() throws IOException {
        final int character = currentReader.read();
        if (character == -1) {
            throw exception.get();
        }
        return character;
    }

    @SuppressWarnings("PMD.AvoidThrowingNullPointerException")
    @Override
    public int read(final byte[] buffer, final int offset, final int len) throws IOException {
        if (buffer == null) {
            throw new NullPointerException();
        } else if (offset < 0 || len < 0 || len > buffer.length - offset) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            return readNextLine(buffer, offset, len);
        }
    }

    /**
     * @return count of user input requests
     */
    public int getReads() {
        return reads;
    }

    private int readNextLine(final byte[] buffer, final int offset, final int len)
            throws IOException {
        // track user input requests count
        reads++;
        final int c = read();
        if (c == -1) {
            return -1;
        }
        buffer[offset] = (byte) c;

        int i = 1;
        for (; (i < len) && !isCompleteLineWritten(buffer, i - 1); ++i) {
            final byte read = (byte) read();
            if (read == -1) {
                break;
            } else {
                buffer[offset + i] = read;
            }
        }
        return i;
    }

    private boolean isCompleteLineWritten(final byte[] buffer, final int indexLastByteWritten) {
        final byte[] separator = System.getProperty("line.separator").getBytes(StandardCharsets.UTF_8);
        final int indexFirstByteOfSeparator = indexLastByteWritten - separator.length + 1;
        return indexFirstByteOfSeparator >= 0
                && contains(buffer, separator, indexFirstByteOfSeparator);
    }

    private boolean contains(final byte[] array, final byte[] pattern, final int indexStart) {
        for (int i = 0; i < pattern.length; ++i) {
            if (array[indexStart + i] != pattern[i]) {
                return false;
            }
        }
        return true;
    }
}
