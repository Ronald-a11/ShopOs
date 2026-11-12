-- Sales are idempotent on (tenant, device, client sale id). Receipt numbers are per branch.

CREATE TABLE branch_receipt_sequences (
    tenant_id     UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    branch_id     UUID NOT NULL REFERENCES branches (id) ON DELETE CASCADE,
    last_number   BIGINT NOT NULL,
    PRIMARY KEY (tenant_id, branch_id),
    CONSTRAINT branch_receipt_sequences_number_chk CHECK (last_number >= 0)
);

CREATE TABLE sales (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    branch_id        UUID NOT NULL REFERENCES branches (id) ON DELETE CASCADE,
    device_id        UUID NOT NULL REFERENCES pos_devices (id) ON DELETE CASCADE,
    shift_id         UUID NOT NULL REFERENCES cashier_shifts (id) ON DELETE CASCADE,
    user_id          UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    client_sale_id   TEXT NOT NULL,
    receipt_number   TEXT NOT NULL,
    currency         TEXT NOT NULL,
    status           TEXT NOT NULL DEFAULT 'COMPLETED',
    total_amount     NUMERIC(14, 2) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT sales_status_chk CHECK (status IN ('COMPLETED', 'RETURNED', 'VOID_REQUESTED', 'VOIDED')),
    CONSTRAINT sales_total_chk CHECK (total_amount >= 0),
    CONSTRAINT sales_tenant_device_client_uidx UNIQUE (tenant_id, device_id, client_sale_id)
);

CREATE INDEX sales_tenant_branch_created_idx ON sales (tenant_id, branch_id, created_at DESC);

CREATE TABLE sale_items (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id      UUID NOT NULL REFERENCES sales (id) ON DELETE CASCADE,
    tenant_id    UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    product_id   UUID NOT NULL REFERENCES products (id),
    quantity     NUMERIC(14, 3) NOT NULL,
    unit_price   NUMERIC(14, 2) NOT NULL,
    line_total   NUMERIC(14, 2) NOT NULL,
    CONSTRAINT sale_items_qty_chk CHECK (quantity > 0),
    CONSTRAINT sale_items_price_chk CHECK (unit_price >= 0),
    CONSTRAINT sale_items_total_chk CHECK (line_total >= 0)
);

CREATE INDEX sale_items_sale_id_idx ON sale_items (sale_id);

CREATE TABLE sale_payments (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id               UUID NOT NULL REFERENCES sales (id) ON DELETE CASCADE,
    tenant_id             UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    method                TEXT NOT NULL,
    amount                NUMERIC(14, 2) NOT NULL,
    provider_reference    TEXT,
    CONSTRAINT sale_payments_amount_chk CHECK (amount > 0)
);

CREATE INDEX sale_payments_sale_id_idx ON sale_payments (sale_id);
