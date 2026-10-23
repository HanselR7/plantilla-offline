ALTER TABLE outbox ADD COLUMN next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE outbox ADD COLUMN failed_at TIMESTAMPTZ;

DROP INDEX IF EXISTS idx_outbox_pending;
CREATE INDEX idx_outbox_pending ON outbox (next_attempt_at, created_at)
    WHERE sent_at IS NULL AND failed_at IS NULL;