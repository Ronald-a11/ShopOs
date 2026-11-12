-- Identity (users, credentials, sessions, recovery) and tenancy (tenants, memberships).
-- Tenant-owned business tables get RLS in later migrations; memberships stay app-enforced for now.

CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email               TEXT NOT NULL,
    display_name        TEXT NOT NULL,
    phone               TEXT,
    status              TEXT NOT NULL,
    email_verified_at   TIMESTAMPTZ,
    phone_verified_at   TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT users_email_lower_chk CHECK (email = lower(email)),
    CONSTRAINT users_status_chk CHECK (status IN ('ACTIVE', 'DISABLED', 'PENDING_VERIFICATION'))
);

CREATE UNIQUE INDEX users_email_uidx ON users (email);

CREATE TABLE user_credentials (
    user_id               UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    password_hash         TEXT NOT NULL,
    password_updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE auth_sessions (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    access_token_hash    TEXT NOT NULL,
    refresh_token_hash   TEXT NOT NULL,
    remember_me          BOOLEAN NOT NULL DEFAULT FALSE,
    access_expires_at    TIMESTAMPTZ NOT NULL,
    refresh_expires_at   TIMESTAMPTZ NOT NULL,
    revoked_at           TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_used_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    user_agent           TEXT,
    ip_address           TEXT,
    CONSTRAINT auth_sessions_access_token_uidx UNIQUE (access_token_hash),
    CONSTRAINT auth_sessions_refresh_token_uidx UNIQUE (refresh_token_hash)
);

CREATE INDEX auth_sessions_user_id_idx ON auth_sessions (user_id);

CREATE TABLE password_reset_tokens (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash   TEXT NOT NULL,
    expires_at   TIMESTAMPTZ NOT NULL,
    used_at      TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT password_reset_tokens_hash_uidx UNIQUE (token_hash)
);

CREATE TABLE verification_challenges (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID REFERENCES users (id) ON DELETE CASCADE,
    method        TEXT NOT NULL,
    destination   TEXT NOT NULL,
    code_hash     TEXT NOT NULL,
    expires_at    TIMESTAMPTZ NOT NULL,
    consumed_at   TIMESTAMPTZ,
    attempts      INTEGER NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT verification_challenges_method_chk CHECK (method IN ('EMAIL', 'PHONE'))
);

CREATE INDEX verification_challenges_destination_idx
    ON verification_challenges (method, destination);

CREATE TABLE tenants (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         TEXT NOT NULL,
    status       TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT tenants_status_chk CHECK (status IN ('ACTIVE', 'SUSPENDED'))
);

CREATE TABLE memberships (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    user_id      UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role         TEXT NOT NULL,
    status       TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT memberships_role_chk CHECK (role IN (
        'OWNER', 'ADMIN', 'MANAGER', 'SUPERVISOR', 'CASHIER', 'STOCK_CONTROLLER'
    )),
    CONSTRAINT memberships_status_chk CHECK (status IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT memberships_tenant_user_uidx UNIQUE (tenant_id, user_id)
);

CREATE INDEX memberships_user_id_idx ON memberships (user_id);
