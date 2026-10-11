# Build verification

Verified on 2026-10-11 with OpenJDK 17 and Maven 3.9.12.

- clean verify completed successfully: 13 tests, zero failures/errors.
- Packaged executable JAR launched successfully.
- Public health endpoint returned UP.
- Administrator-authenticated inbox returned 200.
- A verified synthetic webhook was durably accepted and processed by the worker.

HTTP client tests use local fake provider servers, including authentication and write replay protection. Webhook tests exercise validation, authentication, duplicates, size limits, transactional handling and retries. No live credentials were used, no live messages were sent, and Docker/PostgreSQL was not executed. Tenant/account configuration still needs an end-to-end check in your environment.
