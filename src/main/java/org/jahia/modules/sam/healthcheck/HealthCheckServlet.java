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
import org.osgi.service.component.annotations.Reference;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"java:S2226", "java:S1989"})
@Component(service = {javax.servlet.http.HttpServlet.class, javax.servlet.Servlet.class}, property = {"alias=/healthcheck", "allow-api-token=true"})
public class HealthCheckServlet extends HttpServlet {
    private HttpServlet gql;
    private ProbeSeverity defaultSeverity;
    private String defaultIncludes;
    private ProbeStatus.Health statusThreshold;

    @Reference(service = ProbesRegistry.class)
    private ProbesRegistry probesRegistry;

    private PermissionService permissionService;
    private int statusCode;

    @Activate
    public void activate(Map<String, Object> config) {
        //setting default values for probes
        defaultSeverity = (config.get("severity.default")!=null ? ProbeSeverity.valueOf((String) config.get("severity.default")) : ProbeSeverity.MEDIUM);
        defaultIncludes = (config.get("includes.default") != null) ? (String) config.get("includes.default") : "";
        statusThreshold = (config.get("status.threshold")!=null ? ProbeStatus.Health.valueOf((String) config.get("status.threshold")) : ProbeStatus.Health.RED);
        statusCode = (config.get("status.code")!=null ? Integer.parseInt((String) config.get("status.code")) : 503);
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
        String severity = Optional.ofNullable(req.getParameter("severity")).orElse(defaultSeverity.name()).toUpperCase();

        try {
            ProbeSeverity.valueOf(severity);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Filter 'includes' param (or default includes) against valid probe names
        String includesParam = Optional.ofNullable(req.getParameter("includes")).orElse(defaultIncludes);
        String tmpIncludes = null;
        if (!includesParam.isEmpty()) {
            Set<String> includeSet = Stream.of(includesParam.split(",")).collect(Collectors.toCollection(HashSet::new));
            tmpIncludes = probesRegistry.getProbes().stream()
                    .filter(b -> includeSet.contains(b.getName()))
                    .map(b -> "\"" + b.getName() + "\"")
                    .collect(Collectors.joining(","));
        }
        String includes = tmpIncludes;

        HttpServletRequest requestWrapper = new HttpServletRequestWrapper(req) {
            @Override
            public boolean isAsyncSupported() {
                return false;
            }

            @Override
            public String getParameter(String name) {
                if (name.equals("query")) {
                    String params = "severity: " + severity
                            + ((includes != null) ? String.format(", includes: [%s]", includes) : "");
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
        StringWriter writer = new StringWriter();
        HttpServletResponse responseWrapper = new HttpServletResponseWrapper(resp) {
            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                return new ServletOutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        writer.write((char) b);
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setWriteListener(WriteListener writeListener) {
                    }
                };
            }

            @Override
            public PrintWriter getWriter() throws IOException {
                return new PrintWriter(writer);
            }

            @Override
            public void setContentLength(int len) {
            }
        };

        permissionService.addScopes(Collections.singleton("graphql"), req);
        gql.service(requestWrapper, responseWrapper);

        try {
            String result = writer.getBuffer().toString();
            JSONObject obj = new JSONObject(result);
            if (obj.has("errors") && obj.getJSONArray("errors").length() > 0) {
                JSONArray errors = obj.getJSONArray("errors");
                JSONObject error = errors.getJSONObject(0);
                if (error.getString("errorType").equals(GqlAccessDeniedException.class.getSimpleName())) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN,error.getString("message"));
                } else {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error.getString("message"));
                }
            } else {
                JSONObject healthCheckNode = obj.getJSONObject("data")
                        .getJSONObject("admin")
                        .getJSONObject("jahia")
                        .getJSONObject("healthCheck");

                ProbeStatus.Health status = ProbeStatus.Health.valueOf(healthCheckNode.getJSONObject("status").getString("health"));

                if (status.ordinal() >= statusThreshold.ordinal()) {
                    resp.setStatus(statusCode);
                } else {
                    resp.setStatus(HttpServletResponse.SC_OK);
                }


                try (StringWriter finalWriter = new StringWriter()) {
                    healthCheckNode.write(finalWriter);
                    result = finalWriter.getBuffer().toString();
                }

                resp.setContentLength(result.length());

                try (PrintWriter respWriter = resp.getWriter()) {
                    respWriter.write(result);
                }
            }
        } catch (JSONException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
