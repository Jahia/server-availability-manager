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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
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
    private static final String CUSTOM_LAYOUT_NOTE =" The layout of jahia.log is not the default one, so every line that contains ERROR, SEVERE or FATAL is counted.";
    // The header of the default layouts: a log line starts with its date, and its level is the word just before
    // the first '['. Jahia writes "2026-10-15 01:41:13,660: ERROR [thread] logger: message" and Tomcat
    // "15-Oct-2026 02:07:25.149 SEVERE [thread] ...". Group 1 is the level.
    private static final Pattern headerPattern = Pattern.compile("\\d[^\\[]*\\s([A-Z]+)\\s+\\[.*");
    private static final Set<String> errorLevels = Set.of("ERROR", "SEVERE", "FATAL");
    // Used only when no line of the file has that header, which means a custom layout.
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
        try (BufferedReader br = new BufferedReader(new FileReader(jahiaLogFilepath))) {
            return statusOf(br);
        } catch (Exception e) {
            logger.debug("Jahia errors can not be checked as the probe is unable to read the log file", e);
            return new ProbeStatus("Jahia errors can not be checked", ProbeStatus.Health.YELLOW);
        }
    }

    // Separate from getStatus() for testability: a unit test passes the lines without a file on disk.
    static ProbeStatus statusOf(BufferedReader br) throws IOException {
        boolean defaultLayout = false;
        int byLevel = 0;
        int byPattern = 0;
        List<String> levelLines = new ArrayList<>();
        List<String> patternLines = new ArrayList<>();
        String currentLine;
        while ((currentLine = br.readLine()) != null) {
            Matcher header = headerPattern.matcher(currentLine);
            if (header.matches()) {
                defaultLayout = true;
                if (errorLevels.contains(header.group(1)) && byLevel++ < MAX_COLLECTED_LINES) {
                    levelLines.add(currentLine.trim());
                }
            // Once a line had the header, the count by pattern is no longer used.
            } else if (!defaultLayout && logPattern.matcher(currentLine).matches() && byPattern++ < MAX_COLLECTED_LINES) {
                patternLines.add(currentLine.trim());
            }
        }

        // One line with the header is enough to count the whole file by level; otherwise the previous pattern counts.
        int numberOfError = defaultLayout ? byLevel : byPattern;
        if (numberOfError == 0) {
            return new ProbeStatus("No errors are present on the platform", ProbeStatus.Health.GREEN);
        }
        String matchingLinesStr = String.join("; ", defaultLayout ? levelLines : patternLines);
        if (numberOfError > MAX_COLLECTED_LINES) {
            matchingLinesStr += "; ... and " + (numberOfError - MAX_COLLECTED_LINES) + " more";
        }
        String message = yellowMessage.format(new Object[]{numberOfError, matchingLinesStr});
        return new ProbeStatus(defaultLayout ? message : message + CUSTOM_LAYOUT_NOTE, ProbeStatus.Health.YELLOW);
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.DEBUG;
    }

}
