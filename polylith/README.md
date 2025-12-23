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

**Production path (recommended):** use a small backend (e.g., Lambda + API Gateway) to hold refresh tokens server-side and issue short-lived access tokens to the SPA via HttpOnly cookies.
