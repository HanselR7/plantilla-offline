-- Lado nube: instalaciones locales autorizadas a sincronizar.
CREATE TABLE sync_client (
                             id           UUID PRIMARY KEY,
                             name         VARCHAR(100) NOT NULL,
                             token_hash   VARCHAR(64)  NOT NULL UNIQUE,
                             created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
                             last_seen_at TIMESTAMPTZ,
                             revoked_at   TIMESTAMPTZ
);

-- Lado local: credencial con la que se identifica ante la nube.
ALTER TABLE installation ADD COLUMN sync_token VARCHAR(100);