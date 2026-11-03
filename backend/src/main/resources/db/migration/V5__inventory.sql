-- Branch inventory balances, append-only movements, and idempotent operations.

CREATE TABLE inventory_balances (
    tenant_id     UUID NOT NULL,
    branch_id     UUID NOT NULL,
    product_id    UUID NOT NULL,
    quantity      NUMERIC(19, 4) NOT NULL DEFAULT 0,
    reorder_level NUMERIC(19, 4),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, branch_id, product_id),
    CONSTRAINT inventory_balances_branch_fk
        FOREIGN KEY (tenant_id, branch_id) REFERENCES branches (tenant_id, id),
    CONSTRAINT inventory_balances_product_fk
        FOREIGN KEY (tenant_id, product_id) REFERENCES products (tenant_id, id)
);

CREATE TABLE inventory_movements (
    tenant_id      UUID NOT NULL,
    id             UUID NOT NULL DEFAULT gen_random_uuid(),
    branch_id      UUID NOT NULL,
    product_id     UUID NOT NULL,
    movement_type  TEXT NOT NULL,
    quantity_delta NUMERIC(19, 4) NOT NULL,
    reference_type TEXT,
    reference_id   UUID,
    occurred_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, id),
    CONSTRAINT inventory_movements_branch_fk
        FOREIGN KEY (tenant_id, branch_id) REFERENCES branches (tenant_id, id),
    CONSTRAINT inventory_movements_product_fk
        FOREIGN KEY (tenant_id, product_id) REFERENCES products (tenant_id, id),
    CONSTRAINT inventory_movements_quantity_delta_check CHECK (quantity_delta <> 0),
    CONSTRAINT inventory_movements_type_check CHECK (
        movement_type IN (
            'OPENING', 'RECEIPT', 'SALE', 'RETURN', 'WASTAGE',
            'ADJUSTMENT', 'TRANSFER_OUT', 'TRANSFER_IN'
        )
    )
);

CREATE INDEX inventory_movements_branch_occurred_idx
    ON inventory_movements (tenant_id, branch_id, occurred_at DESC);
CREATE INDEX inventory_movements_reference_idx
    ON inventory_movements (tenant_id, reference_type, reference_id);

CREATE TABLE inventory_operations (
    tenant_id           UUID NOT NULL,
    client_operation_id UUID NOT NULL,
    operation_type      TEXT NOT NULL,
    aggregate_id        UUID NOT NULL,
    branch_id           UUID NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, client_operation_id),
    CONSTRAINT inventory_operations_type_check CHECK (operation_type IN ('RECEIPT', 'ADJUSTMENT'))
);

ALTER TABLE inventory_balances ENABLE ROW LEVEL SECURITY;
ALTER TABLE inventory_balances FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_inventory_balances ON inventory_balances
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER TABLE inventory_movements ENABLE ROW LEVEL SECURITY;
ALTER TABLE inventory_movements FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_inventory_movements ON inventory_movements
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER TABLE inventory_operations ENABLE ROW LEVEL SECURITY;
ALTER TABLE inventory_operations FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_inventory_operations ON inventory_operations
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);
