package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.modulemanager.models.JahiaDepends;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Module dependencies probe to find out modules that have no-range module's dependencies that could lead to problem during minor
 * dependant module's upgrade
 */
@Component(service = Probe.class, immediate = true)
public class ModuleDependenciesProbe implements Probe {

    private JahiaTemplateManagerService templateManagerService;

    @Reference
    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    @Override
    public String getName() {
        return "ModuleDependencies";
    }

    @Override
    public String getDescription() {
        return "Checks if any of the modules on Jahia instance have other module's dependency problems";
    }

    @Override
    public ProbeStatus getStatus() {
        Set<String> depVersionIssues = hasMinorVersionModuleDependency();

        if (!depVersionIssues.isEmpty()) {
            return new ProbeStatus(String.format("At least one module has a module dependency on a specific version: %s",
                    depVersionIssues),
                    ProbeStatus.Health.YELLOW);
        }

        return new ProbeStatus("All modules have well defined dependencies", ProbeStatus.Health.GREEN);
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.MEDIUM;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
    }

    /**
     * List modules that have module's dependencies with an exact minor version
     * @return a List<String> for modules with such exact version dependency
     */
    private Set<String> hasMinorVersionModuleDependency() {
        Set<String> result = new HashSet<>();
        templateManagerService.getModuleStates().keySet().stream().filter(BundleUtils::isJahiaModuleBundle).map(BundleUtils::getModule).forEach(module -> {
            module.getVersionDepends().stream().filter(JahiaDepends::hasVersion)
                    .filter(dep -> dep.getVersionRange().isExact() || dep.getMaxVersion().isEmpty()).forEach(dep -> {
                        result.add(String.format("Module %s has dependencies without range: %s", module.getId(), dep));
            });
        });
        return result;
    }

}
