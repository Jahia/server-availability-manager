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
        // The probe reports that this component failed and points at the log, so this message reaches the log
        // and not the health check response. The non-ASCII character is kept because it is what a reader
        // greps for when checking that the log carries the cause the probe promised.
        throw new IllegalStateException("Deliberate activation failure (\u00e9), used by the ModulesComponentState probe test");
    }
}
