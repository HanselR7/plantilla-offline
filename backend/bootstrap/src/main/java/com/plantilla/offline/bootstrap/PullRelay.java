package com.plantilla.offline.bootstrap;

import com.plantilla.offline.application.sync.SyncApplyUseCase;
import com.plantilla.offline.application.sync.SyncChange;
import jakarta.annotation.Resource;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.transaction.UserTransaction;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/** Descarga de la nube los cambios posteriores al último cursor y los aplica localmente. */
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class PullRelay {

    private static final Logger LOG = Logger.getLogger(PullRelay.class.getName());
    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES_PER_RUN = 50;

    private final boolean enabled = "LOCAL".equalsIgnoreCase(
            System.getProperty("plantilla.mode", System.getenv().getOrDefault("PLANTILLA_MODE", "LOCAL")).trim());

    @Resource(lookup = "java:jboss/datasources/PlantillaDS")
    private DataSource dataSource;

    @Resource
    private UserTransaction utx;

    @Inject
    SyncApplyUseCase apply;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final Jsonb jsonb = JsonbBuilder.create();

    @Schedule(second = "*/15", minute = "*", hour = "*", persistent = false)
    public void pull() {
        if (!enabled) {
            return;
        }
        try {
            String cloudUrl = readCloudUrl();
            if (cloudUrl == null || cloudUrl.isBlank()) {
                return; // sin nube configurada
            }
            for (String type : apply.types()) {
                pullType(cloudUrl, type);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Relé de bajada: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void pullType(String cloudUrl, String type) throws Exception {
        for (int page = 0; page < MAX_PAGES_PER_RUN; page++) {
            long since = inTx(() -> apply.lastVersion(type));

            HttpRequest request = HttpRequest.newBuilder(URI.create(
                            cloudUrl + "/sync/pull?type=" + type + "&since=" + since + "&limit=" + PAGE_SIZE))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOG.warning("Bajada de '" + type + "': HTTP " + response.statusCode());
                return;
            }

            Map<String, Object> body = jsonb.fromJson(response.body(), Map.class);
            List<SyncChange> changes = new ArrayList<>();
            for (Map<String, Object> m : (List<Map<String, Object>>) body.get("changes")) {
                Map<String, Object> data = (Map<String, Object>) m.get("data");
                changes.add(new SyncChange(
                        type,
                        UUID.fromString((String) m.get("aggregateId")),
                        ((Number) m.get("version")).longValue(),
                        Boolean.TRUE.equals(m.get("deleted")),
                        data == null ? new HashMap<>() : data));
            }
            long next = ((Number) body.get("nextVersion")).longValue();
            boolean hasMore = Boolean.TRUE.equals(body.get("hasMore"));

            // Cambios y cursor se guardan juntos: si algo falla, no se pierde ni se salta nada.
            inTx(() -> {
                apply.applyPage(type, changes, next);
                return null;
            });

            if (!hasMore) {
                return;
            }
        }
    }

    private String readCloudUrl() throws Exception {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("select cloud_url from installation limit 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    private <T> T inTx(Callable<T> work) throws Exception {
        utx.begin();
        try {
            T result = work.call();
            utx.commit();
            return result;
        } catch (Exception e) {
            try {
                utx.rollback();
            } catch (Exception ignored) {
                // la transacción ya terminó
            }
            throw e;
        }
    }
}