package org.jahia.modules.sam.healthcheck.probes;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.services.modulemanager.DefinitionsManagerService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.touk.throwing.ThrowingPredicate;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Component(service = Probe.class, immediate = true)
public class ModulesDefinitionsProbe implements Probe {
    private static final Logger logger = LoggerFactory.getLogger(ModulesDefinitionsProbe.class);

    private DefinitionsManagerService definitionsManagerService;
    private JahiaTemplateManagerService templateManagerService;

    @Reference
    public void setDefinitionsManagerService(DefinitionsManagerService definitionsManagerService) {
        this.definitionsManagerService = definitionsManagerService;
    }

    @Reference
    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    @Override
    public String getName() {
        return "ModuleDefinitions";
    }

    @Override
    public String getDescription() {
        return "Checks if modules are compatibles with the current deployed definitions";
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.HIGH;
    }


    @Override
    public ProbeStatus getStatus() {
        Collection<String> incompatibleModules = getInvalidaModules();

        if (!incompatibleModules.isEmpty()) {
            return new ProbeStatus(String.format("The definitions used by the started %s modules correspond to the definitions of higher, non started, versions of these modules.", StringUtils.join(incompatibleModules, ",")),
                    ProbeStatus.Health.RED);
        }
        return new ProbeStatus("All modules are ok", ProbeStatus.Health.GREEN);
    }

    public Collection<String> getInvalidaModules() {
        if (definitionsManagerService.skipDefinitionValidation()) {
            logger.debug("Skipping CND definition validation...");
            return Collections.emptyList();
        }
        return templateManagerService.getTemplatePackageRegistry().getRegisteredModules().keySet().stream()
                .filter(ThrowingPredicate.unchecked(f -> !definitionsManagerService.isLatest(f) && definitionsManagerService.checkDefinition(f) != DefinitionsManagerService.CND_STATUS.OK))
                .collect(Collectors.toList());
    }
}
