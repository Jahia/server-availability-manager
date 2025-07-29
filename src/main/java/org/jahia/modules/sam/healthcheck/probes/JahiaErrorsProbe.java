package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

    private static final int MAX_COLLECTED_LINES = 10;
    private static final MessageFormat yellowMessage = new MessageFormat("A total of {0} errors are present on the platform, errors are not expected in a production environment and we recommend reviewing these. Matching lines are: [{1}]");
    private static Pattern logPattern = Pattern.compile(".*(ERROR|SEVERE|FATAL).*");

    @Override
    public String getName() {
        return "JahiaErrors";
    }

    @Activate
    protected void activate() {
        jahiaLogFilepath = System.getProperty("jahia.log.dir") + "jahia.log";
    }

    @Override
    public String getDescription() {
        return "Count the number of errors faced by Jahia. This probe is useful in a CI/CD context during the startup and provisioning phase of Jahia to detect errors triggered during the installation of modules. ";
    }


    @Override
    public ProbeStatus getStatus() {
        int numberOfError = 0;
        List<String> matchingLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(jahiaLogFilepath))) {
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                if (logPattern.matcher(currentLine).matches()){
                    numberOfError++;
                    if (matchingLines.size() < MAX_COLLECTED_LINES) {
                        matchingLines.add(currentLine.trim());
                    }
                }
            }

            String matchingLinesStr = String.join("; ", matchingLines);
            if (numberOfError > MAX_COLLECTED_LINES) {
                matchingLinesStr += "; ... and " + (numberOfError - MAX_COLLECTED_LINES) + " more";
            }

            return numberOfError == 0 ?
                    new ProbeStatus("No errors are present on the platform", ProbeStatus.Health.GREEN) :
                    new ProbeStatus(yellowMessage.format(new Object[]{numberOfError, matchingLinesStr}), ProbeStatus.Health.YELLOW);
        } catch (Exception e) {
            logger.debug("Jahia errors can not be checked as the probe is unable to read the log file", e);
            return new ProbeStatus("Jahia errors can not be checked", ProbeStatus.Health.YELLOW);
        }
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.DEBUG;
    }

}
