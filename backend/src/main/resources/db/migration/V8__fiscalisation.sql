-- Minimal fiscal queue: pending documents, registered devices, and fiscal-day sessions.
-- Submission to FDMS is asynchronous; P0 records the pending document and retry state.

CREATE TABLE fiscal_devices (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    code         TEXT NOT NULL,
    name         TEXT NOT NULL,
    status       TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fiscal_devices_status_chk CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT fiscal_devices_tenant_code_uidx UNIQUE (tenant_id, code)
);

CREATE INDEX fiscal_devices_tenant_id_idx ON fiscal_devices (tenant_id);

CREATE TABLE fiscal_documents (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    sale_id      UUID NOT NULL REFERENCES sales (id) ON DELETE CASCADE,
    status       TEXT NOT NULL DEFAULT 'PENDING_SUBMISSION',
    attempts     INTEGER NOT NULL DEFAULT 0,
    last_error   TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fiscal_documents_status_chk CHECK (status IN (
        'PENDING_SUBMISSION', 'SUBMITTED', 'FAILED', 'ACCEPTED'
    )),
    CONSTRAINT fiscal_documents_attempts_chk CHECK (attempts >= 0),
    CONSTRAINT fiscal_documents_tenant_sale_uidx UNIQUE (tenant_id, sale_id)
);

CREATE TABLE fiscal_day_sessions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    status       TEXT NOT NULL,
    opened_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    closed_at    TIMESTAMPTZ,
    CONSTRAINT fiscal_day_status_chk CHECK (status IN ('OPEN', 'CLOSED'))
);

CREATE UNIQUE INDEX fiscal_day_one_open_uidx
    ON fiscal_day_sessions (tenant_id)
    WHERE status = 'OPEN';
