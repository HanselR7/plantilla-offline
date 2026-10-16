package com.plantilla.offline.adapter.in.rest;

import com.plantilla.offline.application.health.HealthStatus;
import com.plantilla.offline.application.health.HealthUseCase;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/health")
@RequestScoped
public class HealthResource {

    @Inject
    HealthUseCase healthUseCase;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> health() {
        HealthStatus s = healthUseCase.check();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", s.status());
        body.put("version", s.version());
        body.put("timestamp", s.timestamp().toString());
        body.put("database", s.database());
        if (s.server() != null) {
            Map<String, Object> server = new LinkedHashMap<>();
            server.put("installationId", s.server().installationId().toString());
            server.put("name", s.server().name());
            server.put("mode", s.server().mode());
            body.put("server", server);
        }
        return body;
    }
}