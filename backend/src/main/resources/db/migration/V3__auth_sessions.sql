-- Refresh sessions for JWT rotation. Looked up by token hash before tenant context.

CREATE TABLE auth_sessions (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID NOT NULL REFERENCES users (id),
    tenant_id            UUID NOT NULL REFERENCES tenants (id),
    refresh_token_hash   TEXT NOT NULL,
    device_id            UUID,
    expires_at           TIMESTAMPTZ NOT NULL,
    revoked_at           TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT auth_sessions_refresh_token_hash_unique UNIQUE (refresh_token_hash)
);

CREATE INDEX auth_sessions_user_tenant_idx ON auth_sessions (user_id, tenant_id);
CREATE INDEX auth_sessions_expires_at_idx ON auth_sessions (expires_at);
