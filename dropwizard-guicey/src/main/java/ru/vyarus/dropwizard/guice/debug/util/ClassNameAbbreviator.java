package ru.vyarus.dropwizard.guice.debug.util;

/**
 * This is a copy of {@link ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator} used to avoid direct
 * dependency on logback-classic (to be able to switch to log4j instead).
 *
 * @author Vyacheslav Rusakov
 * @since 29.09.2020
 */
public class ClassNameAbbreviator {

    /**
     * The maximum number of package separators (dots) that abbreviation
     * algorithms can handle. Class or logger names with more separators will have
     * their first MAX_DOTS parts shortened.
     */
    private static final int MAX_DOTS = 16;
    private static final char DOT = '.';

    private final int targetLength;

    /**
     * Create abbreviator.
     *
     * @param targetLength maximum length
     */
    public ClassNameAbbreviator(final int targetLength) {
        this.targetLength = targetLength;
    }

    /**
     * Shorten package names to match max length.
     *
     * @param fqClassName class name
     * @return abbreviated name
     */
    public String abbreviate(final String fqClassName) {
        if (fqClassName == null) {
            throw new IllegalArgumentException("Class name may not be null");
        }
        final String res;

        if (fqClassName.length() < targetLength) {
            res = fqClassName;
        } else {
            final int[] dotIndexesArray = new int[MAX_DOTS];
            // a.b.c contains 2 dots but 2+1 parts.
            // see also http://jira.qos.ch/browse/LBCLASSIC-110
            final int[] lengthArray = new int[MAX_DOTS + 1];

            final int dotCount = computeDotIndexes(fqClassName, dotIndexesArray);

            if (dotCount == 0) {
                res = fqClassName;
            } else {
                computeLengthArray(fqClassName, dotIndexesArray, lengthArray, dotCount);

                final StringBuilder buf = new StringBuilder(targetLength);
                for (int i = 0; i <= dotCount; i++) {
                    if (i == 0) {
                        buf.append(fqClassName, 0, lengthArray[i] - 1);
                    } else {
                        buf.append(fqClassName, dotIndexesArray[i - 1], dotIndexesArray[i - 1] + lengthArray[i]);
                    }
                }

                res = buf.toString();
            }
        }
        return res;
    }

    private int computeDotIndexes(final String className, final int... dotArray) {
        int dotCount = 0;
        int k = 0;
        while (true) {
            // ignore the $ separator in our computations. This is both convenient
            // and sensible.
            k = className.indexOf(DOT, k);
            if (k != -1 && dotCount < MAX_DOTS) {
                dotArray[dotCount] = k;
                dotCount++;
                k++;
            } else {
                break;
            }
        }
        return dotCount;
    }

    @SuppressWarnings("checkstyle:UnnecessaryParentheses")
    private void computeLengthArray(final String className,
                                    final int[] dotArray,
                                    final int[] lengthArray,
                                    final int dotCount) {
        int toTrim = className.length() - targetLength;

        int len;
        for (int i = 0; i < dotCount; i++) {
            int previousDotPosition = -1;
            if (i > 0) {
                previousDotPosition = dotArray[i - 1];
            }
            final int available = dotArray[i] - previousDotPosition - 1;
            len = Math.min(available, 1);

            if (toTrim <= 0) {
                len = available;
            }
            toTrim -= (available - len);
            lengthArray[i] = len + 1;
        }

        lengthArray[dotCount] = className.length() - dotArray[dotCount - 1];
    }
}
