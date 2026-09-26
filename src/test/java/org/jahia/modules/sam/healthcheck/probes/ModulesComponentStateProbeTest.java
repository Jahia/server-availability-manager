package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.ProbeStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Each fixture module of the Cypress suite produces one issue, so the suite never builds a report of several lines.
 * The report is built from two lists, and these cases are the ones that need several of them.
 */
class ModulesComponentStateProbeTest {

    @Test
    void sortsTheIssuesSoTwoAnswersCanBeCompared() {
        ProbeStatus status = ModulesComponentStateProbe.toStatus(new ArrayList<>(Arrays.asList("b", "c", "a")),
                Collections.emptyList());

        assertEquals(ProbeStatus.Health.YELLOW, status.getHealth());
        assertEquals("3 component configuration(s) failed to activate in a started module:\na\nb\nc",
                status.getMessage());
    }

    @Test
    void reportsTenIssuesAndCountsTheRest() {
        List<String> issues = IntStream.range(10, 22).mapToObj(i -> "issue" + i)
                .collect(Collectors.toCollection(ArrayList::new));

        String[] lines = ModulesComponentStateProbe.toStatus(issues, Collections.emptyList()).getMessage().split("\n");

        assertEquals(12, lines.length);
        assertEquals("issue19", lines[10]);
        assertEquals("and 2 more", lines[11]);
    }

    @Test
    void namesTheComponentsTheScanCouldNotRead() {
        List<String> unreadable = Arrays.asList("x", "y");

        ProbeStatus green = ModulesComponentStateProbe.toStatus(new ArrayList<>(), unreadable);
        assertEquals(ProbeStatus.Health.GREEN, green.getHealth());
        assertEquals("No component failed to activate in a started module."
                + " 2 component(s) could not be read, and this answer leaves them out", green.getMessage());

        ProbeStatus yellow = ModulesComponentStateProbe.toStatus(new ArrayList<>(Collections.singletonList("a")),
                unreadable);
        assertEquals("1 component configuration(s) failed to activate in a started module."
                + " 2 component(s) could not be read, and this answer leaves them out:\na", yellow.getMessage());
    }

    @Test
    void answersYellowWhenTheScanThrows() {
        // No reference is set, so the scan throws a NullPointerException. The probe must answer rather than throw,
        // because GqlProbe turns an exception into RED and the servlet then answers 503.
        ProbeStatus status = new ModulesComponentStateProbe().getStatus();

        assertEquals(ProbeStatus.Health.YELLOW, status.getHealth());
    }
}
