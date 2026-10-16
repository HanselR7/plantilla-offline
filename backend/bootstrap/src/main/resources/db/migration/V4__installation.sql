-- Una sola fila por instalación: identifica al servidor ante el frontend y ante la nube.
CREATE TABLE installation (
                              id         UUID PRIMARY KEY,
                              name       VARCHAR(100) NOT NULL,
                              mode       VARCHAR(10)  NOT NULL CHECK (mode IN ('LOCAL', 'CLOUD')),
                              created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

INSERT INTO installation (id, name, mode)
VALUES (gen_random_uuid(), 'Servidor local', 'LOCAL');