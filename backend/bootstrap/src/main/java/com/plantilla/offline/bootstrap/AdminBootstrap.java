package com.plantilla.offline.bootstrap;

import com.plantilla.offline.application.auth.AuthUseCase;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.DependsOn;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import java.util.logging.Logger;

/** Crea el usuario "admin" la primera vez, cuando todavía no hay usuarios. */
@Singleton
@Startup
@DependsOn("DatabaseMigration")
public class AdminBootstrap {

    private static final Logger LOG = Logger.getLogger(AdminBootstrap.class.getName());

    @Inject
    AuthUseCase auth;

    @PostConstruct
    void init() {
        // -Dplantilla.admin.password=... o variable PLANTILLA_ADMIN_PASSWORD
        String configured = System.getProperty("plantilla.admin.password",
                System.getenv("PLANTILLA_ADMIN_PASSWORD"));
        auth.ensureAdmin(configured).ifPresent(generated -> LOG.warning(
                "Usuario 'admin' creado con contraseña generada: " + generated
                        + " (guárdala; no se volverá a mostrar)"));
    }
}