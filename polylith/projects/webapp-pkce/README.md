# webapp-pkce (re-frame + Polylith)

This project is a **complete, deployable re-frame SPA** using:

- AWS Cognito Hosted UI
- Authorization Code + PKCE
- Google + local (Cognito) users
- Token refresh (PoC approach)
- This tutorial uses classic hosted UI (ManagedLoginVersion: 1) for widest compatibility (Lite plan)

---

## Prerequisites

- AWS account
- Domain registered in Route53
- Google OAuth client (for Google login)
- Node.js + npm
- Clojure CLI

### Optional: use a `.env` file

You may define all required environment variables in a `.env` file:

```bash
;; should be here already -- cd polylith/projects/webapp-pkce
cp scripts/.env.example .env
```

The deployment scripts will automatically load .env if present.

The .env file is ignored by git.

---

## ⚠️ Infrastructure warning (READ THIS)

This project creates **real AWS resources**:

- S3 bucket
- CloudFront distribution
- Cognito User Pool + App Client + Domain

> [!IMPORTANT]
> Changing values in `.env` (especially `PROJECT_NAME`,
> `COGNITO_DOMAIN_PREFIX`, or domain settings) may require tearing down
> existing stacks before redeploying.

also...

> [!WARNING]
> Do not use reserved words like `aws` in COGNITO_DOMAIN_PREFIX

Before deploying:

1. Pick a unique project name:

   ```bash
   export PROJECT_NAME=aws-login-pkce-demo
   ```

2. If you have ever deployed this before:

   ```bash
   ./scripts/destroy.sh
   ```

   - You may need to **empty the S3 bucket manually** if deletion fails.

> [!WARNING]
> Never reuse a `PROJECT_NAME` from another environment.

---

## 1. Deploy infrastructure

From this directory:

```bash
;; should be here already -- cd polylith/projects/webapp-pkce

./scripts/check-prereqs.sh
./scripts/deploy.sh
```

Take note of the outputs:

- Cognito domain
- UserPoolClientId
- CloudFront domain

---

## 2. Configure the SPA

Create `resources/public/config.js`:

```js
window.__APP_CONFIG__ = {
  cognitoDomain: "https://<prefix>.auth.us-east-1.amazoncognito.com",
  clientId: "<UserPoolClientId>",
  redirectUri: "http://localhost:3000/callback",
  logoutUri: "http://localhost:3000/",
  scopes: "openid email profile"
};
```

For production, update the URIs to your deployed domain.

---

## 3. Run locally

```bash
npm install
npx shadow-cljs watch app
```

Open:
```
http://localhost:3000
```

---

## 4. Test login flows

- Google login
- Local signup/login (Hosted UI)
- Token refresh (use **Force refresh** button)
- Guarded route: `/#/claims`

---

## 5. Tear down (important)

When finished:

```bash
./scripts/destroy.sh
```

If deletion fails:

- Empty the S3 bucket
- Re-run destroy

---

## What this project intentionally omits

- Backend-for-Frontend (BFF)
- Secure refresh token storage
- Server-side session management

These are discussed in `docs/migration-checklist.md`.

---

## Next steps

- Migrate components into your own app
- Replace PoC refresh with BFF/Lambda approach
- Enable refresh token rotation (production)
