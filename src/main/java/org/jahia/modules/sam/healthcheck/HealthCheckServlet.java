package org.jahia.modules.sam.healthcheck;

import org.jahia.modules.graphql.provider.dxm.security.GqlAccessDeniedException;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.services.securityfilter.PermissionService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"java:S2226", "java:S1989"})
@Component(service = {javax.servlet.http.HttpServlet.class, javax.servlet.Servlet.class}, property = {"alias=/healthcheck", "allow-api-token=true"})
public class HealthCheckServlet extends HttpServlet {
    private static final String ERRORS_FIELD = "errors";
    private HttpServlet gql;
    private final AtomicReference<Settings> settings = new AtomicReference<>();

    @Reference(service = ProbesRegistry.class)
    private ProbesRegistry probesRegistry;

    private PermissionService permissionService;

    /**
     * Serves as both the activation and the modified method: rebuilding the settings is exactly the work
     * a configuration update needs.
     */
    @Activate
    @Modified
    public void activate(Map<String, Object> config) {
        // a single write of a fully built object: on an update the servlet is already serving, so a
        // request reads either every setting from before the update or every setting from after it,
        // and a value the constructor rejects leaves the servlet on its previous settings
        settings.set(new Settings(config));
    }

    @Reference(service = HttpServlet.class, target = "(component.name=graphql.kickstart.servlet.OsgiGraphQLHttpServlet)")
    public void setGql(HttpServlet gql) {
        this.gql = gql;
    }

    @Reference
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Settings currentSettings = settings.get();
        String severity = Optional.ofNullable(req.getParameter("severity")).orElse(currentSettings.defaultSeverity.name()).toUpperCase();

        try {
            ProbeSeverity.valueOf(severity);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Filter 'includes' param (or default includes) against valid probe names
        String includesParam = Optional.ofNullable(req.getParameter("includes")).orElse(currentSettings.defaultIncludes);
        String tmpIncludes = null;
        if (!includesParam.isEmpty()) {
            Set<String> includeSet = Stream.of(includesParam.split(",")).collect(Collectors.toCollection(HashSet::new));
            tmpIncludes = probesRegistry.getProbes().stream()
                    .filter(b -> includeSet.contains(b.getName()))
                    .map(b -> "\"" + b.getName() + "\"")
                    .collect(Collectors.joining(","));
        }
        HttpServletRequest requestWrapper = getRequestWrapper(req, tmpIncludes, severity);
        HealthCheckHttpServletResponseWrapper responseWrapper = new HealthCheckHttpServletResponseWrapper(resp);

        permissionService.addScopes(Collections.singleton("healthcheck"), req);
        gql.service(requestWrapper, responseWrapper);

        try {
            String result = responseWrapper.getContent();
            JSONObject obj = new JSONObject(result);
            if (obj.has(ERRORS_FIELD) && !obj.getJSONArray(ERRORS_FIELD).isEmpty()) {
                handleErrorResponse(resp, obj);
            } else {
                handleSuccessResponse(resp, obj, currentSettings);
            }
        } catch (JSONException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static void handleErrorResponse(HttpServletResponse resp, JSONObject obj) throws IOException {
        JSONArray errors = obj.getJSONArray(ERRORS_FIELD);
        JSONObject error = errors.getJSONObject(0);
        if (error.getString("errorType").equals(GqlAccessDeniedException.class.getSimpleName())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,error.getString("message"));
        } else {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error.getString("message"));
        }
    }

    private static void handleSuccessResponse(HttpServletResponse resp, JSONObject obj, Settings currentSettings) throws IOException {
        String result;
        JSONObject healthCheckNode = obj.getJSONObject("data")
                .getJSONObject("admin")
                .getJSONObject("jahia")
                .getJSONObject("healthCheck");

        ProbeStatus.Health status = ProbeStatus.Health.valueOf(healthCheckNode.getJSONObject("status").getString("health"));

        if (status.ordinal() >= currentSettings.statusThreshold.ordinal()) {
            resp.setStatus(currentSettings.statusCode);
        } else {
            resp.setStatus(HttpServletResponse.SC_OK);
        }


        try (StringWriter finalWriter = new StringWriter()) {
            healthCheckNode.write(finalWriter);
            result = finalWriter.getBuffer().toString();
        }

        // Content-Length counts bytes, and a probe message can carry a non-ASCII character from a third party
        // exception, which takes more than one byte. The content type carries the charset, and it is set before
        // getWriter(). The writer therefore encodes in UTF-8, and the declared length counts those same bytes.
        resp.setContentType("application/json;charset=UTF-8");
        resp.setContentLength(result.getBytes(StandardCharsets.UTF_8).length);

        try (PrintWriter respWriter = resp.getWriter()) {
            respWriter.write(result);
        }
    }

    private static HttpServletRequest getRequestWrapper(HttpServletRequest req, String tmpIncludes, String severity) {

        return new HttpServletRequestWrapper(req) {
            @Override
            public boolean isAsyncSupported() {
                return false;
            }

            @Override
            public String getParameter(String name) {
                if (name.equals("query")) {
                    String params = "severity: " + severity
                            + ((tmpIncludes != null) ? String.format(", includes: [%s]", tmpIncludes) : "");
                    return "{\n" +
                            "  admin {\n" +
                            "    jahia {\n" +
                            "      healthCheck(" + params + ") {\n" +
                            "        status {\n" +
                            "          health\n" +
                            "          message\n" +
                            "        }\n" +
                            "        probes {\n" +
                            "          name\n" +
                            "          severity\n" +
                            "          status {\n" +
                            "            health\n" +
                            "            message\n" +
                            "          }\n" +
                            "        }\n" +
                            "      }\n" +
                            "    }\n" +
                            "  }\n" +
                            "}";
                }
                return super.getParameter(name);
            }
        };
    }

