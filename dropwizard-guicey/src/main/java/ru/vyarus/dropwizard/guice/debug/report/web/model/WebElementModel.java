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


    public WebElementModel(final WebElementType type,
                           final Key key, final boolean instance) {
        this.type = type;
        this.key = key;
        this.instance = instance;
    }

    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    public void setPatternType(final UriPatternType patternType) {
        this.patternType = patternType;
    }

    public WebElementType getType() {
        return type;
    }

    public String getPattern() {
        return pattern;
    }

    public UriPatternType getPatternType() {
        return patternType;
    }

    public Key getKey() {
        return key;
    }

    public boolean isInstance() {
        return instance;
    }
}
