CREATE TABLE nota (
                      id           UUID PRIMARY KEY,
                      titulo       VARCHAR(200) NOT NULL,
                      contenido    TEXT,
                      sync_version BIGINT      NOT NULL DEFAULT 0,
                      updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                      deleted_at   TIMESTAMPTZ
);

CREATE INDEX idx_nota_sync_version ON nota (sync_version);

CREATE TRIGGER trg_nota_sync
    BEFORE INSERT OR UPDATE ON nota
                         FOR EACH ROW EXECUTE FUNCTION sync_touch();