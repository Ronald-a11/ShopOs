-- Branches and POS devices. Tenant-owned; application enforces membership.

CREATE TABLE branches (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    code         TEXT NOT NULL,
    name         TEXT NOT NULL,
    address      TEXT,
    status       TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT branches_status_chk CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT branches_tenant_code_uidx UNIQUE (tenant_id, code)
);

CREATE INDEX branches_tenant_id_idx ON branches (tenant_id);

CREATE TABLE pos_devices (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    branch_id    UUID NOT NULL REFERENCES branches (id) ON DELETE CASCADE,
    code         TEXT NOT NULL,
    name         TEXT NOT NULL,
    platform     TEXT,
    status       TEXT NOT NULL DEFAULT 'AVAILABLE',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pos_devices_status_chk CHECK (status IN ('AVAILABLE', 'IN_USE', 'OFFLINE')),
    CONSTRAINT pos_devices_tenant_code_uidx UNIQUE (tenant_id, code)
);

CREATE INDEX pos_devices_branch_id_idx ON pos_devices (tenant_id, branch_id);
