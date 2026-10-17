CREATE TABLE pairing_code (
                              code_hash  VARCHAR(64) PRIMARY KEY,
                              expires_at TIMESTAMPTZ NOT NULL,
                              used_at    TIMESTAMPTZ,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE paired_device (
                               id           UUID PRIMARY KEY,
                               name         VARCHAR(100) NOT NULL,
                               token_hash   VARCHAR(64)  NOT NULL UNIQUE,
                               created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
                               last_seen_at TIMESTAMPTZ,
                               revoked_at   TIMESTAMPTZ
);

-- URL de la nube que recibirán los clientes al emparejarse (la fija el administrador).
ALTER TABLE installation ADD COLUMN cloud_url VARCHAR(500);