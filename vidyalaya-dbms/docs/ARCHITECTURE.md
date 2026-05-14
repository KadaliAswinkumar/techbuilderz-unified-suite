# Vidyalaya Architecture

## Overview

- **Backend:** Spring Boot 3.3, Java 17+ (Maven `java.version` 17 by default), PostgreSQL, Flyway, JWT + refresh tokens, BCrypt, Spring Security RBAC.
- **Multi-tenancy:** Database-per-tenant. Master database `vidyalaya_master` stores tenant registry (connection metadata, branding, encrypted OpenAI key). Each school has its own PostgreSQL database provisioned at onboarding.
- **Tenant routing:** `TenantMvcInterceptor` sets `TenantContext` from JWT claim `tid` or `X-Tenant-Slug` header. `TenantRoutingDataSource` + `LazyConnectionDataSourceProxy` route JPA to the correct tenant pool.
- **Public site:** `PublicTenantFilter` parses `/public/{slug}/...` and sets tenant context for Thymeleaf + tenant DB reads.
- **Frontend:** React 18 + Vite + TypeScript + Tailwind + TanStack Query + Recharts. Proxies `/api` to the Spring server.

## Security

- Access JWT (HS256-compatible key length) with `role`, optional `tid` (tenant slug), `sub` (user UUID).
- Refresh tokens are opaque, stored SHA-256 hashed (tenant DB or `super_refresh_tokens` in master for super admin).
- Super admin credentials come from environment variables (`SUPER_ADMIN_USERNAME` / `SUPER_ADMIN_PASSWORD`).
- At-rest encryption for tenant DB passwords and optional per-tenant OpenAI keys uses AES-256-GCM (`VIDYALAYA_ENCRYPTION_KEY`, Base64, 32 bytes).

## Diagram

```
Client → Spring Security (JWT) → TenantMvcInterceptor → Services → JPA → Tenant DB
Super-admin routes → Master JDBC only (no tenant JPA)
```
