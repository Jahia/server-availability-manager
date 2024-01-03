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
 * Probe to be used to get the number of exceptions in a log file.
 * This probe have a LOW severity level.
 * The probe returns a yellow level if there is at least one ERROR found in the log file.
 * By default the log file which is used is located to System.getProperty("jahia.log.dir") + "jahia.log"
 */
@Component(immediate = true, service = Probe.class)
public class JahiaExceptionsProbe implements Probe {

    private static final Logger logger = LoggerFactory.getLogger(JahiaExceptionsProbe.class);

    private String filePath;

    private static MessageFormat yellowMessage = new MessageFormat("A total of {0} exceptions are present on the platform, exceptions are not expected in a production environment and we recommend reviewing these.");

    @Override
    public String getName() {
        return "JahiaExceptions";
    }

    @Override
    public String getDescription() {
        return "This is a simple configurable test probe";
    }


    @Override
    public ProbeStatus getStatus() {
        int numberOfError = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                if (currentLine.contains("ERROR")) {
                    numberOfError++;
                }
            }
            return numberOfError == 0 ?
                    new ProbeStatus("No Exceptions are present on the platform", ProbeStatus.Health.GREEN) :
                    new ProbeStatus(yellowMessage.format(numberOfError), ProbeStatus.Health.YELLOW);
        } catch (Exception e) {
            logger.debug("Error while reading the log file", e);
            return new ProbeStatus("Jahia exceptions can not be checked", ProbeStatus.Health.YELLOW);
        }
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.LOW;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        if (config.containsKey("filePath")) {
            filePath = (String) config.get("filePath");
        } else {
            filePath = System.getProperty("jahia.log.dir") + "jahia.log";
        }
    }
}
