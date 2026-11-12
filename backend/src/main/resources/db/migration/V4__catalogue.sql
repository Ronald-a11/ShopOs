-- Sellable products. Prices are tax-exclusive unit prices in the tenant currency of sale.

CREATE TABLE products (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    sku             TEXT NOT NULL,
    name            TEXT NOT NULL,
    barcode         TEXT,
    unit_price      NUMERIC(14, 2) NOT NULL,
    tax_rate_code   TEXT,
    status          TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT products_status_chk CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT products_unit_price_chk CHECK (unit_price >= 0),
    CONSTRAINT products_tenant_sku_uidx UNIQUE (tenant_id, sku)
);

CREATE INDEX products_tenant_id_idx ON products (tenant_id);
CREATE INDEX products_tenant_barcode_idx ON products (tenant_id, barcode);
