CREATE TABLE workspaces (id UUID PRIMARY KEY, name VARCHAR(120) NOT NULL);
CREATE TABLE workspace_locks (id UUID PRIMARY KEY REFERENCES workspaces(id));
CREATE TABLE users (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES workspaces(id), name VARCHAR(160) NOT NULL,
 email VARCHAR(254) NOT NULL UNIQUE, password_hash VARCHAR(255), role VARCHAR(30) NOT NULL,
 active BOOLEAN NOT NULL,
 CHECK (role IN ('ADMIN','WAREHOUSE','SALES','LOGISTICS'))
);
CREATE TABLE sessions (token_hash VARCHAR(64) PRIMARY KEY, user_id UUID NOT NULL REFERENCES users(id), expires_at TIMESTAMP WITH TIME ZONE NOT NULL);
CREATE INDEX sessions_expiry_idx ON sessions(expires_at);
CREATE TABLE inventory_items (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES workspaces(id), sku VARCHAR(32) NOT NULL,
 name VARCHAR(160) NOT NULL, quantity INTEGER NOT NULL CHECK(quantity>=0), price BIGINT NOT NULL CHECK(price>=0),
 cost BIGINT NOT NULL CHECK(cost>=0), minimum INTEGER NOT NULL CHECK(minimum>=0),
 conservation VARCHAR(30) NOT NULL CHECK(conservation IN ('GOOD','DAMAGED','QUARANTINED')), version BIGINT,
 UNIQUE(tenant_id,sku), UNIQUE(tenant_id,id)
);
CREATE TABLE stock_movements (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, product_id UUID NOT NULL, actor_id UUID NOT NULL REFERENCES users(id),
 delta INTEGER NOT NULL CHECK(delta<>0), type VARCHAR(40) NOT NULL, reason VARCHAR(500) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 FOREIGN KEY(tenant_id,product_id) REFERENCES inventory_items(tenant_id,id)
);
CREATE INDEX stock_movements_tenant_idx ON stock_movements(tenant_id,created_at);
CREATE TABLE sales (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES workspaces(id), number VARCHAR(60) NOT NULL,
 status VARCHAR(30) NOT NULL CHECK(status IN ('CONFIRMED','CANCELLED')),
 fulfillment_status VARCHAR(30) NOT NULL, dispatch_id UUID,
 request_key VARCHAR(100), fingerprint VARCHAR(10000), created_at TIMESTAMP WITH TIME ZONE NOT NULL, version BIGINT,
 UNIQUE(tenant_id,request_key), UNIQUE(tenant_id,id)
);
CREATE TABLE sale_lines (
 sale_id UUID NOT NULL REFERENCES sales(id), line_order INTEGER NOT NULL,
 id UUID NOT NULL UNIQUE, product_id UUID NOT NULL REFERENCES inventory_items(id), name VARCHAR(160) NOT NULL,
 sku VARCHAR(32) NOT NULL, quantity INTEGER NOT NULL CHECK(quantity>0), returned INTEGER NOT NULL CHECK(returned>=0 AND returned<=quantity),
 price_cents BIGINT NOT NULL CHECK(price_cents>=0), cost_cents BIGINT NOT NULL CHECK(cost_cents>=0),
 PRIMARY KEY(sale_id,line_order)
);
CREATE TABLE drivers (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES workspaces(id), name VARCHAR(160) NOT NULL,
 license VARCHAR(40) NOT NULL, UNIQUE(tenant_id,license), UNIQUE(tenant_id,id)
);
CREATE TABLE vehicles (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES workspaces(id), name VARCHAR(160) NOT NULL,
 plate VARCHAR(20) NOT NULL, UNIQUE(tenant_id,plate), UNIQUE(tenant_id,id)
);
CREATE TABLE dispatches (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, driver_id UUID NOT NULL, vehicle_id UUID NOT NULL,
 destination VARCHAR(240) NOT NULL, status VARCHAR(30) NOT NULL CHECK(status IN ('PENDING','IN_TRANSIT','DELIVERED')),
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, recipient VARCHAR(160), delivered_at TIMESTAMP WITH TIME ZONE, version BIGINT,
 FOREIGN KEY(tenant_id,driver_id) REFERENCES drivers(tenant_id,id), FOREIGN KEY(tenant_id,vehicle_id) REFERENCES vehicles(tenant_id,id),
 UNIQUE(tenant_id,id)
);
CREATE TABLE dispatch_lines (
 dispatch_id UUID NOT NULL REFERENCES dispatches(id), line_order INTEGER NOT NULL, id UUID NOT NULL,
 sale_id UUID NOT NULL REFERENCES sales(id), sale_line_id UUID NOT NULL REFERENCES sale_lines(id),
 product_id UUID NOT NULL REFERENCES inventory_items(id), name VARCHAR(160) NOT NULL,
 quantity INTEGER NOT NULL CHECK(quantity>0), PRIMARY KEY(dispatch_id,line_order), UNIQUE(sale_line_id)
);
CREATE TABLE delivery_incidents (
 dispatch_id UUID NOT NULL REFERENCES dispatches(id), incident_order INTEGER NOT NULL,
 id UUID NOT NULL, description VARCHAR(500) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 PRIMARY KEY(dispatch_id,incident_order)
);
