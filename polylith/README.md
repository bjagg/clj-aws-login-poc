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

Open: http://localhost:3000
