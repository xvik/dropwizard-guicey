package ru.vyarus.dropwizard.guice.examples;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jobs.JobConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vyacheslav Rusakov
 * @since 11.03.2018
 */
public class JobsAppConfiguration extends JobConfiguration {

    @JsonProperty("quartz")
    private Map<String, String> quartz = new HashMap<>();

    public Map<String, String> getQuartzConfiguration() {
        return quartz;
    }
}
