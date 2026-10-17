package com.plantilla.offline.bootstrap;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class DatabaseMigration {

    @Resource(lookup = "java:jboss/datasources/PlantillaDS")
    private DataSource dataSource;

    @PostConstruct
    void migrate() {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
        applyInstallationMode();
    }

    /** Modo de esta instalación: -Dplantilla.mode=LOCAL|CLOUD o variable PLANTILLA_MODE. */
    private void applyInstallationMode() {
        String mode = System.getProperty("plantilla.mode",
                System.getenv().getOrDefault("PLANTILLA_MODE", "LOCAL")).trim().toUpperCase();
        if (!mode.equals("LOCAL") && !mode.equals("CLOUD")) {
            throw new IllegalStateException("plantilla.mode inválido: " + mode + " (use LOCAL o CLOUD)");
        }
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("update installation set mode = ?")) {
            ps.setString(1, mode);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo aplicar el modo de instalación", e);
        }
    }
}