package org.jahia.modules.sam.healthcheck.probes;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.ModuleState;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Holds what every probe declares in the same shape: its name, its description and its default severity. A probe
 * passes the three values to this constructor instead of writing three accessors of its own.
 *
 * <p>A probe is a Declarative Services component, so a subclass keeps a public constructor that takes no
 * argument.
 */
public abstract class AbstractProbe implements Probe {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProbe.class);

    /** The configuration property that names what a probe must not report. Two probes read it. */
    protected static final String BLACKLIST_CONFIG_PROPERTY = "blacklist";

    private final String name;
    private final String description;
    private final ProbeSeverity defaultSeverity;

    protected AbstractProbe(String name, String description, ProbeSeverity defaultSeverity) {
        this.name = name;
        this.description = description;
        this.defaultSeverity = defaultSeverity;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return defaultSeverity;
    }

    /**
     * Reads a comma separated configuration value, which is how a probe takes a list of module or component
     * names from its configuration.
     *
     * @param config the configuration the probes registry applied
     * @param key    the property to read
     * @return the entries, trimmed, with the empty ones dropped. An absent or empty value gives an empty set.
     */
    protected static Set<String> parseNameList(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value == null) {
            return Collections.emptySet();
        }

        // Configuration admin gives a property as a String, and it can also give it as an array or a
        // collection. String.valueOf on either of those yields the object identity, which would become one
        // garbage entry, so each element is read on its own.
        Stream<String> entries;
        if (value.getClass().isArray()) {
            // Array.get reads an array of any component type, a primitive one included, which
            // Arrays.stream(Object[]) cannot.
            entries = IntStream.range(0, Array.getLength(value)).mapToObj(i -> Array.get(value, i))
                    .filter(Objects::nonNull).map(String::valueOf);
        } else if (value instanceof Collection) {
            entries = ((Collection<?>) value).stream().filter(Objects::nonNull).map(String::valueOf);
        } else {
            entries = Stream.of(String.valueOf(value));
        }

        // An empty value needs no test of its own, because the filter below drops the empty entry it splits to.
        return Collections.unmodifiableSet(entries
                .flatMap(entry -> Arrays.stream(entry.split(",")))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toSet()));
    }

    /**
     * Reads a whole number from the configuration, which is how a probe takes a threshold or a timeout.
     *
     * @param config       the configuration the probes registry applied
     * @param key          the property to read
     * @param defaultValue what an absent, empty or unusable value falls back to
     * @return the configured number, or the default. A probe therefore returns to its default when the
     *         operator removes the property, and one unusable value never leaves a probe half configured.
     */
    protected static int parseNumber(Map<String, Object> config, String key, int defaultValue) {
        Object value = config.get(key);
        if (value == null || String.valueOf(value).isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            LOGGER.warn("The {} property is not a whole number, so {} is used instead: {}", key, defaultValue, value);
            return defaultValue;
        }
    }

    /**
     * Selects the Jahia modules a probe reports on. Two probes read the same module states through the same two
     * configuration lists, so the selection lives here rather than in each of them.
     *
     * @param moduleStates what JahiaTemplateManagerService reports
     * @param blacklist    the module names the operator chose not to report, which are always dropped
     * @param whitelist    the only module names to keep. An empty whitelist keeps every module.
     * @return the entries left, in the order the module states gave them
     */
    protected static Stream<Map.Entry<Bundle, ModuleState>> selectModules(Map<Bundle, ModuleState> moduleStates,
            Set<String> blacklist, Set<String> whitelist) {
        return moduleStates.entrySet().stream()
                .filter(entry -> !blacklist.contains(entry.getKey().getSymbolicName()))
                .filter(entry -> whitelist.isEmpty() || whitelist.contains(entry.getKey().getSymbolicName()));
    }
}
