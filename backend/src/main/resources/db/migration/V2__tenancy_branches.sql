-- Tenancy, memberships, branches and POS devices (P0 schema slice).
-- Tenant-owned tables use composite (tenant_id, id) keys and FORCE RLS.

CREATE TABLE tenants (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name              TEXT NOT NULL,
    default_currency  CHAR(3) NOT NULL DEFAULT 'USD',
    display_timezone  TEXT NOT NULL DEFAULT 'Africa/Harare',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email          TEXT NOT NULL,
    display_name   TEXT NOT NULL,
    password_hash  TEXT,
    status         TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT users_email_unique UNIQUE (email),
    CONSTRAINT users_status_check CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE TABLE tenant_memberships (
    tenant_id  UUID NOT NULL REFERENCES tenants (id),
    user_id    UUID NOT NULL REFERENCES users (id),
    role       TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, user_id),
    CONSTRAINT tenant_memberships_role_check CHECK (
        role IN ('OWNER', 'ADMIN', 'MANAGER', 'SUPERVISOR', 'CASHIER', 'STOCK_CONTROLLER')
    )
);

CREATE TABLE branches (
    tenant_id  UUID NOT NULL REFERENCES tenants (id),
    id         UUID NOT NULL DEFAULT gen_random_uuid(),
    code       TEXT NOT NULL,
    name       TEXT NOT NULL,
    address    TEXT,
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, id),
    CONSTRAINT branches_code_unique UNIQUE (tenant_id, code)
);

CREATE TABLE membership_branches (
    tenant_id  UUID NOT NULL,
    user_id    UUID NOT NULL,
    branch_id  UUID NOT NULL,
    PRIMARY KEY (tenant_id, user_id, branch_id),
    CONSTRAINT membership_branches_membership_fk
        FOREIGN KEY (tenant_id, user_id) REFERENCES tenant_memberships (tenant_id, user_id),
    CONSTRAINT membership_branches_branch_fk
        FOREIGN KEY (tenant_id, branch_id) REFERENCES branches (tenant_id, id)
);

CREATE TABLE pos_devices (
    tenant_id       UUID NOT NULL,
    id              UUID NOT NULL DEFAULT gen_random_uuid(),
    branch_id       UUID NOT NULL,
    name            TEXT NOT NULL,
    platform        TEXT NOT NULL,
    external_serial TEXT,
    status          TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, id),
    CONSTRAINT pos_devices_branch_fk
        FOREIGN KEY (tenant_id, branch_id) REFERENCES branches (tenant_id, id),
    CONSTRAINT pos_devices_platform_check CHECK (
        platform IN ('WINDOWS', 'ANDROID', 'ANDROID_HANDHELD', 'WEB')
    ),
    CONSTRAINT pos_devices_status_check CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE INDEX pos_devices_branch_idx ON pos_devices (tenant_id, branch_id);

-- Row-level security: second safeguard; app still filters by tenant.
ALTER TABLE tenants ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenants FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_tenants ON tenants
    USING (id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER TABLE tenant_memberships ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_memberships FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_tenant_memberships ON tenant_memberships
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER TABLE branches ENABLE ROW LEVEL SECURITY;
ALTER TABLE branches FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_branches ON branches
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER TABLE membership_branches ENABLE ROW LEVEL SECURITY;
ALTER TABLE membership_branches FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_membership_branches ON membership_branches
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER TABLE pos_devices ENABLE ROW LEVEL SECURITY;
ALTER TABLE pos_devices FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_pos_devices ON pos_devices
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

-- users is global (email login); no tenant_id RLS on users.
