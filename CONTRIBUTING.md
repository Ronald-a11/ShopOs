# Contributing to ShopOS

## The rule

Nobody pushes to `main` except the technical lead. Everyone else works on a
branch and opens a pull request. A PR merges only when CI is green and the
technical lead has approved it.

## Workflow

1. Pick an issue (or ask for one). Assign it to yourself.
2. Branch from an up-to-date `main`:
   ```sh
   git switch main && git pull
   git switch -c feat/sales-commit
   ```
   Prefixes: `feat/`, `fix/`, `chore/`, `docs/`, `test/`.
3. Commit small, focused changes. Message format:
   `area: what changed` — e.g. `sales: commit sale with idempotency key`.
4. Push and open a PR against `main`. Fill in the template, link the issue
   (`Closes #12`).
5. CI runs backend, frontend and Docker builds. Fix anything red before
   asking for review.
6. Address review comments with new commits (don't force-push during review).
7. The technical lead merges with **squash and merge**. Delete your branch.

Keep PRs small — under ~400 changed lines is easy to review same-day.

## What CI checks

| Job | Runs |
|-----|------|
| Backend | `./mvnw verify`: unit tests, Testcontainers integration tests, ArchUnit module rules |
| Frontend | `npm run lint`, `typecheck`, `test`, `build` |
| Docker image | full image build |

## After merge

Every merge to `main` builds one Docker image and deploys it to **staging**
automatically. The technical lead promotes that same image to **production**
by running the *Deploy production* workflow; nothing is rebuilt.

## Things that get a PR sent back

- `tenantId` read from a request body or path
- `double`/`float` for money
- Editing a Flyway migration that is already on `main`
- Deleting a sale or fiscal record instead of reversing it
- Secrets, tokens or fiscal credentials in code, config or logs
- Marking a document fiscalised before FDMS accepted it
