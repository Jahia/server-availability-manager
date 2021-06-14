package org.jahia.modules.sam.healthcheck.probes;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.ModuleState;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Module state probe to find out modules that are not active, specific modules can be added to blacklist to
 * ignore them and to whitelist to check only specific ones, by default all modules are whitelisted
 */
@Component(service = Probe.class, immediate = true)
public class ModuleStateProbe implements Probe {

    private static final String BLACKLIST_CONFIG_PROPERTY = "blacklist";
    private static final String WHITELIST_CONFIG_PROPERTY = "whitelist";

    private JahiaTemplateManagerService templateManagerService;
    private List<String> blacklist;
    private List<String> whitelist;

    @Reference
    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    @Override
    public String getName() {
        return "ModuleState";
    }

    @Override
    public String getDescription() {
        return "Checks if any of the modules on Jahia instance are in an inactive state";
    }

    @Override
    public ProbeStatus getStatus() {
        return isAnyModuleInactive() ? ProbeStatus.RED : ProbeStatus.GREEN;
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.MEDIUM;
    }

    private boolean isAnyModuleInactive() {
        Map<Bundle, ModuleState> moduleStateMap = templateManagerService.getModuleStates()
                .entrySet()
                .stream()
                .filter(entry -> {
                    Bundle bundle = entry.getKey();
                    if (blacklist.contains(bundle.getSymbolicName()) || (!whitelist.isEmpty()
                            && !StringUtils.isEmpty(whitelist.get(0))
                            && !whitelist.contains(bundle.getSymbolicName()))) {
                        return false;
                    }
                    return !BundleUtils.isFragment(bundle) && entry.getValue().getState() != ModuleState.State.STARTED;
                })
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        return !moduleStateMap.isEmpty();
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        if (config.containsKey(BLACKLIST_CONFIG_PROPERTY) && !StringUtils.isEmpty(String.valueOf(config.containsKey(BLACKLIST_CONFIG_PROPERTY)))) {
            blacklist = Arrays.stream(String.valueOf(config.get(BLACKLIST_CONFIG_PROPERTY)).split(",")).collect(toList());
        } else {
            blacklist = Collections.emptyList();
        }

        if (config.containsKey(WHITELIST_CONFIG_PROPERTY) && !StringUtils.isEmpty(String.valueOf(config.containsKey(WHITELIST_CONFIG_PROPERTY)))) {
            whitelist = Arrays.stream(String.valueOf(config.get(WHITELIST_CONFIG_PROPERTY)).split(",")).collect(toList());
        } else {
            whitelist = Collections.emptyList();
        }
    }
}
