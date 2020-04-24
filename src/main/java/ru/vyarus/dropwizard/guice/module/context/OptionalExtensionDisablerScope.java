package ru.vyarus.dropwizard.guice.module.context;

/**
 * Class used as disabling scope for optional extensions. Additional scope is required to differentiate
 * direct (manual) disabling and automatic disable (and, for example, be able to avoid automatic disables
 * mention in report).
 */
public class OptionalExtensionDisablerScope {
}
