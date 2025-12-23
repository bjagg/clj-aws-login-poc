# Polylith (aws-login) — re-frame + PKCE

This folder is the Polylith workspace for the **re-frame PKCE** implementation.

## Structure

- `components/oauth-pkce` — PKCE + token exchange + token storage
- `components/app-config` — runtime config from `window.__APP_CONFIG__`
- `bases/webapp` — minimal re-frame SPA (Login/Logout + claims display)
- `projects/webapp-pkce` — shadow-cljs build (dev server + release build)

## Run locally

```bash
cd projects/webapp-pkce
npm install
npx shadow-cljs watch app
```

Open: [http://localhost:3000]

## Refresh tokens (PoC approach)

This project demonstrates a **simple refresh-on-demand** strategy:

- The initial PKCE code exchange stores:
  - `id_token`, `access_token`, and (if provided) `refresh_token`
- On startup (or before protected API calls), the app can dispatch:
  - `[:auth/ensure-fresh]`
- If the access token expires soon (default: 60s), the app uses:
  - `grant_type=refresh_token` against Cognito `/oauth2/token`

### Security note

For simplicity, the PoC stores tokens in **localStorage**, including the refresh token.
That is *not* the safest choice for production because JS-accessible storage is vulnerable to XSS.

**Production path (recommended):** use a small backend (e.g., Lambda + API Gateway) to hold refresh tokens server-side
and issue short-lived access tokens to the SPA via HttpOnly cookies.

---

## What this repo teaches

1. How to deploy AWS Cognito + Hosted UI for a SPA
2. How to implement Authorization Code + **PKCE** in a re-frame app
3. How to add **local signup/login** (Cognito native users)
4. How to add **refresh-on-demand** (PoC approach)

---

## Quick start (local)

1. Deploy AWS stacks (see `smoke-pkce/` or your project’s infra/scripts)
2. Create `config.js` from `config.example.js`
3. Run:
   - `npm install`
   - `npx shadow-cljs watch app`
4. Open:
   - `http://localhost:3000`

---

## Local signup/login

See: `docs/local-signup-login.md`

---

## Refresh tokens

See: `docs/refresh-tokens.md`

---

## Production note

For production, prefer a BFF/Lambda approach to keep refresh tokens out of browser storage.
