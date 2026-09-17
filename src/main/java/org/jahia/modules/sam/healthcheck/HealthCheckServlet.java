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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
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

        // The block below answers a body that does not parse. Reading the capture can fail with an
        // IOException, which is a different failure, so that read happens here and reaches the container.
        String result = responseWrapper.getContent();

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
        // exception. Declaring the character count truncated such a body. The encoding is set before getWriter(),
        // because the writer takes the encoding of the response at that moment.
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
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
     * writer, so both go to one buffer and keep their order. The buffer is decoded as UTF-8, which is what the
     * GraphQL servlet writes. Decoding each byte on its own turned a two byte character into two characters.
     */
    private static class HealthCheckHttpServletResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final PrintWriter writer = new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8));

        public HealthCheckHttpServletResponseWrapper(HttpServletResponse resp) {
            super(resp);
        }

        /** @return what the internal call wrote, through the stream, the writer, or both */
        public String getContent() {
            writer.flush();
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }

        @Override
        public ServletOutputStream getOutputStream() {
            writer.flush();
            return new ServletOutputStream() {
                @Override
                public void write(int b) {
                    buffer.write(b);
                }

                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        flushWriterRoute();
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
         * one character. A flush cannot emit a pending surrogate half. The servlet writes the body itself, and
         * the internal call uses one route, so that case does not arise here.
         */
        @Override
        public PrintWriter getWriter() {
            if (writer == null) {
                Writer target = new Writer() {
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
                };

                // PrintWriter.close() drops the writer it wraps and then discards every later write in
                // silence. This response hands out one writer, so closing it flushes and keeps it usable.
                writer = new PrintWriter(target) {
                    @Override
                    public void close() {
                        flush();
                    }
                };
            }
            return writer;
        }

        /** @return UTF-8 always, because the buffer is decoded as UTF-8 whatever the caller asked for. */
        @Override
        public String getCharacterEncoding() {
            return StandardCharsets.UTF_8.name();
        }

        @Override
        public PrintWriter getWriter() {
            return writer;
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
