package com.plantilla.offline.bootstrap;

import jakarta.annotation.Resource;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class OutboxRelay {

    private static final Logger LOG = Logger.getLogger(OutboxRelay.class.getName());
    private static final int BATCH_SIZE = 50;

    private final boolean enabled = "LOCAL".equalsIgnoreCase(
            System.getProperty("plantilla.mode", System.getenv().getOrDefault("PLANTILLA_MODE", "LOCAL")).trim());

    @Resource(lookup = "java:jboss/datasources/PlantillaDS")
    private DataSource dataSource;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final Jsonb jsonb = JsonbBuilder.create();

    @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
    public void relay() {
        if (!enabled) {
            return;
        }
        try (Connection c = dataSource.getConnection()) {
            String[] installation = readInstallation(c);
            String installationId = installation[0];
            String cloudUrl = installation[1];
            String syncToken = installation[2];
            if (cloudUrl == null || cloudUrl.isBlank()) {
                return; // sin nube configurada: nada que enviar
            }
            if (syncToken == null || syncToken.isBlank()) {
                LOG.warning("Relé del outbox: falta installation.sync_token, no se envía nada");
                return;
            }
            List<Pending> pending = readPending(c);
            if (pending.isEmpty()) {
                return;
            }
            send(c, installationId, cloudUrl, syncToken, pending);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Relé del outbox: " + e.getMessage());
        }
    }

    private record Pending(UUID id, String type, UUID aggregateId, String operation,
                           String payloadJson, UUID idempotencyKey, int attempts) {
    }

    private String[] readInstallation(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("select id, cloud_url, sync_token from installation limit 1");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return new String[]{rs.getString(1), rs.getString(2), rs.getString(3)};
        }
    }

    private List<Pending> readPending(Connection c) throws SQLException {
        List<Pending> list = new ArrayList<>();
        String sql = "select id, aggregate_type, aggregate_id, operation, payload::text, idempotency_key, attempts "
                + "from outbox where sent_at is null and failed_at is null and next_attempt_at <= now() "
                + "order by created_at limit " + BATCH_SIZE;
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Pending(rs.getObject(1, UUID.class), rs.getString(2), rs.getObject(3, UUID.class),
                        rs.getString(4), rs.getString(5), rs.getObject(6, UUID.class), rs.getInt(7)));
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private void send(Connection c, String installationId, String cloudUrl, String syncToken, List<Pending> pending) throws Exception {
        List<Map<String, Object>> ops = new ArrayList<>();
        for (Pending p : pending) {
            ops.add(Map.of(
                    "idempotencyKey", p.idempotencyKey().toString(),
                    "aggregateType", p.type(),
                    "aggregateId", p.aggregateId().toString(),
                    "operation", p.operation(),
                    "payload", jsonb.fromJson(p.payloadJson(), Map.class)));
        }
        String body = jsonb.toJson(Map.of("installationId", installationId, "operations", ops));

        HttpRequest request = HttpRequest.newBuilder(URI.create(cloudUrl + "/sync/push"))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + syncToken)
                .header("X-Installation-Id", installationId)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            String detail = response.body() == null ? "" : response.body();
            if (detail.length() > 200) {
                detail = detail.substring(0, 200);
            }
            int code = response.statusCode();
            boolean permanent = code >= 400 && code < 500
                    && code != 401 && code != 403 && code != 404
                    && code != 408 && code != 429;
            markFailed(c, pending, "HTTP " + code + " " + detail, permanent);
            return;
        }

        Map<String, Object> result = jsonb.fromJson(response.body(), Map.class);
        List<String> confirmed = (List<String>) result.get("confirmed");
        markSent(c, pending, confirmed);
    }

    private void markSent(Connection c, List<Pending> pending, List<String> confirmed) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("update outbox set sent_at = now(), last_error = null where id = ?")) {
            for (Pending p : pending) {
                if (confirmed.contains(p.idempotencyKey().toString())) {
                    ps.setObject(1, p.id());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    /** 10 s, 30 s, 1 min, 5 min, 15 min, 1 h (tope). */
    private static final int[] BACKOFF_SECONDS = {10, 30, 60, 300, 900, 3600};

    private void markFailed(Connection c, List<Pending> pending, String error, boolean permanent)
            throws SQLException {
        String sql = permanent
                ? "update outbox set attempts = attempts + 1, last_error = ?, failed_at = now() where id = ?"
                : "update outbox set attempts = attempts + 1, last_error = ?, "
                  + "next_attempt_at = now() + (? * interval '1 second') where id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Pending p : pending) {
                ps.setString(1, error);
                if (permanent) {
                    ps.setObject(2, p.id());
                } else {
                    ps.setInt(2, BACKOFF_SECONDS[Math.min(p.attempts(), BACKOFF_SECONDS.length - 1)]);
                    ps.setObject(3, p.id());
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}