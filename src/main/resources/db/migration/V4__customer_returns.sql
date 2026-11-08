CREATE TABLE customer_returns (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, sale_id UUID NOT NULL, sale_line_id UUID NOT NULL REFERENCES sale_lines(id),
 product_id UUID NOT NULL REFERENCES inventory_items(id), quantity INTEGER NOT NULL CHECK(quantity>0),
 disposition VARCHAR(20) NOT NULL CHECK(disposition IN ('RESTOCK','LOSS')), reason VARCHAR(500) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, loss_cents BIGINT NOT NULL CHECK(loss_cents>=0),
 FOREIGN KEY(tenant_id,sale_id) REFERENCES sales(tenant_id,id)
);
