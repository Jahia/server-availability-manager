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
        // exception, which takes more than one byte. The content type carries the charset and it is set before
        // getWriter(), so the writer encodes in UTF-8 and the declared length counts those same bytes.
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
     * Collects the response of the internal GraphQL call. The call can write through the stream or through the
     * writer, and both go to one buffer, which is decoded as UTF-8. Decoding each byte on its own turned a two
     * byte character into two characters.
     *
     * <p>This wrapper captures the body only. It does not intercept sendError, flushBuffer, reset or
     * resetBuffer, which reach the real response. A caller that discards its output therefore leaves the
     * discarded bytes in this buffer.
     */
    private static class HealthCheckHttpServletResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        /**
         * One encoder for the whole capture. Encoding each write on its own replaced a character written as a
         * surrogate pair across two writes.
         */
        private final OutputStreamWriter encoder = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);

        /** The stream flushes the encoder to keep the two write methods in order, and only when it has been used. */
        private boolean encoderUsed;

        public HealthCheckHttpServletResponseWrapper(HttpServletResponse resp) {
            super(resp);
        }

        private void flushEncoder() throws IOException {
            if (encoderUsed) {
                encoder.flush();
                encoderUsed = false;
            }
        }

        /**
         * Closes the encoder rather than flushing it, because a flush never emits a character left pending as
         * half of a surrogate pair, and only a close does. This ends the capture, so it is called once.
         *
         * @return what the internal call wrote, through the stream, the writer, or both
         */
        public String getContent() throws IOException {
            encoder.close();
            return buffer.toString(StandardCharsets.UTF_8.name());
        }

        @Override
        public ServletOutputStream getOutputStream() {
            return new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    flushEncoder();
                    buffer.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    flushEncoder();
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

        /**
         * Each call returns a new writer over the shared encoder, and closing that writer flushes the encoder
         * rather than closing it. A caller that closes the writer therefore loses nothing.
         */
        @Override
        public PrintWriter getWriter() {
            return new PrintWriter(new Writer() {
                @Override
                public void write(char[] chars, int off, int len) throws IOException {
                    encoderUsed = true;
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

        /** The buffer is decoded as UTF-8, so a caller that builds its own encoder uses the same encoding. */
        @Override
        public String getCharacterEncoding() {
            return StandardCharsets.UTF_8.name();
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
