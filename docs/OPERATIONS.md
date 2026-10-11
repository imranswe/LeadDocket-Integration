# Lead Docket operations

PORT defaults to 8081. DB_URL, DB_USERNAME and DB_PASSWORD override H2. Each project has its own database and configuration. PostgreSQL migrations are provided; Docker/PostgreSQL execution was not exercised in this environment.

## Inbox processing

Accepted notifications commit to webhook_inbox before acknowledgement. A worker polls every two seconds, locks a pending row in a database transaction, calls WebhookEventHandler, and records completion atomically with the supplied JDBC audit handler. Failures roll back handler changes and record exponential backoff. Five failed attempts mark an event DEAD. Run one application worker per database: multiple replicas need additional distributed coordination. External side effects require their own idempotency; they cannot participate in the JDBC transaction.

WEBHOOK_WORKER_ENABLED=false pauses processing. Spring properties webhook.poll-ms and webhook.max-attempts tune processing. Maximum raw body is 1 MiB. No retention job is included; define retention for inbox payloads and audit records, which may contain personal information.

GET /api/webhooks/inbox lists the latest 100 statuses without payloads. POST /api/webhooks/inbox/{id}/retry resets a DEAD event. Both require administrator credentials. Monitor DEAD events and database health. Public /actuator/health omits internal details.

## Failures and extension

Missing provider configuration returns 503 when invoked. Invalid input returns 400. Provider errors return 502 with upstream status; upstream rate limits return 429 and Retry-After when available. HTTP calls have bounded timeouts. Writes are not automatically replayed; resolve ambiguous outcomes before retrying.

Replace AuditEventHandler with your domain-specific database workflow. Configuration uses environment secrets; .env and data/build directories are ignored. Configure HTTPS ingress, backups, restricted administrator access, and monitoring for deployment.

This runnable starter supplies integration plumbing and an audit handler, without a UI or automatic domain synchronization. Live provider access requires your credentials and tenant/account setup.
