package org.jahia.modules.sam.healthcheck;

import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The capture wrapper touches no container, so it is tested here rather than through HTTP. The writer route is
 * not reachable from an HTTP test today, because the request wrapper disables the async path that uses it, and
 * these cases are what that route has to get right.
 */
class HealthCheckHttpServletResponseWrapperTest {

    /** A response that answers nothing. The wrapper never reads from it, and the constructor refuses null. */
    private static HttpServletResponse silentResponse() {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    Class<?> type = method.getReturnType();
                    if (type == boolean.class) {
                        return false;
                    }
                    if (type == int.class) {
                        return 0;
                    }
                    return null;
                });
    }

    private static HealthCheckServlet.HealthCheckHttpServletResponseWrapper capture() {
        return new HealthCheckServlet.HealthCheckHttpServletResponseWrapper(silentResponse());
    }

    @Test
    void aTwoByteCharacterWrittenThroughTheWriterSurvives() throws IOException {
        HealthCheckServlet.HealthCheckHttpServletResponseWrapper wrapper = capture();

        PrintWriter writer = wrapper.getWriter();
        writer.write("caf\u00e9");
        writer.flush();

        assertEquals("caf\u00e9", wrapper.getContent());
    }

    @Test
    void aSurrogatePairSplitAcrossTwoWritesIsOneCharacter() throws IOException {
        HealthCheckServlet.HealthCheckHttpServletResponseWrapper wrapper = capture();
        String rocket = new String(Character.toChars(0x1F680));

        PrintWriter writer = wrapper.getWriter();
        writer.write(rocket.substring(0, 1));
        writer.write(rocket.substring(1));

        assertEquals(rocket, wrapper.getContent());
    }

    @Test
    void theStreamAndTheWriterKeepTheirOrder() throws IOException {
        HealthCheckServlet.HealthCheckHttpServletResponseWrapper wrapper = capture();

        wrapper.getWriter().write("a");
        wrapper.getOutputStream().write("b".getBytes(StandardCharsets.UTF_8));
        wrapper.getWriter().write("c");

        assertEquals("abc", wrapper.getContent());
    }

    @Test
    void readingTheContentTwiceGivesTheSameAnswer() throws IOException {
        HealthCheckServlet.HealthCheckHttpServletResponseWrapper wrapper = capture();
        wrapper.getWriter().write("once");

        String first = wrapper.getContent();
        String second = wrapper.getContent();

        assertEquals("once", first);
        assertSame(first, second);
    }
}
