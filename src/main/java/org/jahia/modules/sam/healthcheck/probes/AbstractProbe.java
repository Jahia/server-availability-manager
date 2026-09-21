package org.jahia.modules.sam.healthcheck.probes;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds what every probe declares in the same shape: its name, its description and its default severity. A probe
 * passes the three values to this constructor instead of writing three accessors of its own.
 *
 * <p>A probe is a Declarative Services component, so a subclass keeps a public constructor that takes no
 * argument.
 */
public abstract class AbstractProbe implements Probe {

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
        if (value == null || StringUtils.isEmpty(String.valueOf(value))) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(Arrays.stream(String.valueOf(value).split(","))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toSet()));
    }
}
