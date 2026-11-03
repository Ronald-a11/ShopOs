-- Tenant-global product catalogue and barcodes.

CREATE TABLE products (
    tenant_id    UUID NOT NULL REFERENCES tenants (id),
    id           UUID NOT NULL DEFAULT gen_random_uuid(),
    sku          TEXT NOT NULL,
    name         TEXT NOT NULL,
    description  TEXT,
    tax_code     TEXT NOT NULL,
    unit_price   NUMERIC(19, 4),
    currency     VARCHAR(3),
    active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, id),
    CONSTRAINT products_sku_unique UNIQUE (tenant_id, sku)
);

CREATE TABLE product_barcodes (
    tenant_id       UUID NOT NULL,
    barcode         TEXT NOT NULL,
    product_id      UUID NOT NULL,
    unit_multiplier NUMERIC(19, 4) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, barcode),
    CONSTRAINT product_barcodes_product_fk
        FOREIGN KEY (tenant_id, product_id) REFERENCES products (tenant_id, id),
    CONSTRAINT product_barcodes_unit_multiplier_positive CHECK (unit_multiplier > 0)
);

CREATE INDEX product_barcodes_product_idx ON product_barcodes (tenant_id, product_id);
CREATE INDEX products_name_idx ON products (tenant_id, name);

ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE products FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_products ON products
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER TABLE product_barcodes ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_barcodes FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_product_barcodes ON product_barcodes
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);
