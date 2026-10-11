# Lead Docket API guide

All local API routes require HTTP Basic credentials from APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD. Import the Postman collection and set its password. Response bodies from upstream are returned as provider JSON; the application does not translate the provider's success flag.

| Local route | Behavior |
| --- | --- |
| POST /api/leaddocket/opportunities | Convert a JSON string map into form fields and POST to LD_OPPORTUNITY_URL |
| POST /api/leaddocket/operations/{name} | Invoke a server-configured tenant REST operation |
| GET /api/webhooks/inbox | Latest 100 delivery statuses |
| POST /api/webhooks/inbox/{id}/retry | Retry a DEAD event |

## Opportunity intake

```json
{"First":"Sample","Last":"Contact","Phone":"+15555550100","Email":"sample@example.com","Summary":"Integration example"}
```

The client uses application/x-www-form-urlencoded and encodes values, including plus signs. Field names are configurable in Lead Docket. A successful provider response normally contains success:true and opportunityId. A response containing success:false is an application failure even if HTTP status is 200; the caller must check it. No REST API key is attached to the generated source integration URL. Treat that URL as sensitive.

## Tenant REST operations

Public documentation confirms the tenant console and API-key configuration, but not every endpoint, DTO or exact key placement. Open https://YOUR_FIRM.leaddocket.com/api/ with your authorized account and copy its contract. Set LD_API_BASE_URL, LD_API_KEY, and explicitly choose LD_API_AUTH_MODE=HEADER or QUERY plus LD_API_KEY_NAME. The default UNCONFIGURED prevents guessing authentication. If your tenant needs another authentication scheme, adapt LeadDocketClient before use.

Copy config/leaddocket-routes.example.yml to config/application.yml, replace its placeholder path with a verified path, and launch with SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:./config/. Spring reads the named operation from this additional configuration. Remove unused routes.

For the example operation read-lead, call POST /api/leaddocket/operations/read-lead:

```json
{"pathParameters":{"id":"123"},"query":{"exampleFilter":["value"]},"body":null}
```

The local proxy is POST regardless of the configured upstream method. Each operation uses a relative path and fixed method GET/POST/PUT/PATCH/DELETE; caller-supplied arbitrary upstream URLs are rejected. Query and path values are encoded. JSON bodies are passed through. For form operations set content-type: application/x-www-form-urlencoded and supply an object map as body. Verify actual query and payload fields in your console. No active lead CRUD paths are invented or shipped.

## Official references

- [API key and tenant console](https://support.leaddocket.com/hc/en-us/articles/4414934625563-API-Key-and-Console)
- [Third-party form integration](https://support.leaddocket.com/hc/en-us/articles/360045902691-Integrating-Third-Party-Sources-With-Lead-Docket)
- [Integrations and intake/API distinction](https://support.leaddocket.com/hc/en-us/articles/360058912291-Lead-Docket-Integrations)

Reviewed for this project on 2026-10-11. Live tenant access was not available.
