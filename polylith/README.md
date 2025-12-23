# Polylith AWS Cognito PKCE Login (re-frame)

This repository contains **learning-focused implementations** of AWS Cognito login
using **Authorization Code + PKCE**, progressing from simple smoke tests to a
production-shaped re-frame + Polylith application.

If you want the **real application example**, start here:

👉 **`polylith/projects/webapp-pkce/`**

---

## Repo structure

```
smoke/            # Minimal static smoke test (no PKCE)
smoke-pkce/       # Static PKCE smoke test (pure JS)
polylith/         # re-frame + Polylith implementation (this section)
```

Each folder is **self-contained** and can be deployed independently.

---

## Polylith layout

```
polylith/
  components/
    oauth-pkce/        # PKCE + token exchange + refresh (PoC)
    app-config/        # Runtime config via window.__APP_CONFIG__
  bases/
    webapp/            # re-frame SPA
  projects/
    webapp-pkce/       # Deployable SPA + infra + scripts
      infra/cfn/
      scripts/
```

Infrastructure and deployment scripts live **with the deployable project**.

---

## ⚠️ Important: infrastructure lifecycle

Before deploying **anything** under `polylith/projects/webapp-pkce/`:

- Read the project README
- Destroy any previous smoke stacks using their project name
- Choose a unique `PROJECT_NAME`
- Destroy any previous stacks using the same name

CloudFormation will **not overwrite safely** if:

- bucket names collide
- Cognito domains already exist

---

## Learning goals (Polylith version)

This implementation demonstrates:

- Cognito Hosted UI (local + Google login)
- Authorization Code + PKCE (SPA-safe)
- Token refresh (PoC approach, documented tradeoffs)
- Route guarding (no routing libraries)
- Polylith component boundaries for auth logic

---

## Production note

This repo intentionally implements a **simple refresh-token strategy** using
`localStorage` so learners can see the mechanics.

👉 **Do not copy this verbatim into production**  
See: `docs/refresh-tokens.md` and `docs/migration-checklist.md`.

---

## Where to go next

- Start with: `projects/webapp-pkce/README.md`
- Then review:
  - `docs/local-signup-login.md`
  - `docs/refresh-tokens.md`
  - `docs/troubleshooting.md`
