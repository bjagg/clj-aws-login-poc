# Polylith re-frame PKCE Example

This folder contains the **Polylith workspace** version of the PoC: a minimal **re-frame** SPA that performs
**Authorization Code + PKCE** against **Cognito Hosted UI**, then displays decoded claims.

This README is intentionally short for now; we’ll expand it as the app scaffolding comes together.

---

## What lives here

- `components/` — reusable bricks (PKCE/OAuth, config, JWT helpers, router helpers)
- `bases/` — the runnable SPA base (re-frame UI + routing)
- `projects/` — buildable deployables (e.g., `reframe-spa`)
- `development/` — dev profile/project (REPL/dev tooling)

---

## Goal state

- `components/oauth-pkce` provides:
  - start login (build authorize URL w/ PKCE challenge)
  - handle callback (`?code=...`) + token exchange
  - logout URL builder
  - token storage helpers

- `bases/webapp` provides:
  - a single page with Login/Logout buttons
  - `/callback` handling
  - shows “Not logged in” vs decoded claims

---

## Next steps (scaffold only)

We’ll add:

1. a minimal `shadow-cljs` setup in the `projects/reframe-spa/` project
2. a tiny re-frame app in `bases/webapp`
3. PKCE logic in `components/oauth-pkce` (ported from `smoke-pkce/app.example.js`)

Once the Polylith version works locally, we’ll deploy it using the same AWS stacks you validated in `smoke-pkce/`.
