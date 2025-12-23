# Migrating this PoC into your own re-frame app

## What to copy
- `components/oauth-pkce/` (PKCE + token exchange + storage)
- `components/app-config/` (runtime config)
- The relevant bits from the `webapp` base:
  - events for login/logout/callback/token refresh
  - subs for auth state
  - views for your UI hooks (login button, session status)

## What to adapt
### Config source
This repo loads runtime config from `window.__APP_CONFIG__` (e.g., `public/config.js`).
In a real app you might load config from:
- a server endpoint
- environment-injected build artifacts
- a secrets/config service (non-public)

### Token storage choice
PoC uses `localStorage` for simplicity.
Production options:
- **BFF/Lambda session** (recommended): refresh tokens never exposed to JS
- memory-only storage: safer than localStorage but loses session on reload

### Routing
This repo demonstrates dependency-free routing:
- `/callback` path for OAuth
- hash routes for in-app pages

If your app uses a router (reitit/secretary/bidi), keep `/callback` as a real path and integrate the guard logic into your router.

## Production checklist
- [ ] Refresh-on-demand implemented (PoC) OR BFF-based refresh (recommended)
- [ ] Route guarding for protected pages
- [ ] Clear logout behavior (local tokens + Cognito session)
- [ ] Audit / logging plan for auth events
- [ ] CSP headers tuned for your asset pipeline
- [ ] Secrets handled outside the repo (AWS Secrets Manager, SSM, etc.)
