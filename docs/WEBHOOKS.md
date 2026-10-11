# Lead Docket webhooks

Endpoint: POST /webhooks/leaddocket. Configure an external HTTPS URL in Manage → Webhook Rules and choose your triggers. Copy the organization authentication key at the bottom of that page into LD_WEBHOOK_KEY.

The endpoint verifies x-leaddocket-signature as the 64-character hexadecimal HMAC-SHA256 of the exact raw request bytes, keyed with the UTF-8 authentication key. Comparison is constant-time. The official article describes keyed SHA-256 and links an HMAC-SHA256 verifier; that linked verifier is the basis for this implementation. Validate a real delivery in your tenant before deployment. Do not parse/reserialize JSON before computing a signature.

Missing or invalid signature returns 401; missing configured key returns 503. Invalid JSON/object or identity returns 400 and bodies above 1 MiB return 413. Accepted events are durably stored and return 200 with eventId and duplicate status. Processing runs after acknowledgement.

## Delivery identity

No universal unique webhook delivery ID is documented publicly. EventTypeId is an event-type enum, and LeadDocketLeadId identifies a lead; neither is a unique delivery ID.

The default LD_DEDUPLICATION_MODE=PAYLOAD_HASH hashes the exact bytes. This suppresses byte-identical retries but can also collapse two legitimate identical events. Choose NONE to retain every delivery with a generated ID if identical events must be preserved. If your actual payload includes a truly unique delivery ID, set LD_EVENT_ID_FIELD to its top-level field name. It takes precedence over payload-hash mode. Do not choose EventTypeId or a lead/contact ID as a delivery key. Changed bytes under the same configured ID return 400.

The audit handler reads EventType and LeadDocketLeadId (or ContactId), without enforcing a fixed event-specific DTO. Customize the handler for your triggers.

## Local verification example

With the application's LD_WEBHOOK_KEY exported, run:

```bash
python3 scripts/send-test-webhook.py
```

This submits examples/webhook.json to localhost:8081, signs its exact bytes, and prints the response. This is a synthetic local test; no real Lead Docket account is contacted.

## Official references

- [Manage webhooks and signature verification](https://support.leaddocket.com/hc/en-us/articles/5019884869147-Manage-Webhooks)
- [Webhook payload fields](https://support.leaddocket.com/hc/en-us/articles/360045259012-Fields-Sent-with-Webhooks)
