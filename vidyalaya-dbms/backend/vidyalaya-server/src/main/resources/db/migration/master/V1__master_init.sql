CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    slug VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    db_name VARCHAR(128) NOT NULL UNIQUE,
    db_host VARCHAR(255) NOT NULL,
    db_port INT NOT NULL,
    db_username VARCHAR(128) NOT NULL,
    db_password_enc TEXT NOT NULL,
    primary_color VARCHAR(32),
    logo_url VARCHAR(512),
    openai_api_key_enc TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tenants_slug ON tenants (slug);
