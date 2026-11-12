-- One OPEN cashier shift per device. Cash movements are till events, not sales.

CREATE TABLE cashier_shifts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    branch_id       UUID NOT NULL REFERENCES branches (id) ON DELETE CASCADE,
    device_id       UUID NOT NULL REFERENCES pos_devices (id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status          TEXT NOT NULL DEFAULT 'OPEN',
    opening_float   NUMERIC(14, 2) NOT NULL,
    counted_cash    NUMERIC(14, 2),
    expected_cash   NUMERIC(14, 2),
    variance        NUMERIC(14, 2),
    note            TEXT,
    opened_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    closed_at       TIMESTAMPTZ,
    CONSTRAINT cashier_shifts_status_chk CHECK (status IN ('OPEN', 'CLOSED')),
    CONSTRAINT cashier_shifts_opening_float_chk CHECK (opening_float >= 0),
    CONSTRAINT cashier_shifts_counted_cash_chk CHECK (counted_cash IS NULL OR counted_cash >= 0)
);

CREATE UNIQUE INDEX cashier_shifts_one_open_device_uidx
    ON cashier_shifts (tenant_id, device_id)
    WHERE status = 'OPEN';

CREATE INDEX cashier_shifts_branch_idx ON cashier_shifts (tenant_id, branch_id);

CREATE TABLE cash_movements (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    shift_id     UUID NOT NULL REFERENCES cashier_shifts (id) ON DELETE CASCADE,
    type         TEXT NOT NULL,
    amount       NUMERIC(14, 2) NOT NULL,
    note         TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT cash_movements_type_chk CHECK (type IN ('PAID_IN', 'PAID_OUT', 'DROP')),
    CONSTRAINT cash_movements_amount_chk CHECK (amount > 0)
);

CREATE INDEX cash_movements_shift_idx ON cash_movements (tenant_id, shift_id);
