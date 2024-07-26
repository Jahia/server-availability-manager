/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.modules.sam.healthcheck.probes;

import org.apache.commons.lang.StringUtils;
import org.eclipse.gemini.blueprint.extender.support.ApplicationContextConfiguration;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.osgi.framework.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component(service = Probe.class, immediate = true)
public class ModulesSpringUsageProbe implements Probe, BundleListener, BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModulesSpringUsageProbe.class);
    private static final String EXCLUDE_JAHIA_MODULES_PROPERTY = "excludeJahiaModules";
    private static final List<SpringUsageInfo> springUsages = new ArrayList<>();
    private static final Set<Integer> refreshEvents = Set.of(BundleEvent.INSTALLED, BundleEvent.UNINSTALLED);
    private static boolean needRefresh = true;

    private boolean excludeJahiaModules = true;

    @Override
    public String getName() {
        return "ModulesSpringUsage";
    }

    @Override
    public String getDescription() {
        return "Checks if some modules are using Spring on the Jahia instance";
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.MEDIUM;
    }

    @Override
    public ProbeStatus getStatus() {
        this.searchForSpringUsageInBundles();
        String configMessage = "(Jahia modules " + (excludeJahiaModules ? "not " : "") + "checked) ";
        if (springUsages.isEmpty()) {
            return new ProbeStatus(configMessage.concat("No modules using spring found "), ProbeStatus.Health.GREEN);
        }
        String springUsageMessage = springUsages.stream().map(SpringUsageInfo::toString).collect(Collectors.joining(", "));
        return new ProbeStatus(configMessage.concat("Found modules that are using spring, that jahia doesn't support anymore. Details:   ").concat(springUsageMessage), ProbeStatus.Health.YELLOW);
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        if (config.containsKey(EXCLUDE_JAHIA_MODULES_PROPERTY) && !StringUtils.isEmpty(String.valueOf(config.containsKey(EXCLUDE_JAHIA_MODULES_PROPERTY)))) {
            excludeJahiaModules = Boolean.parseBoolean(String.valueOf(config.get(EXCLUDE_JAHIA_MODULES_PROPERTY)));
        }
        needRefresh = true;
    }

    @Activate
    public void start(BundleContext ctx) {
        LOGGER.debug("Starting ModulesSpringUsageProbe");
        ctx.addBundleListener(this);
    }

    @Deactivate
    public void stop(BundleContext ctx) {
        LOGGER.debug("Stopping ModulesSpringUsageProbe");
        ctx.removeBundleListener(this);
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        if (refreshEvents.contains(event.getType())) {
            LOGGER.debug("Setting need refresh flag.");
            needRefresh = true;
        }
    }

    protected synchronized void searchForSpringUsageInBundles() {
        if (needRefresh) {
            springUsages.clear();

            for (Bundle bundle : FrameworkService.getBundleContext().getBundles()) {
                if (!BundleUtils.isJahiaModuleBundle(bundle)) {
                    LOGGER.debug("Ignoring bundle: {}, not a Jahia module", bundle.getSymbolicName());
                    continue;
                }
                String bundleGroupId = BundleUtils.getModuleGroupId(bundle);
                if (excludeJahiaModules && bundleGroupId != null && bundleGroupId.startsWith("org.jahia.modules")) {
                    LOGGER.debug("Ignoring Jahia provided module: {}", bundle.getSymbolicName());
                    continue;
                }

                LOGGER.debug("Exploring bundle: {}", bundle.getSymbolicName());
                // Explore using ApplicationContextConfiguration (presence of spring context xml file)
                ApplicationContextConfiguration config = new ApplicationContextConfiguration(bundle);
                if (config.isSpringPoweredBundle()) {
                    LOGGER.info("Detected Spring powered module: {}", bundle.getSymbolicName());
                    springUsages.add(new SpringUsageInfo(bundle, "detected a Spring context xml file"));
                }
                // Explore OSGI imports for Spring Framework related package or Jahia Spring Related package
                String imports = bundle.getHeaders().get("Import-Package");
                if (imports != null) {
                    for (String importStatement : imports.split("(?<=\\S),(?!\\h*\\d)\\h*")) {
                        LOGGER.debug("Exploring import package: {}", importStatement);
                        if (importStatement.startsWith("org.springframework")) {
                            LOGGER.info("Spring related package: {} imported in module: {}", importStatement,
                                    bundle.getSymbolicName());
                            springUsages.add(new SpringUsageInfo(bundle,
                                    "detected a Spring related package imported in OSGI manifest: [" + importStatement + "]"));
                        }
                    }
                }
            }
            needRefresh = false;
        }
    }

    protected static class SpringUsageInfo {
        private String module;
        private String usage;

        public SpringUsageInfo(Bundle bundle, String usage) {
            this.module = "(id:".concat(String.valueOf(bundle.getBundleId())).concat(") ").concat(bundle.getSymbolicName())
                    .concat(" - ").concat(bundle.getVersion().toString());
            this.usage = usage;
        }

        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public String getUsage() {
            return usage;
        }

        public void setUsage(String usage) {
            this.usage = usage;
        }

        @Override public String toString() {
            return "module[" + module + "] " + usage + "   ";
        }
    }

}
