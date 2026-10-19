CREATE TABLE app_user (
                          id            UUID PRIMARY KEY,
                          username      VARCHAR(64)  NOT NULL,
                          password_hash VARCHAR(200) NOT NULL,
                          role          VARCHAR(10)  NOT NULL CHECK (role IN ('ADMIN', 'USER')),
                          disabled_at   TIMESTAMPTZ,
                          created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX uq_app_user_username ON app_user (lower(username));

CREATE TABLE auth_session (
                              id                 UUID PRIMARY KEY,
                              user_id            UUID        NOT NULL REFERENCES app_user (id),
                              access_hash        VARCHAR(64) NOT NULL UNIQUE,
                              access_expires_at  TIMESTAMPTZ NOT NULL,
                              refresh_hash       VARCHAR(64) NOT NULL UNIQUE,
                              refresh_expires_at TIMESTAMPTZ NOT NULL,
                              revoked_at         TIMESTAMPTZ,
                              created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_auth_session_user ON auth_session (user_id);