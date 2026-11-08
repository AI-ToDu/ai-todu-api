CREATE TABLE integration_outbox (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES workspaces(id), type VARCHAR(100) NOT NULL,
 schema_version INTEGER NOT NULL, occurred_at TIMESTAMP WITH TIME ZONE NOT NULL, payload TEXT NOT NULL,
 status VARCHAR(20) NOT NULL CHECK(status IN ('PENDING','PUBLISHED')), attempts INTEGER NOT NULL,
 next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL, published_at TIMESTAMP WITH TIME ZONE, last_error VARCHAR(255)
);
CREATE INDEX integration_outbox_pending_idx ON integration_outbox(status,next_attempt_at);
CREATE TABLE notifications (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES workspaces(id), type VARCHAR(60) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, is_read BOOLEAN NOT NULL
);
