-- On-hand balances and the movement ledger. quantity_delta is signed; zero is rejected.

CREATE TABLE inventory_balances (
    tenant_id    UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    branch_id    UUID NOT NULL REFERENCES branches (id) ON DELETE CASCADE,
    product_id   UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    quantity     NUMERIC(14, 3) NOT NULL DEFAULT 0,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, branch_id, product_id)
);

CREATE TABLE inventory_movements (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    branch_id        UUID NOT NULL REFERENCES branches (id) ON DELETE CASCADE,
    product_id       UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    quantity_delta   NUMERIC(14, 3) NOT NULL,
    movement_type    TEXT NOT NULL,
    reason           TEXT,
    note             TEXT,
    reference_id     UUID,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT inventory_movements_delta_chk CHECK (quantity_delta <> 0),
    CONSTRAINT inventory_movements_type_chk CHECK (movement_type IN ('RECEIPT', 'SALE', 'RETURN', 'ADJUSTMENT'))
);

CREATE INDEX inventory_movements_branch_idx
    ON inventory_movements (tenant_id, branch_id, created_at DESC);
