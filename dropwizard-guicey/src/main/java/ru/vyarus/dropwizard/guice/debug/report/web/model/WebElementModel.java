package ru.vyarus.dropwizard.guice.debug.report.web.model;

import com.google.inject.Key;
import com.google.inject.servlet.UriPatternType;

/**
 * ServletModule servlet or filter registration model.
 *
 * @author Vyacheslav Rusakov
 * @since 23.10.2019
 */
public class WebElementModel {

    private final WebElementType type;
    private final Key key;
    private final boolean instance;

    private String pattern;
    private UriPatternType patternType;

    /**
     * Create model.
     *
     * @param type     element type
     * @param key      binding key
     * @param instance true for instance
     */
    public WebElementModel(final WebElementType type,
                           final Key key, final boolean instance) {
        this.type = type;
        this.key = key;
        this.instance = instance;
    }

    /**
     * @param pattern binding pattern
     */
    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    /**
     * @param patternType binding pattern type
     */
    public void setPatternType(final UriPatternType patternType) {
        this.patternType = patternType;
    }

    /**
     * @return element type
     */
    public WebElementType getType() {
        return type;
    }

    /**
     * @return binding pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @return binding pattern type
     */
    public UriPatternType getPatternType() {
        return patternType;
    }

    /**
     * @return binding key
     */
    public Key getKey() {
        return key;
    }

    /**
     * @return true for binding by instance
     */
    public boolean isInstance() {
        return instance;
    }
}
