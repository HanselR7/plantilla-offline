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
import com.plantilla.offline.adapter.out.persistence.sync.ProcessedOperationJpaAdapter;
import com.plantilla.offline.application.sync.SyncHandler;
import com.plantilla.offline.application.sync.SyncPushService;
import com.plantilla.offline.application.sync.SyncPushUseCase;
import jakarta.enterprise.inject.Instance;
import com.plantilla.offline.adapter.out.persistence.sync.SyncLocalStateAdapter;
import com.plantilla.offline.application.sync.SyncApplyService;
import com.plantilla.offline.application.sync.SyncApplyUseCase;
import com.plantilla.offline.application.sync.SyncChangesService;
import com.plantilla.offline.application.sync.SyncChangesUseCase;
import com.plantilla.offline.adapter.out.persistence.auth.AuthJpaAdapter;
import com.plantilla.offline.application.auth.AuthService;
import com.plantilla.offline.application.auth.AuthUseCase;

@ApplicationScoped
public class UseCaseConfig {

    @Inject
    NotaJpaRepository notaRepository;

    @Inject
    ServerInfoJpaAdapter serverInfo;

    @Inject
    PairingJpaAdapter pairingRepository;

    @Inject
    Instance<SyncHandler> syncHandlers;

    @Inject
    ProcessedOperationJpaAdapter processedOperations;

    @Inject
    SyncLocalStateAdapter syncLocalState;

    @Inject
    AuthJpaAdapter authRepository;

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

    @Produces
    @ApplicationScoped
    public SyncPushUseCase syncPushUseCase() {
        return new SyncPushService(syncHandlers.stream().toList(), processedOperations);
    }

    @Produces
    @ApplicationScoped
    public SyncChangesUseCase syncChangesUseCase() {
        return new SyncChangesService(syncHandlers.stream().toList());
    }

    @Produces
    @ApplicationScoped
    public SyncApplyUseCase syncApplyUseCase() {
        return new SyncApplyService(syncHandlers.stream().toList(), syncLocalState, syncLocalState);
    }

    @Produces
    @ApplicationScoped
    public AuthUseCase authUseCase() {
        return new AuthService(authRepository);
    }
}