package org.jahia.test.sam.brokenactivation;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * An immediate component whose activate method throws. SCR records the exception as the component failure reason,
 * so the component is reported in FAILED_ACTIVATION while the module around it stays STARTED.
 */
@Component(service = BrokenActivationComponent.class, immediate = true)
public class BrokenActivationComponent {

    @Activate
    public void start() {
        throw new IllegalStateException("Deliberate activation failure, used by the ModulesComponentState probe test");
    }
}
