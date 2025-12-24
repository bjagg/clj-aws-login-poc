# AWS Cognito Social Login (Google) — ClojureScript PoC

This repo is a **learning-focused** proof of concept for adding **social SSO** (Google)
to a **static SPA** hosted on AWS, using:

- **Cognito Hosted UI** + **OAuth 2.0**
- **CloudFront + S3** static hosting
- **Route53 + ACM** (DNS + TLS)
- Two progressively more “real” front-end examples:
  - a tiny vanilla JS smoke test
  - a ClojureScript **re-frame** app (Polylith workspace)

---

## Recommended path

- Start with smoke → smoke-pkce → polylith webapp-pkce

---

## Choose your path

### 1) Smoke test (Implicit Flow) — quickest sanity check

Use this first if you just want to confirm that DNS/ACM/CloudFront/Cognito are wired correctly.

- Go to: `smoke/`
- Run: `./scripts/check-prereqs.sh` then `./scripts/deploy.sh`
- Upload the smoke files to the bucket (see the folder README)

### 2) Smoke test (Authorization Code + PKCE) — best practice for SPAs

This is the recommended OAuth flow for browser-based apps.

- Go to: `smoke-pkce/`
- Run: `./scripts/check-prereqs.sh` then `./scripts/deploy.sh`
- Configure `app.js` from `app.example.js` (see the folder README)

### 3) Polylith re-frame app (PKCE) — the “real” ClojureScript example

This is the full ClojureScript implementation with reusable components.

- Go to: `polylith/`
- Start with: `polylith/README.md`

---

## Important note: don’t deploy both smoke variants to the same domain at once

Both `smoke/` and `smoke-pkce/` assume they own the same DNS + CloudFront alias + Cognito domain config by default.

If you switch between them, **destroy the previous stacks first** (instructions are in each README),
or override `PROJECT_NAME`, `DOMAIN_NAME`, and `COGNITO_DOMAIN_PREFIX`.

---

## Repo layout

- `smoke/` — implicit-flow smoke test + standalone infra/scripts
- `smoke-pkce/` — PKCE smoke test + standalone infra/scripts
- `polylith/` — Polylith workspace for the re-frame PKCE app

---

## Security

Do not commit `app.js` files that contain your environment-specific values:

- `smoke/app.js`
- `smoke-pkce/app.js`

Each folder includes `.gitignore` entries and guidance.

See `SECURITY.md` for additional notes.
