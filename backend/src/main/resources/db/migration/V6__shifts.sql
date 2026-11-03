-- Cashier shifts, cash movements, and idempotent shift operations.

CREATE TABLE cashier_shifts (
    tenant_id        UUID NOT NULL,
    id               UUID NOT NULL DEFAULT gen_random_uuid(),
    branch_id        UUID NOT NULL,
    device_id        UUID NOT NULL,
    cashier_user_id  UUID NOT NULL,
    status           TEXT NOT NULL,
    opening_float    NUMERIC(19, 4) NOT NULL,
    currency         VARCHAR(3) NOT NULL,
    expected_cash    NUMERIC(19, 4) NOT NULL,
    counted_cash     NUMERIC(19, 4),
    variance         NUMERIC(19, 4),
    approval_reason  TEXT,
    opened_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    closed_at        TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, id),
    CONSTRAINT cashier_shifts_branch_fk
        FOREIGN KEY (tenant_id, branch_id) REFERENCES branches (tenant_id, id),
    CONSTRAINT cashier_shifts_device_fk
        FOREIGN KEY (tenant_id, device_id) REFERENCES pos_devices (tenant_id, id),
    CONSTRAINT cashier_shifts_status_check CHECK (status IN ('OPEN', 'COUNTED', 'CLOSED'))
);

CREATE UNIQUE INDEX cashier_shifts_one_open_per_cashier_device_idx
    ON cashier_shifts (tenant_id, cashier_user_id, device_id)
    WHERE status = 'OPEN';

CREATE INDEX cashier_shifts_branch_opened_idx
    ON cashier_shifts (tenant_id, branch_id, opened_at DESC);

CREATE TABLE shift_cash_movements (
    tenant_id   UUID NOT NULL,
    id          UUID NOT NULL DEFAULT gen_random_uuid(),
    shift_id    UUID NOT NULL,
    type        TEXT NOT NULL,
    amount      NUMERIC(19, 4) NOT NULL,
    currency    VARCHAR(3) NOT NULL,
    reason      TEXT,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, id),
    CONSTRAINT shift_cash_movements_shift_fk
        FOREIGN KEY (tenant_id, shift_id) REFERENCES cashier_shifts (tenant_id, id),
    CONSTRAINT shift_cash_movements_type_check CHECK (type IN ('PAID_IN', 'PAID_OUT', 'DROP')),
    CONSTRAINT shift_cash_movements_amount_check CHECK (amount > 0)
);

CREATE INDEX shift_cash_movements_shift_occurred_idx
    ON shift_cash_movements (tenant_id, shift_id, occurred_at);

CREATE TABLE shift_operations (
    tenant_id           UUID NOT NULL,
    client_operation_id UUID NOT NULL,
    operation_type      TEXT NOT NULL,
    aggregate_id        UUID NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, client_operation_id),
    CONSTRAINT shift_operations_type_check CHECK (operation_type IN ('OPEN', 'CASH_MOVEMENT', 'CLOSE'))
);

ALTER TABLE cashier_shifts ENABLE ROW LEVEL SECURITY;
ALTER TABLE cashier_shifts FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_cashier_shifts ON cashier_shifts
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER TABLE shift_cash_movements ENABLE ROW LEVEL SECURITY;
ALTER TABLE shift_cash_movements FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_shift_cash_movements ON shift_cash_movements
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER TABLE shift_operations ENABLE ROW LEVEL SECURITY;
ALTER TABLE shift_operations FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_shift_operations ON shift_operations
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);
