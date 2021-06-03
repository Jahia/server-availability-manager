package org.jahia.modules.sam.healthcheck;

import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"java:S2226", "java:S1989"})
@Component(service = {javax.servlet.http.HttpServlet.class, javax.servlet.Servlet.class}, property = {"alias=/healthcheck"})
public class HealthCheckServlet extends HttpServlet {
    private HttpServlet gql;
    private ProbeSeverity defaultSeverity;
    private ProbeStatus statusThreshold;
    private int statusCode;

    @Activate
    public void activate(Map<String, Object> config) {
        defaultSeverity = ProbeSeverity.valueOf((String)config.get("severity.default"));
        statusThreshold = ProbeStatus.valueOf((String)config.get("status.threshold"));
        statusCode = Integer.parseInt((String) config.get("status.code"));
    }

    @Reference(service = HttpServlet.class, target = "(jmx.objectname=graphql.servlet:type=graphql)")
    public void setGql(HttpServlet gql) {
        this.gql = gql;
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

        HttpServletRequest requestWrapper = new HttpServletRequestWrapper(req) {
            @Override
            public String getParameter(String name) {
                if (name.equals("query")) {
                    return "{\n" +
                            "  admin {\n" +
                            "    serverAvailabilityManager {\n" +
                            "      healthCheck(severity:" + severity + ") {\n" +
                            "        status\n" +
                            "        probes {\n" +
                            "          name\n" +
                            "          severity\n" +
                            "status\n" +
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
            public PrintWriter getWriter() throws IOException {
                return new PrintWriter(writer);
            }
        };

        gql.service(requestWrapper, responseWrapper);

        try {
            String result = writer.getBuffer().toString();
            JSONObject obj = new JSONObject(result);
            JSONObject healthCheckNode = obj.getJSONObject("data")
                    .getJSONObject("admin")
                    .getJSONObject("serverAvailabilityManager")
                    .getJSONObject("healthCheck");

            ProbeStatus status = ProbeStatus.valueOf(healthCheckNode.getString("status"));

            if (status.ordinal() >= statusThreshold.ordinal()) {
                resp.setStatus(statusCode);
            }

            healthCheckNode.write(resp.getWriter());
        } catch (JSONException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
