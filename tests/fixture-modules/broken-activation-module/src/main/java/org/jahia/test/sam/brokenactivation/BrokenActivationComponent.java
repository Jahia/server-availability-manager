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
        // The non-ASCII character is deliberate. The probe copies this message into the health check response,
        // and the character proves that the response declares its length in bytes.
        throw new IllegalStateException("Deliberate activation failure (\u00e9), used by the ModulesComponentState probe test");
    }
}
