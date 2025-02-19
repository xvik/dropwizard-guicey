package ru.vyarus.dropwizard.guice.test.jupiter.ext.track;

import com.codahale.metrics.Timer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * Represents single method execution. Method arguments and the result stored as raw objects and in string form:
 * this is required because raw objects might be mutable and could change in another method, before test verification
 * would access them. Raw objects interception might be disabled, and so there would be nulls (string representations
 * would always be).
 * <p>
 * For the console output, it is preferred to wrap string values with quotes (to clearly see string bounds). Use
 * {@link #getQuotedArguments()} and {@link #getQuotedResult()} for console output (as {@link #toStringTrack()} do).
 * <p>
 * Bean instance, where method was called, is identified by unique hash: {@link #getInstanceHash()} (this is the same
 * string as in default Object.toString (@something part)). Hash is required to detect method calls to different
 * instances.
 * <p>
 * The same metrics timer used to track calls of the same method (even for different objects).
 *
 * @author Vyacheslav Rusakov
 * @since 11.02.2025
 */
@SuppressFBWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
public class MethodTrack implements Comparable<MethodTrack> {

    private final Method method;
    private final Class<?> service;
    private final String instanceHash;
    private final long started;
    private final Duration duration;
    // arguments may contain non-primitive objects which could be modified after track recording,
    // and so we can't rely on this for reporting
    private final Object[] rawArguments;
    private final String[] arguments;
    private final Object rawResult;
    private final String result;
    private final Throwable throwable;
    // indicates what arguments were strings to quote it in output (arguments + result)
    private final boolean[] stringMarkers;

    // overall timer metric for all method executions (instance available in all tracks for convenience)
    private final Timer timer;

    @SuppressWarnings({"ParameterNumber", "PMD.ExcessiveParameterList", "PMD.ConstructorCallsOverridableMethod"})
    public MethodTrack(final Class<?> service,
                       final Method method,
                       final String instanceHash,
                       final long started,
                       final Duration duration,
                       final Object[] rawArguments,
                       final String[] arguments,
                       final Object rawResult,
                       final String result,
                       final Throwable throwable,
                       final boolean[] stringMarkers,
                       final Timer timer) {
        this.method = method;
        this.service = service;
        this.instanceHash = instanceHash;
        this.started = started;
        this.duration = duration;
        this.rawArguments = rawArguments;
        this.arguments = arguments;
        this.rawResult = rawResult;
        this.result = isVoidMethod() ? null : result;
        this.throwable = throwable;
        this.stringMarkers = stringMarkers;
        this.timer = timer;
    }

    /**
     * @return called method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return type of called guice bean
     */
    public Class<?> getService() {
        return service;
    }

    /**
     * @return called instance hash
     */
    public String getInstanceHash() {
        return instanceHash;
    }

    /**
     * @return method start time
     */
    public long getStarted() {
        return started;
    }

    /**
     * @return method duration
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * WARNING: arguments could contain mutable objects, changed after method call (or even during the call) and so
     * be careful when use it (e.g., for reporting) - values might not be actual.
     *
     * @return arguments used for method call or null if raw objects keeping disabled
     */
    @SuppressWarnings("PMD.MethodReturnsInternalArray")
    public Object[] getRawArguments() {
        return rawArguments;
    }

    /**
     * String representation details:
     * - Primitive values, number and booleans stored as is
     * - String values could be truncated (by default 30 chars allowed)
     * - Objects represented as ObjectType@instanceHash
     * - null is "null".
     *
     * @return string representation of method arguments
     */
    @SuppressWarnings("PMD.MethodReturnsInternalArray")
    public String[] getArguments() {
        return arguments;
    }

    /**
     * @return true if method is void (no return)
     */
    public boolean isVoidMethod() {
        return method.getReturnType().equals(void.class);
    }

    /**
     * WARNING: the result could be a mutable objects, changed after method call and so be careful when use it
     * (e.g. for reporting) - value might not be exactly the same as returned value after the call.
     *
     * @return result object or null if raw objects keeping disabled
     */
    public Object getRawResult() {
        return rawResult;
    }

    /**
     * Note that if the method is void, a string result would also be null. Also, would be null if an error happened.
     *
     * @return string representation of the result
     */
    public String getResult() {
        return result;
    }

    /**
     * @return error thrown by method or null
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * @return true if no error appears, false otherwise
     */
    public boolean isSuccess() {
        return throwable == null;
    }

    /**
     * @return timer for all executions of this method (shared instance)
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * Almost the same as {@link #getArguments()}, but all string arguments wrapped with quotes (to see string bounds).
     *
     * @return string arguments for the console report
     */
    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    public String[] getQuotedArguments() {
        final String[] res = new String[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            String arg = arguments[i];
            if (stringMarkers[i]) {
                arg = "\"" + arg + "\"";
            }
            res[i] = arg;
        }
        return res;
    }

    /**
     * Almost the same as {@link #getResult()}, but, if the result returns string, quote it to see bounds.
     *
     * @return result string or null if method is void
     */
    public String getQuotedResult() {
        if (isVoidMethod()) {
            return null;
        }
        return stringMarkers[arguments.length] ? "\"" + result + "\"" : result;
    }

    /**
     * @return string representation of method call (with arguments and return value)
     */
    @SuppressWarnings("PMD.ConsecutiveLiteralAppends")
    public String toStringTrack() {
        // put all strings in quotes to make obvious not trimmed or empty strings
        final StringBuilder res = new StringBuilder(method.getName())
                .append('(').append(String.join(", ", getQuotedArguments())).append(')');
        if (!isSuccess()) {
            res.append(" ERROR ").append(throwable.getClass().getSimpleName())
                    .append(": ").append(throwable.getMessage());
        } else if (!isVoidMethod()) {
            res.append(" = ").append(getQuotedResult());
        }
        return res.toString();
    }

    @Override
    public String toString() {
        return toStringTrack() + "\t (" + PrintUtils.ms(duration) + ")";
    }

    @Override
    public int compareTo(final MethodTrack o) {
        return Long.compare(started, o.getStarted());
    }
}
