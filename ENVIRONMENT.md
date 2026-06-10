# Environment & Configuration

This document describes runtime configuration, environment variables, and secrets handling.

## Environment Variables

Common variables used in production:

- `SPRING_PROFILES_ACTIVE` — set to `prod` in production.
- `DATABASE_URL` / `SPRING_DATASOURCE_URL` — JDBC connection string.
- `DATABASE_USER` / `SPRING_DATASOURCE_USERNAME`
- `DATABASE_PASSWORD` / `SPRING_DATASOURCE_PASSWORD`
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- `OAUTH_CLIENT_ID`, `OAUTH_CLIENT_SECRET`
- `JWT_SECRET` (if using JWTs)

## Secrets Management

- Never commit secrets to source control.
- Use a secrets manager (cloud KMS, HashiCorp Vault) and inject secrets at deploy time.

## Configuration Tips

- Prefer environment variables over checked-in config for sensitive values.
- Keep non-sensitive defaults in `application.properties` and override via env vars.

## Example (Docker)

Provide a `.env` or use your platform's secret injection. Avoid storing plaintext secrets in CI logs.

