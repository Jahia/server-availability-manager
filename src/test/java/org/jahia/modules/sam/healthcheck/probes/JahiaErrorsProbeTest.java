package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.ProbeStatus;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The lines are written as the jahia.log appender of Jahia lays them out, "%d: %-5p [%t] %c: %m", as Tomcat logs,
 * and in custom layouts that a customer may configure in log4j2.xml.
 */
class JahiaErrorsProbeTest {

    private static final String INFO_LINE = "2026-10-15 01:39:43,900: INFO  [main] org.jahia.bin.Jahia: Jahia started";

    @Test
    void countsALineLoggedAtAnErrorLevel() throws IOException {
        assertErrors(3, status(INFO_LINE,
                "2026-10-15 01:41:13,660: ERROR [http-nio-8080-exec-3] org.jahia.services.content.JCRNodeWrapperImpl: Repository error unable to read node /sites/mySite/home",
                "2026-10-15 01:41:13,660: FATAL [main] org.jahia.bin.listeners.JahiaContextLoaderListener: Error while starting Jahia",
                "15-Oct-2026 02:07:25.149 SEVERE [http-nio-8080-exec-1] org.apache.catalina.webresources.AbstractFileResourceSet.logIgnoredSymlink Resource for web application [] at path [/WEB-INF/lib] was not loaded"));
    }

    @Test
    void ignoresALevelWordThatTheMessageQuotes() throws IOException {
        assertGreen(status(INFO_LINE,
                "2026-10-15 01:39:43,904: INFO  [pool-335-thread-1] org.jahia.modules.requestsafety.filter.AtmosphereRpcFilter: [REQUEST-SAFETY] AtmosphereRpcFilter activated on [REQUEST, ERROR, FORWARD], non-GET requests to /atmosphere are blocked.",
                "2026-10-15 01:39:43,909: INFO  [pool-335-thread-1] org.jahia.bin.filters.CompositeFilter: Registering servlet filter org.jahia.modules.requestsafety.filter.EngineThemeParameterFilter@282310cf[enabled=true,urlPatterns=<null>,matchAllUrls=true,order=-5.0,beanName=<null>,filterName=<null>,dispatcherTypes=[REQUEST, ERROR],parameters={}]",
                "2026-10-15 01:39:43,909: WARN  [main] org.example.Service: the previous run ended with ERROR [code 42]"));
    }

    @Test
    void ignoresALineWithoutALevel() throws IOException {
        // The file may also start with such a line, such as the end of a stack trace.
        assertErrors(1, status("\tat org.example.ERROR.handle(ERROR.java:12)",
                "2026-10-15 01:41:13,660: ERROR [main] org.example.Service: failure",
                "Caused by: java.lang.IllegalStateException: ERROR [code 42]"));
    }

    @Test
    void fallsBackToThePreviousPatternForACustomLayout() throws IOException {
        ProbeStatus status = status("{\"level\":\"INFO\",\"message\":\"Jahia started\"}",
                "{\"level\":\"ERROR\",\"message\":\"Repository error\"}",
                // The fallback cannot tell a level from a quoted word: a custom layout keeps the false positive of issue #269.
                "{\"level\":\"INFO\",\"message\":\"AtmosphereRpcFilter activated on [REQUEST, ERROR, FORWARD]\"}");
        assertErrors(2, status);
        assertTrue(status.getMessage().endsWith("The layout of jahia.log is not the default one, so every line that contains ERROR, SEVERE or FATAL is counted."));
    }

    @Test
    void answersGreenOnAnEmptyLog() throws IOException {
        assertGreen(status());
    }

    private static ProbeStatus status(String... lines) throws IOException {
        return JahiaErrorsProbe.statusOf(new BufferedReader(new StringReader(String.join("\n", lines))));
    }

    private static void assertErrors(int expected, ProbeStatus status) {
        assertEquals(ProbeStatus.Health.YELLOW, status.getHealth());
        assertTrue(status.getMessage().startsWith("A total of " + expected + " errors"), status.getMessage());
    }

    private static void assertGreen(ProbeStatus status) {
        assertEquals(ProbeStatus.Health.GREEN, status.getHealth());
    }
}
