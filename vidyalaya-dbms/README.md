# Vidyalaya — School Management SaaS

Multi-tenant school management (database-per-tenant) with Spring Boot 3 + PostgreSQL and React + Vite + Tailwind dashboard.

## Prerequisites

- Java 17+ (project targets **17** by default; set `<java.version>21</java.version>` in `backend/pom.xml` if you use JDK 21)
- Maven 3.9+
- Node 20+
- **Podman** (recommended here) or Docker — for local PostgreSQL via Compose

## Quick start

**Database (Podman — no Docker required):** from the repo root, ensure the Podman machine is running on macOS (`podman machine start` if needed), then:

```bash
podman compose -f docker-compose.yml up -d
podman compose -f docker-compose.yml ps
```

If you use Docker instead: `docker compose up -d` with the same `docker-compose.yml`.

Postgres is published on **host port 5433** so it does not clash with another database already using **5432**.

**API** (pick one):

```bash
# Recommended: profile dev (skips password login via dev-token), DB on 5433
./scripts/run-backend.sh
```

Or manually:

```bash
cd backend/vidyalaya-server
export MASTER_DB_URL=jdbc:postgresql://localhost:5433/vidyalaya_master
export MASTER_DB_USER=vidyalaya
export MASTER_DB_PASSWORD=vidyalaya
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Or from `backend/` (parent reactor) with the same exports:

```bash
cd backend
mvn -pl vidyalaya-server spring-boot:run -Dspring-boot.run.profiles=dev
```

**Important:** When chaining shell commands, put **`&&`** between them. A typo like `...-pl vidyalaya-servercd ...` (missing space and `&&`) makes Maven look for a module named `vidyalaya-servercd` and fail with “Could not find the selected project in the reactor”.

Console logs include a per-request `rid=` (correlation id) on each line while a request is in flight; each completed HTTP call logs one `API METHOD path -> status in Nms` line. For SQL and extra domain detail: `mvn spring-boot:run -Dspring-boot.run.profiles=api-debug`.

In another terminal:

```bash
cd frontend
npm install
npm run dev
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Dashboard: http://localhost:5173 (or `http://127.0.0.1:5173` / `http://[::1]:5173` — all are in default CORS)

**One-shot local restart (stop Vite + API, Postgres up, reset demo rows if `demo` exists, start API + Vite, smoke-check):**

```bash
chmod +x scripts/restart-local-stack.sh scripts/stop-local-dev.sh scripts/verify-demo-data.sh
./scripts/restart-local-stack.sh
```

Run that in your own terminal (not via a restricted runner) so the JVM can open TCP connections to Postgres. Demo data reset uses `scripts/reset-demo-tenant-data.sql` against the tenant database named in `tenants.db_name` for slug `demo`.

### Smoke check (after API + optional UI are up)

```bash
chmod +x scripts/verify-local.sh
./scripts/verify-local.sh
```

### Already hardened in this repo (so you hit fewer “paper cuts”)

| Area | What we did |
|------|----------------|
| **Postgres port** | Compose publishes **5433** on the host; JDBC default matches (`MASTER_DB_URL`). |
| **Maven `-pl`** | Prefer `cd backend/vidyalaya-server && mvn spring-boot:run` or `./scripts/run-backend.sh` — avoids `vidyalaya-servercd`-style typos. |
| **Bean cycle (SQL init)** | `spring.sql.init.mode=never` + `@Lazy` on `TenantDataSourceFactory` → `TenantRegistry`. |
| **JPA + routing DS** | Tenant EMF sets **PostgreSQL dialect** + `hibernate.boot.allow_jdbc_metadata_access=false` so Hibernate does not open a tenant connection at bootstrap (no `TenantContext` yet). |
| **CORS** | Defaults include **localhost, 127.0.0.1, IPv6 loopback** on port 5173; **OPTIONS /** permitAll**; **`allowCredentials=false`** (JWT is not a cookie); with profiles **`dev`** or **`api-debug`**, extra **`http://localhost:*`** / **`127.0.0.1:*`** patterns. |
| **Vite proxy** | Proxies `/api` and `/public` to **`http://127.0.0.1:8080`** with **`changeOrigin: true`**. |
| **Frontend HTTP** | **`publicHttp`** (`src/lib/http.ts`) for login, dev-token, and refresh — never attaches a stale `Authorization` header. |
| **Flyway auth errors** | Clear message when Postgres password is wrong (28P01). |

### Skip login (local demo only)

With **`npm run dev`**, Vite loads `frontend/.env.development` (`VITE_OPEN_API_DEV=true`). The UI calls **`GET /api/auth/dev-token`** once to store a super-admin JWT — that route exists only when the API runs with Spring profile **`dev`** (`application-dev.yml` sets `vidyalaya.dev.open-api=true`). **`./scripts/run-backend.sh`** enables profile `dev` by default.

For a real login flow, run the API **without** profile `dev` and set `VITE_OPEN_API_DEV=false` in `frontend/.env.development` (or `frontend/.env.local`).

### If the backend will not start

1. **Postgres must be up first** — the log line `Master PostgreSQL is NOT reachable` means Compose is not running or the VM is off (Podman macOS: `podman machine start`).
2. **Port 5433** — Compose maps Postgres to **`localhost:5433`**. If you see `bind: address already in use` for **5433**, change the left port in `docker-compose.yml` and set `MASTER_DB_URL` to match.
3. **`FATAL: password authentication failed for user "vidyalaya"`** — the API uses `MASTER_DB_USER` / `MASTER_DB_PASSWORD` (defaults: `vidyalaya` / `vidyalaya`, same as `docker-compose.yml`). Your DB volume may predate those values — align env vars or run `podman compose down -v` then `up -d` (wipes local data). If you accidentally point at another Postgres on the same host/port, credentials may not match.
4. **Maven cannot download dependencies** — fix DNS/VPN or run `mvn -U -pl vidyalaya-server compile` once on a working network.

**Note:** Spring Boot does not read a `.env` file by itself. Either export variables in your shell, configure your IDE run configuration, or use a tool like `direnv`.

### Tenant resolution (local)

Send header `X-Tenant-Slug: <school-slug>` on API requests after login, or register a tenant and use the returned slug.

### Bootstrap super-admin

On first startup, configure `SUPER_ADMIN_USERNAME` / `SUPER_ADMIN_PASSWORD` in environment (see `.env.example`). Use `POST /api/auth/register-tenant` (super-admin) to onboard a school (creates tenant DB + admin user).

## Documentation

- [docs/API_SPEC.md](docs/API_SPEC.md) — endpoint request/response specs
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)

## License

Proprietary / your license.
