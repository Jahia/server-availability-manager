package org.jahia.modules.sam.healthcheck.probes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The lines are written as the jahia.log appender of Jahia lays them out, "%d: %-5p [%t] %c: %m", as Tomcat logs,
 * and in custom layouts that a customer may configure in log4j2.xml.
 */
class JahiaErrorsProbeTest {

    @Test
    void countsALineLoggedAtAnErrorLevel() {
        assertTrue(JahiaErrorsProbe.isErrorLine("2026-10-15 01:41:13,660: ERROR [http-nio-8080-exec-3] org.jahia.services.content.JCRNodeWrapperImpl: Repository error unable to read node /sites/mySite/home"));
        assertTrue(JahiaErrorsProbe.isErrorLine("2026-10-15 01:41:13,660: FATAL [main] org.jahia.bin.listeners.JahiaContextLoaderListener: Error while starting Jahia"));
        assertTrue(JahiaErrorsProbe.isErrorLine("15-Oct-2026 02:07:25.149 SEVERE [http-nio-8080-exec-1] org.apache.catalina.webresources.AbstractFileResourceSet.logIgnoredSymlink Resource for web application [] at path [/WEB-INF/lib] was not loaded"));
    }

    @Test
    void ignoresALevelWordThatTheMessageQuotes() {
        assertFalse(JahiaErrorsProbe.isErrorLine("2026-10-15 01:39:43,904: INFO  [pool-335-thread-1] org.jahia.modules.requestsafety.filter.AtmosphereRpcFilter: [REQUEST-SAFETY] AtmosphereRpcFilter activated on [REQUEST, ERROR, FORWARD], non-GET requests to /atmosphere are blocked."));
        assertFalse(JahiaErrorsProbe.isErrorLine("2026-10-15 01:39:43,909: INFO  [pool-335-thread-1] org.jahia.bin.filters.CompositeFilter: Registering servlet filter org.jahia.modules.requestsafety.filter.EngineThemeParameterFilter@282310cf[enabled=true,urlPatterns=<null>,matchAllUrls=true,order=-5.0,beanName=<null>,filterName=<null>,dispatcherTypes=[REQUEST, ERROR],parameters={}]"));
        assertFalse(JahiaErrorsProbe.isErrorLine("2026-10-15 01:39:43,909: WARN  [main] org.example.Service: the previous run ended with ERROR [code 42]"));
    }

    @Test
    void countsALineWithoutTheDefaultHeaderWhenItNamesAnErrorLevel() {
        assertTrue(JahiaErrorsProbe.isErrorLine("{\"timestamp\":\"2026-10-15T01:41:13.660Z\",\"level\":\"ERROR\",\"thread\":\"main\",\"message\":\"Repository error\"}"));
        assertTrue(JahiaErrorsProbe.isErrorLine("ERROR 2026-10-15 01:41:13,660 [main] org.jahia.services.content.JCRNodeWrapperImpl: Repository error"));
        assertTrue(JahiaErrorsProbe.isErrorLine("\tat org.example.ERROR.handle(ERROR.java:12)"));
        // The fallback cannot tell a level from a quoted word: a custom layout keeps the false positive of issue #269.
        assertTrue(JahiaErrorsProbe.isErrorLine("{\"level\":\"INFO\",\"message\":\"AtmosphereRpcFilter activated on [REQUEST, ERROR, FORWARD]\"}"));
    }

    @Test
    void ignoresALineWithoutTheDefaultHeaderThatNamesNoErrorLevel() {
        assertFalse(JahiaErrorsProbe.isErrorLine("{\"level\":\"INFO\",\"message\":\"Jahia started\"}"));
        assertFalse(JahiaErrorsProbe.isErrorLine("\tat org.example.Service.handle(Service.java:12)"));
    }
}
