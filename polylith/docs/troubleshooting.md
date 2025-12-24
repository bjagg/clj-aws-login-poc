# Troubleshooting

This repo is intentionally minimal, so most failures are configuration issues rather than code issues.

## OAuth / Hosted UI

### `redirect_uri_mismatch` (Google)

**Symptom:** Google shows `Error 400: redirect_uri_mismatch`.

**Cause:** The redirect URI being sent to Cognito/Google is not registered in Google Cloud Console.

**Fix:**

- In Google Cloud Console → OAuth client → Authorized redirect URIs, include:
  - `https://<cognito-domain-prefix>.auth.<region>.amazoncognito.com/oauth2/idpresponse`

### `error=invalid_request` + `error_description=invalid_scope`

**Symptom:** Cognito redirects back to `/callback` with `invalid_scope`.

**Cause:** Cognito/Google identity provider is requesting unsupported scopes from Google.
A common mistake is adding `offline_access` (Google doesn’t accept it as a scope).

**Fix:**

- In Cognito → Identity providers → Google → scopes, set:
  - `openid email profile`
- In the SPA `config.js`, keep:
  - `scopes: "openid email profile"`

### Redirecting to the wrong environment (prod vs localhost)

**Symptom:** Clicking Login redirects to the deployed site instead of localhost.

**Cause:** Your local `config.js` still contains the production redirect URL.

**Fix:**

- Local config should use:

  - `redirectUri: "http://localhost:3000/callback"`
  - `logoutUri: "http://localhost:3000/"`

## Static hosting (S3/CloudFront)

### SPA loads but JS is 403 from S3

**Symptom:** `GET .../app.js 403 (Forbidden)`.

**Cause:** You’re bypassing CloudFront and hitting the S3 origin directly.

**Fix:** Always load the site from the CloudFront URL / custom domain, not the S3 website/origin URL.

### CSP blocks inline scripts/styles (during smoke testing)

**Symptom:** Browser console shows CSP violations for inline `<script>`/`style`.

**Fix options:**

- Prefer external JS/CSS files (recommended).
- Or loosen CSP for the smoke test only (not recommended for production).

### CloudFormation deletion fails (bucket not empty)

**Symptom:** Stack delete fails because the S3 bucket contains objects.

**Fix:**

- Empty the bucket before deleting, or add a custom resource/lifecycle process if you want automated cleanup.

## “It works in one browser but not another”

Caching is the usual culprit.

**Fix:**

- Hard refresh (Cmd+Shift+R)
- Or clear site data for the origin (Application tab → Clear storage)
