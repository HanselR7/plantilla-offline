package com.plantilla.offline.bootstrap;

import com.plantilla.offline.adapter.out.persistence.nota.NotaJpaRepository;
import com.plantilla.offline.adapter.out.persistence.server.ServerInfoJpaAdapter;
import com.plantilla.offline.application.health.HealthService;
import com.plantilla.offline.application.health.HealthUseCase;
import com.plantilla.offline.application.nota.NotaService;
import com.plantilla.offline.application.nota.NotaUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import com.plantilla.offline.adapter.out.persistence.pairing.PairingJpaAdapter;
import com.plantilla.offline.application.pairing.PairingService;
import com.plantilla.offline.application.pairing.PairingUseCase;

@ApplicationScoped
public class UseCaseConfig {

    @Inject
    NotaJpaRepository notaRepository;

    @Inject
    ServerInfoJpaAdapter serverInfo;

    @Inject
    PairingJpaAdapter pairingRepository;

    @Produces
    @ApplicationScoped
    public HealthUseCase healthUseCase() {
        return new HealthService("1.0.0-SNAPSHOT", serverInfo);
    }

    @Produces
    @ApplicationScoped
    public NotaUseCase notaUseCase() {
        return new NotaService(notaRepository);
    }

    @Produces
    @ApplicationScoped
    public PairingUseCase pairingUseCase() {
        return new PairingService(pairingRepository);
    }
}