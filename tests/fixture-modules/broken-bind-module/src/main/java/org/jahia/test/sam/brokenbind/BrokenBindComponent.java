package org.jahia.test.sam.brokenbind;

import org.jahia.modules.sam.Probe;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * An immediate component with a mandatory reference whose interface this module cannot load, because the package
 * import is declared at a range no bundle satisfies. SCR still selects a matching service, because the framework
 * assumes a requester with no wire for the package is using reflection, then fails to invoke the bind method.
 *
 * <p>SCR records no failure reason for a bind failure, so the component stays in SATISFIED and never reaches
 * ACTIVE. That combination is what the ModulesComponentState probe reports.
 */
@Component(service = BrokenBindComponent.class, immediate = true)
public class BrokenBindComponent {

    @Reference
    public void setProbe(Probe probe) {
        // never called: the parameter type cannot be loaded by this bundle
    }
}
