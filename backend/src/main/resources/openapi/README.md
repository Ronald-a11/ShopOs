# ShopOS OpenAPI contract

[`shopos-v1.yaml`](shopos-v1.yaml) is the agreed P0 REST contract for `/api/v1`.

Controllers and Flyway schema land only after this file is reviewed. Both
backend and frontend implement against this contract; do not invent routes
outside it for P0.

- Tenant comes from the auth token, never from path or body.
- Branch scoping is documented on each operation.
- Money and quantities are decimal strings.
