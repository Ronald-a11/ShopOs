## What and why

<!-- One or two sentences. Link the issue: Closes #123 -->

## How I tested it

<!-- Commands you ran, screens you checked, tests you added. -->

## Checklist

- [ ] Tenant comes from the session, never from the request body
- [ ] Money is `BigDecimal` / `NUMERIC(19,4)` — no `double` or `float`
- [ ] Retryable writes are idempotent (client UUID, safe to replay)
- [ ] No deletes of committed sales or fiscal records
- [ ] New migration is a new `V<n>__*.sql` file; no merged migration was edited
- [ ] No secrets, tokens or fiscal credentials in code or logs
- [ ] Tests added or updated; CI is green
