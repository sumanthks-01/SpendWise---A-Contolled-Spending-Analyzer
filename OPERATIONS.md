# Operations & Runbook

This runbook covers common operational tasks for running SpendWise in production.

## Start/Stop

- Start: run the container with production environment variables, or use your orchestration tooling.
- Stop: gracefully drain traffic and stop instances.

## Health Checks

- Implement liveness and readiness endpoints in the application.
- Configure load balancer health checks to use readiness probes.

## Backups

- Regularly back up the production database and verify restores.
- Keep backups encrypted and stored offsite.

## Monitoring

- Monitor application metrics (errors, latency, throughput), infrastructure metrics, and logs.
- Configure alerts for high error rates, increased latency, and resource saturation.

## Incident Response

- Triage severity, collect logs and recent deploys, and roll back if necessary.
- Notify stakeholders and follow post-incident review practices.

## Deployments

- Use a controlled deployment strategy (blue/green or canary) for production.
- Run smoke tests after deployment and monitor metrics.

## Maintenance Windows and Notifications

- Schedule maintenance windows for database migrations and disruptive changes.
- Notify users via status page and email as appropriate.

