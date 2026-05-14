# Premium Production Checklist

## Security Baseline
- Set `SUPER_ADMIN_PASSWORD` in production; blank values now fail startup.
- Keep `vidyalaya.dev.open-api=false` in all non-local environments.
- Disable and monitor any `/api/auth/dev-token` usage in production logs.
- Rotate JWT and encryption secrets periodically.

## Account Recovery
- Use `POST /api/auth/forgot-password` to capture reset requests.
- Execute controlled reset with super-admin using `POST /api/auth/reset-password`.
- Communicate temporary reset completion through approved admin channel.

## Tenant Operations
- Verify tenant slug before provisioning.
- Run tenant DB migrations before onboarding users.
- Keep monthly backup and restore drill logs per tenant.

## App Access and Roles
- Validate role-based frontend navigation using `/api/auth/me`.
- Confirm non-admin users cannot access restricted admin routes.

## Reliability
- Keep deployment runbook with rollback steps.
- Verify login, refresh token rotation, and password change flows after each release.
