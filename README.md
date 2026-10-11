# Lead Docket Spring Boot integration

Independent Maven project using Java 17+, Spring Boot 4.0.6, JDBC, Liquibase, local H2, and PostgreSQL through Docker Compose.

Opportunity form intake, configurable tenant REST operations, HMAC-verified webhooks, and a durable inbox.

## Quick start

```bash
cp .env.example .env
# Edit .env: replace passwords and configure provider settings.
set -a
. ./.env
set +a
java -jar dist/leaddocket-integration-1.0.0.jar
```

The executable JAR is included. Run from the project directory; H2 stores data in ./data. Direct Java/Maven launches do not load .env automatically. PowerShell users should set the same variables using $env:NAME="value".

```bash
./mvnw clean verify
./mvnw spring-boot:run
# Windows: mvnw.cmd clean verify
```

The wrapper downloads Maven and dependencies on first build. JDK 17+ is required. Local base URL: http://localhost:8081. Health: `curl http://localhost:8081/actuator/health`.

## Provider setup

Set LD_OPPORTUNITY_URL to the generated URL in Manage → Integrations. Field names must match that integration. Intake does not require a REST API key. Set LD_WEBHOOK_KEY to the organization key in Manage → Webhook Rules.

General REST paths and exact API-key placement must be copied from your firm's authenticated /api/ console. They are deliberately unconfigured. See docs/API.md and config/leaddocket-routes.example.yml. Public docs do not expose a complete tenant REST contract; this project does not claim preconfigured lead CRUD operations.

## Docker

```bash
docker compose up --build
```

Compose reads .env, starts a separate PostgreSQL database, applies migrations, and binds the app to 127.0.0.1:8081. Add an HTTPS reverse proxy for public webhooks. Container runtime is non-root. `docker compose down` preserves the database volume.

## Documentation

- [API guide](docs/API.md)
- [Webhook guide](docs/WEBHOOKS.md)
- [Operations and extension points](docs/OPERATIONS.md)
- [Build verification](docs/BUILD-VERIFICATION.md)
- Editable examples: examples/requests.http and postman/leaddocket.postman_collection.json.

All /api/** routes use administrator HTTP Basic authentication. APP_ADMIN_PASSWORD must have at least 16 characters. Webhooks use provider verification. Deploy with HTTPS. The supplied handler writes an audit event; replace AuditEventHandler to implement your business workflow.
