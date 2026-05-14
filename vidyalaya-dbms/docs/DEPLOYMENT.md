# Deployment

## Environment variables

| Variable | Purpose |
|----------|---------|
| `MASTER_DB_URL` | JDBC URL for master DB (default `jdbc:postgresql://localhost:5433/vidyalaya_master`; Compose maps host **5433** → container 5432) |
| `MASTER_DB_USER` / `MASTER_DB_PASSWORD` | Postgres credentials (must be able to `CREATE DATABASE`) |
| `JWT_SECRET` | At least 32 bytes for HMAC signing |
| `VIDYALAYA_ENCRYPTION_KEY` | Base64 of 32-byte AES key for secrets at rest |
| `SUPER_ADMIN_USERNAME` / `SUPER_ADMIN_PASSWORD` | Bootstrap super admin |
| `OPENAI_API_KEY` | Fallback if tenant has no per-tenant key |
| `CORS_ALLOWED_ORIGINS` | Comma-separated origins; defaults include `localhost`, `127.0.0.1`, and `[::1]` on port **5173** |

## Local Postgres (Compose)

**Podman** (when Docker is not installed):

```bash
podman compose -f docker-compose.yml up -d
```

**Docker:**

```bash
docker compose up -d
```

On macOS with Podman, run `podman machine start` if the VM is stopped. Ensure the DB user can create databases for tenant provisioning.

## Build

```bash
cd backend && mvn -DskipTests package
cd ../frontend && npm ci && npm run build
```

Serve the Spring Boot JAR; place the `frontend/dist` assets behind your CDN or embed as static resource if desired (current setup uses Vite dev proxy for local).

## Production notes

- Terminate TLS at ingress; set secure cookies if you move refresh tokens to cookies.
- Rotate `JWT_SECRET` and invalidate refresh families on compromise.
- Back up each tenant database independently.
