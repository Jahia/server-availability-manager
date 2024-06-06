package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.MessageFormat;
import java.util.Map;

/**
 * Probe to be used to get the number of ERROR in a log file.
 * This probe have a LOW severity level.
 * The probe returns a yellow level if there is at least one ERROR found in the log file.
 * By default the log file which is used is located to System.getProperty("jahia.log.dir") + "jahia.log"
 */
@Component(immediate = true, service = Probe.class)
public class JahiaErrorsProbe implements Probe {

    private static final Logger logger = LoggerFactory.getLogger(JahiaErrorsProbe.class);

    private String jahiaLogFilepath;

    private ProbeSeverity severity = ProbeSeverity.LOW;

    private static final MessageFormat yellowMessage = new MessageFormat("A total of {0} errors are present on the platform, errors are not expected in a production environment and we recommend reviewing these.");

    @Override
    public String getName() {
        return "JahiaErrors";
    }

    @Override
    public String getDescription() {
        return "Count the number of errors faced by Jahia";
    }


    @Override
    public ProbeStatus getStatus() {
        int numberOfError = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(jahiaLogFilepath))) {
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                if (currentLine.contains("ERROR")) {
                    numberOfError++;
                }
            }
            return numberOfError == 0 ?
                    new ProbeStatus("No errors are present on the platform", ProbeStatus.Health.GREEN) :
                    new ProbeStatus(yellowMessage.format(new Object[]{numberOfError}), ProbeStatus.Health.YELLOW);
        } catch (Exception e) {
            logger.debug("Jahia errors can not be checked as the probe is unable to read the log file", e);
            return new ProbeStatus("Jahia errors can not be checked", ProbeStatus.Health.YELLOW);
        }
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return severity;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        if (config.containsKey("jahiaLogFilepath")) {
            jahiaLogFilepath = (String) config.get("jahiaLogFilepath");
        } else {
            jahiaLogFilepath = System.getProperty("jahia.log.dir") + "jahia.log";
        }
        if (config.containsKey("severity")) {
            severity = ProbeSeverity.valueOf((String) config.get("severity"));
        } else {
            severity = ProbeSeverity.LOW;
        }
    }
}
