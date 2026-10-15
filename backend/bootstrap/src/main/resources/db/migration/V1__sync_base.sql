-- Secuencia global de versionado (reloj lógico, no depende de la hora del cliente)
CREATE SEQUENCE sync_version_seq;

-- Outbox transaccional: operaciones pendientes de enviar a la nube
CREATE TABLE outbox (
    id              UUID PRIMARY KEY,
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    UUID         NOT NULL,
    operation       VARCHAR(20)  NOT NULL,
    payload         JSONB        NOT NULL,
    idempotency_key UUID         NOT NULL UNIQUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    sent_at         TIMESTAMPTZ,
    attempts        INTEGER      NOT NULL DEFAULT 0,
    last_error      TEXT
);
CREATE INDEX idx_outbox_pending ON outbox (created_at) WHERE sent_at IS NULL;

-- Control del pull incremental desde la nube
CREATE TABLE sync_state (
    entity_type    VARCHAR(100) PRIMARY KEY,
    last_version   BIGINT       NOT NULL DEFAULT 0,
    last_synced_at TIMESTAMPTZ
);

-- Operaciones ya procesadas (idempotencia del lado receptor)
CREATE TABLE processed_operation (
    idempotency_key UUID PRIMARY KEY,
    processed_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