    private static final class Settings {
        private final ProbeSeverity defaultSeverity;
        private final String defaultIncludes;
        private final ProbeStatus.Health statusThreshold;
        private final int statusCode;

        private Settings(Map<String, Object> config) {
            //setting default values for probes
            defaultSeverity = (config.get("severity.default")!=null ? ProbeSeverity.valueOf((String) config.get("severity.default")) : ProbeSeverity.MEDIUM);
            defaultIncludes = (config.get("includes.default") != null) ? (String) config.get("includes.default") : "";
            statusThreshold = (config.get("status.threshold")!=null ? ProbeStatus.Health.valueOf((String) config.get("status.threshold")) : ProbeStatus.Health.RED);
            statusCode = (config.get("status.code")!=null ? Integer.parseInt((String) config.get("status.code")) : 503);
        }
    }

    /**
     * Collects the response of the internal GraphQL call, which the servlet then re-serves itself. The call can
     * write through the stream or through the writer, and both go to one buffer that is decoded as UTF-8.
     * Decoding each byte on its own turned a two byte character into two characters.
     *
     * <p>The capture is always UTF-8. setCharacterEncoding is therefore ignored, and getCharacterEncoding
     * answers UTF-8, so a caller that builds its own encoder agrees with the buffer.
     *
     * <p>reset and resetBuffer discard what was captured, which is what a caller asking for a reset means.
     * flushBuffer and sendError are not intercepted and reach the real response. The servlet writes the body
     * itself, so a body committed behind its back would be a defect rather than a case to absorb here.
     */
    private static class HealthCheckHttpServletResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        /**
         * One encoder for the whole capture. Encoding each write on its own replaced a character written as a
         * surrogate pair across two writes.
         */
        private OutputStreamWriter encoder = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);

        /**
         * One stream and one writer per response, because the servlet contract promises the same object. The
         * stream flushes the encoder before each write, so bytes written through the two routes keep their
         * order in the buffer.
         */
        private ServletOutputStream outputStream;
        private PrintWriter writer;

        /** The captured body, once read. Reading ends the capture, so the answer cannot change afterwards. */
        private String content;

        public HealthCheckHttpServletResponseWrapper(HttpServletResponse resp) {
            super(resp);
        }

        /**
         * Closes the encoder rather than flushing it. A flush never emits a character left pending as half of
         * a surrogate pair, and only a close does. Closing ends the capture, so the answer is kept, and a
         * second call returns the same string instead of writing to a closed encoder.
         *
         * @return what the internal call wrote, through the stream, the writer, or both
         */
        public String getContent() throws IOException {
            if (content == null) {
                encoder.close();
                content = buffer.toString(StandardCharsets.UTF_8.name());
            }
            return content;
        }

        @Override
        public ServletOutputStream getOutputStream() {
            if (outputStream == null) {
                outputStream = new ServletOutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        encoder.flush();
                        buffer.write(b);
                    }

                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        encoder.flush();
                        buffer.write(b, off, len);
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setWriteListener(WriteListener writeListener) {
                        // ignore callback notifications
                    }
                };
            }
            return outputStream;
        }

        /**
         * The writer goes through the shared encoder. Closing it flushes the encoder rather than closing it,
         * so a caller that closes the writer loses nothing.
         *
         * <p>One case this wrapper cannot order is a caller that alternates this writer and the stream within
         * one character, because a flush cannot emit a pending surrogate half. The servlet writes the body
         * itself, and the internal call uses one route, so that case does not arise here.
         */
        @Override
        public PrintWriter getWriter() {
            if (writer == null) {
                writer = new PrintWriter(new Writer() {
                    @Override
                    public void write(char[] chars, int off, int len) throws IOException {
                        encoder.write(chars, off, len);
                    }

                    @Override
                    public void flush() throws IOException {
                        encoder.flush();
                    }

                    @Override
                    public void close() throws IOException {
                        encoder.flush();
                    }
                });
            }
            return writer;
        }

        /** @return UTF-8 always, because the buffer is decoded as UTF-8 whatever the caller asked for. */
        @Override
        public String getCharacterEncoding() {
            return StandardCharsets.UTF_8.name();
        }

        @Override
        public void setCharacterEncoding(String charset) {
            // the capture is UTF-8, and getCharacterEncoding says so
        }

        @Override
        public void resetBuffer() {
            super.resetBuffer();
            discardCapture();
        }

        @Override
        public void reset() {
            super.reset();
            discardCapture();
        }

        /** A reset asks for the captured body to be forgotten, encoder state included. */
        private void discardCapture() {
            // The encoder is not closed, because closing would emit a pending character into the buffer this
            // call exists to empty. It wraps a byte array and holds no resource of its own.
            buffer.reset();
            encoder = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);
            content = null;
        }

        @Override
        public void setContentLength(int len) {
            // the length of the real response is set once the body is known
        }

        @Override
        public void setContentLengthLong(long len) {
            // the length of the real response is set once the body is known
        }
    }
}
