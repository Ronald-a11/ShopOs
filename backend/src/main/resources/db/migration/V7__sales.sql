-- Sales, line items, tenders, receipt sequences, and return/void idempotency.

CREATE TABLE sales (
    tenant_id         UUID NOT NULL,
    id                UUID NOT NULL DEFAULT gen_random_uuid(),
    branch_id         UUID NOT NULL,
    device_id         UUID NOT NULL,
    client_sale_id    UUID,
    receipt_number    TEXT NOT NULL,
    status            TEXT NOT NULL,
    currency          VARCHAR(3) NOT NULL,
    occurred_at       TIMESTAMPTZ NOT NULL,
    cashier_user_id   UUID NOT NULL,
    shift_id          UUID NOT NULL,
    original_sale_id  UUID,
    config_version    INTEGER NOT NULL DEFAULT 0,
    subtotal          NUMERIC(19, 4) NOT NULL,
    discount_total    NUMERIC(19, 4) NOT NULL DEFAULT 0,
    tax_total         NUMERIC(19, 4) NOT NULL DEFAULT 0,
    grand_total       NUMERIC(19, 4) NOT NULL,
    change_amount     NUMERIC(19, 4),
    fiscal_status     TEXT NOT NULL DEFAULT 'PENDING_SUBMISSION',
    reason            TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, id),
    CONSTRAINT sales_branch_fk
        FOREIGN KEY (tenant_id, branch_id) REFERENCES branches (tenant_id, id),
    CONSTRAINT sales_device_fk
        FOREIGN KEY (tenant_id, device_id) REFERENCES pos_devices (tenant_id, id),
    CONSTRAINT sales_shift_fk
        FOREIGN KEY (tenant_id, shift_id) REFERENCES cashier_shifts (tenant_id, id),
    CONSTRAINT sales_original_fk
        FOREIGN KEY (tenant_id, original_sale_id) REFERENCES sales (tenant_id, id),
    CONSTRAINT sales_status_check CHECK (
        status IN ('COMMITTED', 'RETURNED', 'VOIDED', 'PARTIALLY_RETURNED')
    ),
    CONSTRAINT sales_receipt_unique UNIQUE (tenant_id, receipt_number)
);

CREATE UNIQUE INDEX sales_client_sale_unique
    ON sales (tenant_id, device_id, client_sale_id)
    WHERE client_sale_id IS NOT NULL;

CREATE INDEX sales_branch_occurred_idx
    ON sales (tenant_id, branch_id, occurred_at DESC);
CREATE INDEX sales_cashier_occurred_idx
    ON sales (tenant_id, cashier_user_id, occurred_at DESC);
CREATE INDEX sales_original_idx
    ON sales (tenant_id, original_sale_id);

CREATE TABLE sale_items (
    tenant_id    UUID NOT NULL,
    id           UUID NOT NULL DEFAULT gen_random_uuid(),
    sale_id      UUID NOT NULL,
    product_id   UUID NOT NULL,
    sku          TEXT NOT NULL,
    description  TEXT NOT NULL,
    quantity     NUMERIC(19, 4) NOT NULL,
    unit_price   NUMERIC(19, 4) NOT NULL,
    tax_code     TEXT NOT NULL,
    discount     NUMERIC(19, 4) NOT NULL DEFAULT 0,
    tax_amount   NUMERIC(19, 4) NOT NULL DEFAULT 0,
    line_total   NUMERIC(19, 4) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, id),
    CONSTRAINT sale_items_sale_fk
        FOREIGN KEY (tenant_id, sale_id) REFERENCES sales (tenant_id, id),
    CONSTRAINT sale_items_product_fk
        FOREIGN KEY (tenant_id, product_id) REFERENCES products (tenant_id, id),
    CONSTRAINT sale_items_quantity_check CHECK (quantity <> 0)
);

CREATE INDEX sale_items_sale_idx ON sale_items (tenant_id, sale_id);

CREATE TABLE sale_payments (
    tenant_id           UUID NOT NULL,
    id                  UUID NOT NULL DEFAULT gen_random_uuid(),
    sale_id             UUID NOT NULL,
    method              TEXT NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    amount              NUMERIC(19, 4) NOT NULL,
    provider_reference  TEXT,
    terminal_id         TEXT,
    card_scheme         TEXT,
    masked_pan_last4    TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, id),
    CONSTRAINT sale_payments_sale_fk
        FOREIGN KEY (tenant_id, sale_id) REFERENCES sales (tenant_id, id),
    CONSTRAINT sale_payments_method_check CHECK (
        method IN ('CASH', 'CARD', 'MOBILE_MONEY', 'VOUCHER', 'OTHER')
    ),
    CONSTRAINT sale_payments_amount_check CHECK (amount > 0)
);

CREATE INDEX sale_payments_sale_idx ON sale_payments (tenant_id, sale_id);

CREATE TABLE sale_receipt_sequences (
    tenant_id  UUID NOT NULL,
    branch_id  UUID NOT NULL,
    next_value BIGINT NOT NULL DEFAULT 1,
    PRIMARY KEY (tenant_id, branch_id),
    CONSTRAINT sale_receipt_sequences_branch_fk
        FOREIGN KEY (tenant_id, branch_id) REFERENCES branches (tenant_id, id)
);

CREATE TABLE sale_operations (
    tenant_id           UUID NOT NULL,
    client_operation_id UUID NOT NULL,
    operation_type      TEXT NOT NULL,
    aggregate_id        UUID NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, client_operation_id),
    CONSTRAINT sale_operations_type_check CHECK (operation_type IN ('RETURN', 'VOID'))
);

ALTER TABLE sales ENABLE ROW LEVEL SECURITY;
ALTER TABLE sales FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_sales ON sales
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER TABLE sale_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE sale_items FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_sale_items ON sale_items
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER TABLE sale_payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE sale_payments FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_sale_payments ON sale_payments
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER TABLE sale_receipt_sequences ENABLE ROW LEVEL SECURITY;
ALTER TABLE sale_receipt_sequences FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_sale_receipt_sequences ON sale_receipt_sequences
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER TABLE sale_operations ENABLE ROW LEVEL SECURITY;
ALTER TABLE sale_operations FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_sale_operations ON sale_operations
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);
